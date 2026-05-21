import cv2
import logging
import os
import socket
import sys
import time
import traceback
import ctypes
import struct

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)


LOGGER = logging.getLogger("ai_controller")
LOGGER.addHandler(logging.NullHandler())
LOGGER.propagate = False


def _log_unhandled_exception(exc_type, exc_value, exc_traceback):
    if issubclass(exc_type, KeyboardInterrupt):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        return
    LOGGER.error("Unhandled exception:\n%s", "".join(traceback.format_exception(exc_type, exc_value, exc_traceback)))


sys.excepthook = _log_unhandled_exception

from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer
from pose_detector import PoseDetector
from pose_gesture_recognizer import PoseGestureRecognizer
from gym_pose_detector import GymPoseDetector
from gym_pose_recognizer import GymPoseRecognizer

STATE_ACTIONS = {"IDLE", "BLOCK", "DUCK", "CONCENTRIC", "ECCENTRIC"}
STATE_RESEND_INTERVAL = 0.08
NONE_TO_IDLE_TIMEOUT = 0.30
GYM_SEND_INTERVAL = 0.12
PREVIEW_MIN_WIDTH = 200
PREVIEW_MIN_HEIGHT = 150
PREVIEW_DEFAULT_SCALE = 0.35
PREVIEW_DEFAULT_MARGIN = 16
PREVIEW_STREAM_DEFAULT_PORT = 65434
PREVIEW_STREAM_DEFAULT_FPS = 12.0
PREVIEW_STREAM_DEFAULT_WIDTH = 320
PREVIEW_STREAM_DEFAULT_JPEG_QUALITY = 70


def _parse_env_float(env_key, default_value):
    value = os.environ.get(env_key)
    if value is None or value.strip() == "":
        return default_value
    try:
        return float(value)
    except ValueError:
        return default_value


def _parse_env_int(env_key, default_value):
    value = os.environ.get(env_key)
    if value is None or value.strip() == "":
        return default_value
    try:
        return int(value)
    except ValueError:
        return default_value


def _is_truthy(value):
    if value is None:
        return False
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _resolve_preview_show_window(preview_streaming):
    raw = os.environ.get("AI_PREVIEW_SHOW_WINDOW")
    if raw is None or raw.strip() == "":
        return not preview_streaming
    return _is_truthy(raw)


def _get_screen_size():
    if os.name != "nt":
        return None
    try:
        user32 = ctypes.windll.user32
        try:
            user32.SetProcessDPIAware()
        except Exception:
            pass
        width = user32.GetSystemMetrics(0)
        height = user32.GetSystemMetrics(1)
        if width and height:
            return width, height
    except Exception:
        LOGGER.exception("Could not query screen size for preview window")
    return None


def _normalize_preview_corner(raw_value):
    if raw_value is None:
        return None
    corner = raw_value.strip().lower()
    if corner == "":
        return None
    if corner in {"1", "true", "yes", "on"}:
        return "top-right"
    valid = {"top-left", "top-right", "bottom-left", "bottom-right"}
    if corner in valid:
        return corner
    return None


def _setup_preview_window(window_title, cap):
    corner = _normalize_preview_corner(os.environ.get("AI_PREVIEW_CORNER"))
    if not corner:
        return False

    screen_size = _get_screen_size()
    if not screen_size:
        return False

    scale = _parse_env_float("AI_PREVIEW_SCALE", PREVIEW_DEFAULT_SCALE)
    if scale <= 0:
        scale = PREVIEW_DEFAULT_SCALE
    if scale > 1:
        scale = 1.0

    base_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)) or 640
    base_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)) or 480
    preview_width = max(PREVIEW_MIN_WIDTH, int(base_width * scale))
    preview_height = max(PREVIEW_MIN_HEIGHT, int(base_height * scale))

    margin = _parse_env_int("AI_PREVIEW_MARGIN", PREVIEW_DEFAULT_MARGIN)
    if margin < 0:
        margin = PREVIEW_DEFAULT_MARGIN

    screen_width, screen_height = screen_size
    if "right" in corner:
        x = max(0, screen_width - preview_width - margin)
    else:
        x = margin
    if "bottom" in corner:
        y = max(0, screen_height - preview_height - margin)
    else:
        y = margin

    cv2.namedWindow(window_title, cv2.WINDOW_NORMAL)
    cv2.resizeWindow(window_title, preview_width, preview_height)
    cv2.moveWindow(window_title, x, y)

    topmost = os.environ.get("AI_PREVIEW_TOPMOST", "").strip().lower()
    if topmost in {"1", "true", "yes", "on"} and hasattr(cv2, "WND_PROP_TOPMOST"):
        try:
            cv2.setWindowProperty(window_title, cv2.WND_PROP_TOPMOST, 1)
        except Exception:
            LOGGER.exception("Could not set preview window as topmost")

    return True


def _open_camera_with_fallback(camera_index=0):
    candidates = [("DEFAULT", None)]
    if hasattr(cv2, "CAP_DSHOW"):
        candidates.append(("DSHOW", cv2.CAP_DSHOW))
    if hasattr(cv2, "CAP_MSMF"):
        candidates.append(("MSMF", cv2.CAP_MSMF))

    last_cap = None
    for backend_name, backend_flag in candidates:
        try:
            if backend_flag is None:
                cap = cv2.VideoCapture(camera_index)
            else:
                cap = cv2.VideoCapture(camera_index, backend_flag)

            if last_cap is not None and last_cap is not cap:
                last_cap.release()

            last_cap = cap
            opened = cap is not None and cap.isOpened()
            backend_id = None
            if opened and hasattr(cv2, "CAP_PROP_BACKEND"):
                backend_id = cap.get(cv2.CAP_PROP_BACKEND)

            if opened:
                return cap
        except Exception:
            LOGGER.exception("Camera open raised exception for backend=%s", backend_name)

    return last_cap


def send_action(conn, player_tag, action, cache):
    now = time.monotonic()
    cache_entry = cache[player_tag]
    last_action = cache_entry["action"]
    last_time = cache_entry["time"]

    if action in STATE_ACTIONS and action == last_action and (now - last_time) < STATE_RESEND_INTERVAL:
        return True

    msg = f"{player_tag}:{action}\n"
    try:
        conn.sendall(msg.encode("utf-8"))
    except (socket.timeout, BlockingIOError):
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False

    cache_entry["action"] = action
    cache_entry["time"] = now

    return True


def send_gym_action(conn, action, cache):
    now = time.monotonic()
    cache_entry = cache["GYM"]
    last_action = cache_entry["action"]
    last_time = cache_entry["time"]

    if action == last_action and (now - last_time) < GYM_SEND_INTERVAL:
        return True

    msg = f"GYM:{action}\n"
    try:
        conn.sendall(msg.encode("utf-8"))
    except (socket.timeout, BlockingIOError):
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False

    cache_entry["action"] = action
    cache_entry["time"] = now
    return True

def main():
    mode = "CAMERA_AI"
    if len(sys.argv) > 1:
        mode = sys.argv[1].strip().upper()

    is_gym_mode = mode == "CAMERA_GYM_POSE"
    is_pose_mode = mode == "CAMERA_POSE"
    server_port = 65433 if is_gym_mode else 65432

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('127.0.0.1', server_port))
    server_socket.listen(1)

    conn, addr = server_socket.accept()
    conn.settimeout(0.03)

    preview_streaming = _is_truthy(os.environ.get("AI_PREVIEW_STREAM"))
    preview_show_window = _resolve_preview_show_window(preview_streaming)
    preview_server = None
    preview_conn = None
    preview_port = None
    preview_fps = None
    preview_width = None
    preview_quality = None
    preview_interval = None
    last_preview_time = 0.0

    if preview_streaming:
        preview_port = _parse_env_int("AI_PREVIEW_PORT", PREVIEW_STREAM_DEFAULT_PORT)
        preview_fps = _parse_env_float("AI_PREVIEW_FPS", PREVIEW_STREAM_DEFAULT_FPS)
        if preview_fps <= 0:
            preview_fps = PREVIEW_STREAM_DEFAULT_FPS
        preview_interval = 1.0 / preview_fps

        preview_width = _parse_env_int("AI_PREVIEW_WIDTH", PREVIEW_STREAM_DEFAULT_WIDTH)
        if preview_width <= 0:
            preview_width = PREVIEW_STREAM_DEFAULT_WIDTH

        preview_quality = _parse_env_int("AI_PREVIEW_JPEG_QUALITY", PREVIEW_STREAM_DEFAULT_JPEG_QUALITY)
        if preview_quality < 30 or preview_quality > 95:
            preview_quality = PREVIEW_STREAM_DEFAULT_JPEG_QUALITY

        try:
            preview_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            preview_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            preview_server.bind(('127.0.0.1', preview_port))
            preview_server.listen(1)
            preview_server.settimeout(0.2)
        except OSError:
            LOGGER.exception("Could not start preview stream on port %s", preview_port)
            preview_streaming = False
            preview_server = None
            if os.environ.get("AI_PREVIEW_SHOW_WINDOW") in (None, ""):
                preview_show_window = True

    cap = _open_camera_with_fallback(0)
    if cap is None or not cap.isOpened():
        LOGGER.error("Camera failed to open with all backends. Exiting early.")
        conn.close()
        server_socket.close()
        return

    detector = None
    recognizer_p1 = None
    recognizer_p2 = None

    if is_pose_mode:
        detector = PoseDetector()
        recognizer_p1 = PoseGestureRecognizer()
        recognizer_p2 = PoseGestureRecognizer()
    elif is_gym_mode:
        detector = GymPoseDetector()
        recognizer_p1 = GymPoseRecognizer()
        recognizer_p2 = None
    else:
        detector = HandDetector()
        recognizer_p1 = GestureRecognizer()
        recognizer_p2 = GestureRecognizer()
    send_cache = {
        "P1": {"action": None, "time": 0.0},
        "P2": {"action": None, "time": 0.0},
        "GYM": {"action": None, "time": 0.0},
    }
    last_non_none_time = {"P1": time.monotonic(), "P2": time.monotonic()}
    window_title = "Gym Pose Controller" if is_gym_mode else "Boxing AI - 2 Players Mode"
    if preview_show_window:
        _setup_preview_window(window_title, cap)

    try:
        while True:
            success, frame = cap.read()
            if not success:
                if is_gym_mode:
                    cap.release()
                    time.sleep(0.05)
                    cap = _open_camera_with_fallback(0)
                    if cap is None or not cap.isOpened():
                        LOGGER.error("Gym camera reconnect failed after read error")
                        break
                    continue
                break

            frame = cv2.flip(frame, 1)
            h, w, _ = frame.shape

            if is_pose_mode:
                pose_results = detector.detect(frame)
                gesture_p1 = recognizer_p1.recognize(pose_results["P1"])
                gesture_p2 = recognizer_p2.recognize(pose_results["P2"])
            elif is_gym_mode:
                try:
                    gym_landmarks = detector.detect(frame)
                    gesture_p1 = recognizer_p1.recognize(gym_landmarks)
                except Exception:
                    gesture_p1 = "NONE"
                gesture_p2 = "NONE"
            else:
                results = detector.count_fingers(frame)
                gesture_p1 = recognizer_p1.recognize(results["P1"])
                gesture_p2 = recognizer_p2.recognize(results["P2"])

            now = time.monotonic()
            if not is_gym_mode:
                if gesture_p1 != "NONE":
                    last_non_none_time["P1"] = now
                elif now - last_non_none_time["P1"] > NONE_TO_IDLE_TIMEOUT:
                    gesture_p1 = "IDLE"

                if gesture_p2 != "NONE":
                    last_non_none_time["P2"] = now
                elif now - last_non_none_time["P2"] > NONE_TO_IDLE_TIMEOUT:
                    gesture_p2 = "IDLE"

            if is_gym_mode:
                gym_action = gesture_p1 if gesture_p1 != "NONE" else "NONE"
                if not send_gym_action(conn, gym_action, send_cache):
                    break
            elif gesture_p1 != "NONE":
                if not send_action(conn, "P1", gesture_p1, send_cache):
                    break
            if gesture_p1 != "NONE":
                cv2.putText(frame, f"P1: {gesture_p1}", (50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 3)

            if not is_gym_mode and gesture_p2 != "NONE":
                if not send_action(conn, "P2", gesture_p2, send_cache):
                    break
                cv2.putText(frame, f"P2: {gesture_p2}", (w//2 + 50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 3)

            if not is_gym_mode:
                cv2.line(frame, (w//2, 0), (w//2, h), (255, 255, 0), 2)
                cv2.putText(frame, "P1 AREA", (w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
                cv2.putText(frame, "P2 AREA", (3*w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

            cv2.putText(frame, f"MODE: {mode}", (20, h - 20), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)
            if preview_streaming and preview_server is not None:
                if preview_conn is None:
                    try:
                        preview_conn, _ = preview_server.accept()
                        preview_conn.settimeout(0.05)
                    except socket.timeout:
                        preview_conn = None

                now = time.monotonic()
                if preview_conn is not None and (now - last_preview_time) >= preview_interval:
                    last_preview_time = now
                    try:
                        preview_height = max(1, int(h * (preview_width / float(w))))
                        preview_frame = cv2.resize(frame, (preview_width, preview_height), interpolation=cv2.INTER_AREA)
                        encoded, buffer = cv2.imencode(
                            ".jpg",
                            preview_frame,
                            [int(cv2.IMWRITE_JPEG_QUALITY), preview_quality],
                        )
                        if encoded:
                            payload = buffer.tobytes()
                            header = struct.pack(">I", len(payload))
                            preview_conn.sendall(header + payload)
                    except (socket.timeout, BlockingIOError):
                        pass
                    except (BrokenPipeError, ConnectionResetError, OSError):
                        try:
                            preview_conn.close()
                        except Exception:
                            pass
                        preview_conn = None
            if preview_show_window:
                cv2.imshow(window_title, frame)


            if preview_show_window:
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
    except (socket.timeout, BrokenPipeError, ConnectionResetError):
        LOGGER.exception("Socket connection error")
    except Exception:
        LOGGER.exception("Fatal runtime error")
    finally:
        conn.close()
        server_socket.close()
        if preview_conn is not None:
            try:
                preview_conn.close()
            except Exception:
                pass
        if preview_server is not None:
            try:
                preview_server.close()
            except Exception:
                pass
        cap.release()
        if preview_show_window:
            cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
