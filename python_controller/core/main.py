import cv2
import logging
import os
import socket
import sys
import tempfile
import time
import traceback
import ctypes
import struct
import shutil
import urllib.request

# Anchor paths so running from IDE, script, or bundled exe still works.
if getattr(sys, "frozen", False):
    SCRIPT_DIR = os.path.dirname(os.path.abspath(sys.executable))
else:
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


def resolve_project_root():
    # Installed layout: app root contains assets and AI_Controller.exe.
    if os.path.exists(os.path.join(SCRIPT_DIR, "assets")):
        return SCRIPT_DIR

    # Dev layout: this file sits under python_controller/ and assets is one level up.
    parent = os.path.dirname(SCRIPT_DIR)
    if os.path.exists(os.path.join(parent, "assets")):
        return parent

    return SCRIPT_DIR


PROJECT_ROOT = resolve_project_root()
ASSETS_DIR = os.path.join(PROJECT_ROOT, "assets")
APP_NAME = "BoxingGame"


def _get_windows_local_appdata():
    if os.name != "nt":
        return None

    # Use Windows API first because LOCALAPPDATA env can be stale/broken on some machines.
    try:
        buffer = ctypes.create_unicode_buffer(260)
        csidl_local_appdata = 28
        result = ctypes.windll.shell32.SHGetFolderPathW(None, csidl_local_appdata, None, 0, buffer)
        if result == 0 and buffer.value:
            return buffer.value
    except Exception:
        pass

    return None


def _is_dir_usable(path):
    if not path:
        return False
    try:
        os.makedirs(path, exist_ok=True)
        test_file = os.path.join(path, ".write_test.tmp")
        with open(test_file, "w", encoding="utf-8") as fh:
            fh.write("ok")
        os.remove(test_file)
        return True
    except OSError:
        return False


def resolve_log_file_path():
    windows_local_appdata = _get_windows_local_appdata()
    env_local_appdata = os.environ.get("LOCALAPPDATA")
    home_local_appdata = os.path.join(os.path.expanduser("~"), "AppData", "Local")
    temp_dir = tempfile.gettempdir()

    base_candidates = [windows_local_appdata, env_local_appdata, home_local_appdata, temp_dir]
    seen = set()

    for base in base_candidates:
        if not base:
            continue
        norm_base = os.path.normpath(base)
        if norm_base in seen:
            continue
        seen.add(norm_base)

        candidate_dir = os.path.join(norm_base, APP_NAME)
        if _is_dir_usable(candidate_dir):
            return os.path.join(candidate_dir, "ai_controller.log")

    # Final fallback keeps process alive even in heavily restricted environments.
    return os.path.join(tempfile.gettempdir(), "ai_controller.log")


LOG_FILE_PATH = resolve_log_file_path()
if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)


def setup_logging():
    file_handler = None
    try:
        file_handler = logging.FileHandler(LOG_FILE_PATH, mode="a", encoding="utf-8")
    except OSError:
        # Last fallback keeps process alive even if file logging cannot be created.
        file_handler = logging.StreamHandler()

    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
        handlers=[file_handler],
        force=True,
    )
    logger = logging.getLogger("ai_controller")
    logger.info("=" * 72)
    logger.info("AI controller process started")
    logger.info("argv=%s", sys.argv)
    logger.info("frozen=%s", getattr(sys, "frozen", False))
    logger.info("executable=%s", sys.executable)
    logger.info("cwd=%s", os.getcwd())
    logger.info("script_dir=%s", SCRIPT_DIR)
    logger.info("project_root=%s", PROJECT_ROOT)
    logger.info("assets_dir_exists=%s", os.path.exists(ASSETS_DIR))
    logger.info("log_file=%s", LOG_FILE_PATH)
    return logger


LOGGER = setup_logging()


def _log_unhandled_exception(exc_type, exc_value, exc_traceback):
    if issubclass(exc_type, KeyboardInterrupt):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        return
    LOGGER.error("Unhandled exception:\n%s", "".join(traceback.format_exception(exc_type, exc_value, exc_traceback)))


sys.excepthook = _log_unhandled_exception

def _configure_mediapipe_cache():
    cache_root = os.path.join(os.path.dirname(LOG_FILE_PATH), "mediapipe")
    os.makedirs(cache_root, exist_ok=True)

    import mediapipe as mp
    from mediapipe.python.solutions import download_utils

    original_download = download_utils.download_oss_model
    gcs_prefix = getattr(download_utils, "_GCS_URL_PREFIX", "https://storage.googleapis.com/mediapipe-assets/")
    mp_root_path = os.sep.join(os.path.abspath(download_utils.__file__).split(os.sep)[:-4])

    def _download_to_cache(*args, **kwargs):
        model_path = None
        model_url = None

        if args:
            if len(args) == 1:
                model_path = args[0]
            else:
                first, second = args[0], args[1]
                if isinstance(first, str) and first.startswith("http"):
                    model_url, model_path = first, second
                else:
                    model_path, model_url = first, second

        if model_path is None:
            model_path = kwargs.get("model_path")
            model_url = kwargs.get("model_url", model_url)

        if not model_path:
            return original_download(*args, **kwargs)

        packaged_path = os.path.join(mp_root_path, model_path.replace("/", os.sep))
        if os.path.exists(packaged_path):
            return None

        model_name = os.path.basename(model_path)
        cache_path = os.path.join(cache_root, model_name)
        if not os.path.exists(cache_path):
            if not model_url:
                model_url = gcs_prefix + model_name
            try:
                with urllib.request.urlopen(model_url) as response, open(cache_path, "wb") as output:
                    if hasattr(response, "code") and response.code not in (None, 200):
                        raise ConnectionError(f"Cannot download {model_path} (HTTP {response.code})")
                    output.write(response.read())
            except Exception:
                LOGGER.exception("Failed to download MediaPipe model: %s", model_path)
                return None

        try:
            os.makedirs(os.path.dirname(packaged_path), exist_ok=True)
            shutil.copy2(cache_path, packaged_path)
        except OSError:
            LOGGER.warning("MediaPipe model cached at %s; package path not writable.", cache_path)
        return None

    download_utils.download_oss_model = _download_to_cache
    LOGGER.info("MediaPipe cache directory: %s", cache_root)


_configure_mediapipe_cache()

from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer
from pose_detector import PoseDetector
from pose_gesture_recognizer import PoseGestureRecognizer
from gym_pose_detector import GymPoseDetector
from gym_pose_recognizer import GymPoseRecognizer

STATE_ACTIONS = {"IDLE", "BLOCK", "DUCK", "CONCENTRIC", "ECCENTRIC"}
STATE_RESEND_INTERVAL = 0.08  # ~12.5 Hz
NONE_TO_IDLE_TIMEOUT = 0.30
GYM_SEND_INTERVAL = 0.12
DEBUG_SEND = False
PREVIEW_MIN_WIDTH = 200
PREVIEW_MIN_HEIGHT = 150
PREVIEW_DEFAULT_SCALE = 0.35
PREVIEW_DEFAULT_MARGIN = 16
PREVIEW_STREAM_DEFAULT_PORT = 65434
PREVIEW_STREAM_DEFAULT_FPS = 12.0
PREVIEW_STREAM_DEFAULT_WIDTH = 320
PREVIEW_STREAM_DEFAULT_JPEG_QUALITY = 70


def _log_cv2_diagnostics():
    LOGGER.info("OpenCV version: %s", getattr(cv2, "__version__", "unknown"))
    try:
        build_info = cv2.getBuildInformation()
        focus_terms = ["Video I/O", "DirectShow", "Media Foundation", "MSMF", "FFMPEG", "GStreamer"]
        for line in build_info.splitlines():
            stripped = line.strip()
            if any(term in stripped for term in focus_terms):
                LOGGER.info("cv2-build: %s", stripped)
    except Exception:
        LOGGER.exception("Could not read OpenCV build information")


def _parse_env_float(env_key, default_value):
    value = os.environ.get(env_key)
    if value is None or value.strip() == "":
        return default_value
    try:
        return float(value)
    except ValueError:
        LOGGER.warning("Invalid %s=%s; using %s", env_key, value, default_value)
        return default_value


def _parse_env_int(env_key, default_value):
    value = os.environ.get(env_key)
    if value is None or value.strip() == "":
        return default_value
    try:
        return int(value)
    except ValueError:
        LOGGER.warning("Invalid %s=%s; using %s", env_key, value, default_value)
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
        LOGGER.info("Preview corner is only supported on Windows.")
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
    LOGGER.warning("Invalid AI_PREVIEW_CORNER=%s; expected %s", raw_value, ", ".join(sorted(valid)))
    return None


def _setup_preview_window(window_title, cap):
    corner = _normalize_preview_corner(os.environ.get("AI_PREVIEW_CORNER"))
    if not corner:
        return False

    screen_size = _get_screen_size()
    if not screen_size:
        LOGGER.info("Preview corner requested but screen size unavailable.")
        return False

    scale = _parse_env_float("AI_PREVIEW_SCALE", PREVIEW_DEFAULT_SCALE)
    if scale <= 0:
        LOGGER.warning("AI_PREVIEW_SCALE must be > 0; using %s", PREVIEW_DEFAULT_SCALE)
        scale = PREVIEW_DEFAULT_SCALE
    if scale > 1:
        LOGGER.warning("AI_PREVIEW_SCALE too large; clamping to 1.0")
        scale = 1.0

    base_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)) or 640
    base_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)) or 480
    preview_width = max(PREVIEW_MIN_WIDTH, int(base_width * scale))
    preview_height = max(PREVIEW_MIN_HEIGHT, int(base_height * scale))

    margin = _parse_env_int("AI_PREVIEW_MARGIN", PREVIEW_DEFAULT_MARGIN)
    if margin < 0:
        LOGGER.warning("AI_PREVIEW_MARGIN must be >= 0; using %s", PREVIEW_DEFAULT_MARGIN)
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

    LOGGER.info(
        "Preview window anchored at %s size=%sx%s pos=%s,%s",
        corner,
        preview_width,
        preview_height,
        x,
        y,
    )
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

            LOGGER.info(
                "Camera open attempt: index=%s backend=%s opened=%s backend_id=%s",
                camera_index,
                backend_name,
                opened,
                backend_id,
            )

            if opened:
                return cap
        except Exception:
            LOGGER.exception("Camera open raised exception for backend=%s", backend_name)

    return last_cap


def send_action(conn, player_tag, action, cache):
    now = time.monotonic()
    last_action = cache[player_tag]["action"]
    last_time = cache[player_tag]["time"]

    # Burst gestures must always pass through; posture states are sent only on change/throttled.
    if action in STATE_ACTIONS and action == last_action and (now - last_time) < STATE_RESEND_INTERVAL:
        return True

    msg = f"{player_tag}:{action}\n"
    try:
        conn.sendall(msg.encode("utf-8"))
    except (socket.timeout, BlockingIOError):
        # Under burst movement, drop stale frame command and keep loop running.
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False

    cache[player_tag]["action"] = action
    cache[player_tag]["time"] = now

    if DEBUG_SEND:
        print(f"SENDING: {msg.strip()}")

    return True


def send_gym_action(conn, action, cache):
    now = time.monotonic()
    last_action = cache["GYM"]["action"]
    last_time = cache["GYM"]["time"]

    if action == last_action and (now - last_time) < GYM_SEND_INTERVAL:
        return True

    msg = f"GYM:{action}\n"
    try:
        conn.sendall(msg.encode("utf-8"))
    except (socket.timeout, BlockingIOError):
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False

    cache["GYM"]["action"] = action
    cache["GYM"]["time"] = now
    return True

def main():
    mode = "CAMERA_AI"
    if len(sys.argv) > 1:
        mode = sys.argv[1].strip().upper()

    gym_mode = mode == "CAMERA_GYM_POSE"
    server_port = 65433 if gym_mode else 65432

    LOGGER.info("Selected mode=%s, server_port=%s", mode, server_port)
    _log_cv2_diagnostics()

    # 1. --- SETUP SOCKET ---
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Cho phep chay lai server ngay lap tuc neu bi crash (tranh loi Address already in use)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('127.0.0.1', server_port))
    server_socket.listen(1)

    print("Python: [WAITING] Dang doi Java ket noi...")
    LOGGER.info("Waiting for Java connection on 127.0.0.1:%s", server_port)
    conn, addr = server_socket.accept()
    conn.settimeout(0.03)
    print(f"Python: [CONNECTED] Da ket noi voi Java tai {addr}")
    LOGGER.info("Connected to Java peer at %s", addr)

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
            LOGGER.warning("AI_PREVIEW_FPS must be > 0; using %s", PREVIEW_STREAM_DEFAULT_FPS)
            preview_fps = PREVIEW_STREAM_DEFAULT_FPS
        preview_interval = 1.0 / preview_fps

        preview_width = _parse_env_int("AI_PREVIEW_WIDTH", PREVIEW_STREAM_DEFAULT_WIDTH)
        if preview_width <= 0:
            LOGGER.warning("AI_PREVIEW_WIDTH must be > 0; using %s", PREVIEW_STREAM_DEFAULT_WIDTH)
            preview_width = PREVIEW_STREAM_DEFAULT_WIDTH

        preview_quality = _parse_env_int("AI_PREVIEW_JPEG_QUALITY", PREVIEW_STREAM_DEFAULT_JPEG_QUALITY)
        if preview_quality < 30 or preview_quality > 95:
            LOGGER.warning("AI_PREVIEW_JPEG_QUALITY must be 30-95; using %s", PREVIEW_STREAM_DEFAULT_JPEG_QUALITY)
            preview_quality = PREVIEW_STREAM_DEFAULT_JPEG_QUALITY

        try:
            preview_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            preview_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            preview_server.bind(('127.0.0.1', preview_port))
            preview_server.listen(1)
            preview_server.settimeout(0.2)
            LOGGER.info("Preview stream enabled on 127.0.0.1:%s", preview_port)
        except OSError:
            LOGGER.exception("Could not start preview stream on port %s", preview_port)
            preview_streaming = False
            preview_server = None
            if os.environ.get("AI_PREVIEW_SHOW_WINDOW") in (None, ""):
                preview_show_window = True

    # 2. --- KHOI TAO CAMERA & AI ---
    cap = _open_camera_with_fallback(0)
    if cap is None or not cap.isOpened():
        LOGGER.error("Camera failed to open with all backends. Exiting early.")
        conn.close()
        server_socket.close()
        return

    detector = None
    recognizer_p1 = None
    recognizer_p2 = None

    if mode == "CAMERA_POSE":
        detector = PoseDetector()
        recognizer_p1 = PoseGestureRecognizer()
        recognizer_p2 = PoseGestureRecognizer()
    elif mode == "CAMERA_GYM_POSE":
        detector = GymPoseDetector()
        recognizer_p1 = GymPoseRecognizer()
        recognizer_p2 = None
    else:
        detector = HandDetector()
        # Tao 2 thuc the rieng biet de khong bi lan lon lich su ngon tay cua 2 nguoi
        recognizer_p1 = GestureRecognizer()
        recognizer_p2 = GestureRecognizer()
    send_cache = {
        "P1": {"action": None, "time": 0.0},
        "P2": {"action": None, "time": 0.0},
        "GYM": {"action": None, "time": 0.0},
    }
    last_non_none_time = {"P1": time.monotonic(), "P2": time.monotonic()}
    window_title = "Gym Pose Controller" if mode == "CAMERA_GYM_POSE" else "Boxing AI - 2 Players Mode"
    if preview_show_window:
        _setup_preview_window(window_title, cap)

    read_failures = 0

    try:
        while True:
            success, frame = cap.read()
            if not success:
                read_failures += 1
                LOGGER.warning("Camera read failed (count=%s, mode=%s)", read_failures, mode)
                if mode == "CAMERA_GYM_POSE":
                    # Gym mode self-heals transient camera glitches instead of exiting.
                    cap.release()
                    time.sleep(0.05)
                    cap = _open_camera_with_fallback(0)
                    if cap is None or not cap.isOpened():
                        LOGGER.error("Gym camera reconnect failed after read error")
                        break
                    continue
                break
            read_failures = 0

            # Lat anh (Mirror effect) de nguoi choi dieu khien de hon
            frame = cv2.flip(frame, 1)
            h, w, _ = frame.shape

            # 3. --- NHAN DIEN ---
            if mode == "CAMERA_POSE":
                pose_results = detector.detect(frame)
                gesture_p1 = recognizer_p1.recognize(pose_results["P1"])
                gesture_p2 = recognizer_p2.recognize(pose_results["P2"])
            elif mode == "CAMERA_GYM_POSE":
                try:
                    gym_landmarks = detector.detect(frame)
                    gesture_p1 = recognizer_p1.recognize(gym_landmarks)
                except Exception:
                    # Ignore noisy frame failures; keep camera loop alive.
                    gesture_p1 = "NONE"
                gesture_p2 = "NONE"
            else:
                # results se la dict: {"P1": count, "P2": count}
                results = detector.count_fingers(frame)
                gesture_p1 = recognizer_p1.recognize(results["P1"])
                gesture_p2 = recognizer_p2.recognize(results["P2"])

            now = time.monotonic()
            if mode != "CAMERA_GYM_POSE":
                if gesture_p1 != "NONE":
                    last_non_none_time["P1"] = now
                elif now - last_non_none_time["P1"] > NONE_TO_IDLE_TIMEOUT:
                    gesture_p1 = "IDLE"

                if gesture_p2 != "NONE":
                    last_non_none_time["P2"] = now
                elif now - last_non_none_time["P2"] > NONE_TO_IDLE_TIMEOUT:
                    gesture_p2 = "IDLE"

            # --- XU LY PLAYER 1 (Ben trai) ---
            if mode == "CAMERA_GYM_POSE":
                gym_action = gesture_p1 if gesture_p1 != "NONE" else "NONE"
                if not send_gym_action(conn, gym_action, send_cache):
                    print("Python: [INFO] Gym receiver da ngat, dong camera gym...")
                    break
            elif gesture_p1 != "NONE":
                if mode != "CAMERA_GYM_POSE":
                    if not send_action(conn, "P1", gesture_p1, send_cache):
                        break
            if gesture_p1 != "NONE":
                cv2.putText(frame, f"P1: {gesture_p1}", (50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 3)

            # --- XU LY PLAYER 2 (Ben phai) ---
            if mode != "CAMERA_GYM_POSE" and gesture_p2 != "NONE":
                if not send_action(conn, "P2", gesture_p2, send_cache):
                    break
                cv2.putText(frame, f"P2: {gesture_p2}", (w//2 + 50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 3)

            # 4. --- HIEN THI GUI ---
            if mode != "CAMERA_GYM_POSE":
                # Ve vach ke chia doi man hinh
                cv2.line(frame, (w//2, 0), (w//2, h), (255, 255, 0), 2)
                cv2.putText(frame, "P1 AREA", (w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
                cv2.putText(frame, "P2 AREA", (3*w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

            cv2.putText(frame, f"MODE: {mode}", (20, h - 20), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)
            if preview_streaming and preview_server is not None:
                if preview_conn is None:
                    try:
                        preview_conn, _ = preview_server.accept()
                        preview_conn.settimeout(0.05)
                        LOGGER.info("Preview client connected")
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
                        # Drop preview frame if receiver is slow.
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
    except (socket.timeout, BrokenPipeError, ConnectionResetError) as e:
        print(f"Loi ket noi socket: {e}")
        LOGGER.exception("Socket connection error")
    except Exception as e:
        print(f"Loi: {e}")
        LOGGER.exception("Fatal runtime error")
    finally:
        print("Python: Dang dong ket noi...")
        LOGGER.info("Shutting down controller")
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
