import cv2
import socket
from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer

def main():
    # --- Cấu hình Socket (Cổng 5005 theo Constants.java của ông) ---
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(('127.0.0.1', 5005))
    server_socket.listen(1)
    print("AI: Dang doi Game Java ket noi tai cong 5005...")

    # Tạm thời comment 2 dòng dưới nếu ông muốn test cam mà không cần bật Java
    # conn, addr = server_socket.accept()
    # print(f"AI: Da ket noi voi Java qua {addr}")
    conn = None

    cap = cv2.VideoCapture(0)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

    detector = HandDetector()
    recognizer = GestureRecognizer()

    while True:
        success, frame = cap.read()
        if not success: break
        frame = cv2.flip(frame, 1)

        # Bước 1: Nhận diện và Vẽ khung xương
        frame = detector.find_hands(frame, draw=True)

        # Bước 2: Nhận diện hành động
        fingers = detector.count_fingers()
        gesture = recognizer.recognize(fingers)

        # Bước 3: Hiển thị lên màn hình (Luôn hiện GESTURE: IDLE)
        color = (0, 255, 0) if gesture not in ["IDLE"] else (200, 200, 200)
        cv2.putText(frame, f"GESTURE: {gesture}", (50, 80),
                    cv2.FONT_HERSHEY_SIMPLEX, 2, color, 4)

        # Gửi dữ liệu qua Java
        if conn and gesture != "IDLE":
            try:
                conn.sendall((gesture + "\n").encode('utf-8'))
            except:
                print("Loi ket noi voi Java.")
                break

        cv2.imshow("HUST Boxing Game Controller", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'): break

    if conn: conn.close()
    server_socket.close()
    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
