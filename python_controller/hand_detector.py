import cv2
import mediapipe as mp

class HandDetector:
    def __init__(self):
        self.mp_hands = mp.solutions.hands
        self.mp_draw = mp.solutions.drawing_utils # Công cụ vẽ
        self.hands = self.mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=1,
            min_detection_confidence=0.8, # Tăng lên 0.8 để bắt nét hơn
            min_tracking_confidence=0.8
        )

    def find_hands(self, frame, draw=True):
        img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        self.results = self.hands.process(img_rgb)

        if self.results.multi_hand_landmarks:
            for hand_lms in self.results.multi_hand_landmarks:
                if draw:
                    # Vẽ các điểm mốc và đường nối (khung xương)
                    self.mp_draw.draw_landmarks(
                        frame, hand_lms, self.mp_hands.HAND_CONNECTIONS,
                        self.mp_draw.DrawingSpec(color=(0, 255, 0), thickness=2, circle_radius=2),
                        self.mp_draw.DrawingSpec(color=(0, 0, 255), thickness=2)
                    )
        return frame

    def count_fingers(self):
        if not self.results.multi_hand_landmarks:
            return -1

        hand_lms = self.results.multi_hand_landmarks[0]
        finger_tips = [8, 12, 16, 20]
        count = 0

        # Kiểm tra ngón cái (Thumb) - Logic này chuẩn cho tay phải hướng vào cam
        if hand_lms.landmark[4].x < hand_lms.landmark[3].x:
            count += 1

        # 4 ngón còn lại
        for tip in finger_tips:
            if hand_lms.landmark[tip].y < hand_lms.landmark[tip - 2].y:
                count += 1
        return count
