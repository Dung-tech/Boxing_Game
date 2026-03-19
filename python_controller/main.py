import cv2
import socket  # Thêm thư viện này
from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer

def main():
    # --- SETUP SOCKET ---
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('127.0.0.1', 5000))
    server_socket.listen(1)
    print("Python: Dang doi Java ket noi...")
    conn, addr = server_socket.accept()
    print(f"Python: Da ket noi voi Java tai {addr}")

    cap = cv2.VideoCapture(0)
    detector = HandDetector()
    recognizer = GestureRecognizer()

    try:
        while True:
            success, frame = cap.read()
            if not success: break
            frame = cv2.flip(frame, 1)

            fingers = detector.count_fingers(frame)
            gesture = recognizer.recognize(fingers)

            if gesture != "NONE":
                print(f"SENDING: {gesture}")
                # Gửi dữ liệu qua Java (phải có \n để Java readLine() nhận được)
                conn.sendall((gesture + "\n").encode('utf-8'))
                
                cv2.putText(frame, f"ACTION: {gesture}", (50, 100), 
                            cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 3)
            
            cv2.imshow("Hand Control System", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'): break
    finally:
        conn.close()
        server_socket.close()
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    main()