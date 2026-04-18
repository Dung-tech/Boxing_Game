import cv2
import logging
import os
import socket
import sys
import tempfile
import time
import traceback
import ctypes

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
            window_title = "Gym Pose Controller" if mode == "CAMERA_GYM_POSE" else "Boxing AI - 2 Players Mode"
            cv2.imshow(window_title, frame)


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
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
