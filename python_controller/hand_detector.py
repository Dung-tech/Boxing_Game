import cv2
import mediapipe as mp

class HandDetector:
    def __init__(self):
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=1, # Chỉ cần 1 tay điều khiển cho chuẩn
            min_detection_confidence=0.7,
            min_tracking_confidence=0.7
        )

    def count_fingers(self, frame):
        results = self.hands.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        if not results.multi_hand_landmarks:
            return -1 # Không thấy tay

        hand_lms = results.multi_hand_landmarks[0]
        # Các đầu ngón tay: Trỏ(8), Giữa(12), Nhẫn(16), Út(20)
        finger_tips = [8, 12, 16, 20]
        count = 0

        # Kiểm tra ngón cái (tùy thuộc tay trái hay phải, ở đây làm đơn giản)
        if hand_lms.landmark[4].x < hand_lms.landmark[3].x: # Ngón cái mở
            count += 1

        # Kiểm tra 4 ngón còn lại
        for tip in finger_tips:
            if hand_lms.landmark[tip].y < hand_lms.landmark[tip - 2].y:
                count += 1
        
        return count