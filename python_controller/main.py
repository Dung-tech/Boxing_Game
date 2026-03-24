import cv2
import socket
from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer

def main():
    # 1. --- SETUP SOCKET ---
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Cho phép chạy lại server ngay lập tức nếu bị crash (tránh lỗi Address already in use)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('127.0.0.1', 5000))
    server_socket.listen(1)

    print("Python: [WAITING] Dang doi Java ket noi...")
    conn, addr = server_socket.accept()
    print(f"Python: [CONNECTED] Da ket noi voi Java tai {addr}")

    # 2. --- KHOI TAO CAMERA & AI ---
    cap = cv2.VideoCapture(0)
    detector = HandDetector()

    # Tao 2 thuc the rieng biet de khong bi lan lon lich su ngon tay cua 2 nguoi
    recognizer_p1 = GestureRecognizer()
    recognizer_p2 = GestureRecognizer()

    try:
        while True:
            success, frame = cap.read()
            if not success: break

            # Lat anh (Mirror effect) de nguoi choi dieu khien de hon
            frame = cv2.flip(frame, 1)
            h, w, _ = frame.shape

            # 3. --- NHAN DIEN TAY ---
            # results se la dict: {"P1": count, "P2": count}
            results = detector.count_fingers(frame)

            # --- XU LY PLAYER 1 (Ben trai) ---
            gesture_p1 = recognizer_p1.recognize(results["P1"])
            if gesture_p1 != "NONE":
                msg = f"P1:{gesture_p1}\n"
                conn.sendall(msg.encode('utf-8'))
                print(f"SENDING: {msg.strip()}")
                cv2.putText(frame, f"P1: {gesture_p1}", (50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 3)

            # --- XU LY PLAYER 2 (Ben phai) ---
            gesture_p2 = recognizer_p2.recognize(results["P2"])
            if gesture_p2 != "NONE":
                msg = f"P2:{gesture_p2}\n"
                conn.sendall(msg.encode('utf-8'))
                print(f"SENDING: {msg.strip()}")
                cv2.putText(frame, f"P2: {gesture_p2}", (w//2 + 50, 100),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 3)

            # 4. --- HIEN THI GUI ---
            # Ve vach ke chia doi man hinh
            cv2.line(frame, (w//2, 0), (w//2, h), (255, 255, 0), 2)
            cv2.putText(frame, "P1 AREA", (w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
            cv2.putText(frame, "P2 AREA", (3*w//4 - 50, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

            cv2.imshow("Boxing AI - 2 Players Mode", frame)


            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
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
