import cv2
from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer

def main():
    cap = cv2.VideoCapture(0)
    detector = HandDetector()
    recognizer = GestureRecognizer()

    while True:
        success, frame = cap.read()
        if not success: break
        frame = cv2.flip(frame, 1)

        # 1. Đếm ngón tay
        fingers = detector.count_fingers(frame)
        
        # 2. Nhận diện hành động kèm logic Reset
        gesture = recognizer.recognize(fingers)

        # 3. Hiển thị
        if gesture != "NONE":
            print(f"KÍCH HOẠT: {gesture}")
            cv2.putText(frame, f"ACTION: {gesture}", (50, 100), 
                        cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 3)
        
        cv2.putText(frame, f"Fingers: {fingers if fingers >=0 else 0}", (10, 30), 
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        cv2.imshow("Hand Control System", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'): break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()