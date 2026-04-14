import cv2
import os
import socket
import sys
import time

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
if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)

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

    # 1. --- SETUP SOCKET ---
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Cho phép chạy lại server ngay lập tức nếu bị crash (tránh lỗi Address already in use)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('127.0.0.1', server_port))
    server_socket.listen(1)

    print("Python: [WAITING] Dang doi Java ket noi...")
    conn, addr = server_socket.accept()
    conn.settimeout(0.03)
    print(f"Python: [CONNECTED] Da ket noi voi Java tai {addr}")

    # 2. --- KHOI TAO CAMERA & AI ---
    cap = cv2.VideoCapture(0)
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

    try:
        while True:
            success, frame = cap.read()
            if not success:
                if mode == "CAMERA_GYM_POSE":
                    # Gym mode self-heals transient camera glitches instead of exiting.
                    cap.release()
                    time.sleep(0.05)
                    cap = cv2.VideoCapture(0)
                    continue
                break

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
    except Exception as e:
        print(f"Loi: {e}")
    finally:
        print("Python: Dang dong ket noi...")
        conn.close()
        server_socket.close()
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
