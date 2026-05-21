import cv2
import mediapipe as mp
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils

class HandDetector:
    def __init__(self):
        self.hands = mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=2,
            min_detection_confidence=0.7,
            min_tracking_confidence=0.7
        )

    def count_fingers(self, frame):
        results_dict = {"P1": -1, "P2": -1}
        img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = self.hands.process(img_rgb)

        if results.multi_hand_landmarks:
            for hand_lms in results.multi_hand_landmarks:
                wrist_x = hand_lms.landmark[0].x

                target_player = "NONE"
                if wrist_x < 0.5:
                    target_player = "P1"
                else:
                    target_player = "P2"

                if results_dict[target_player] != -1:
                    continue

                mp_drawing.draw_landmarks(frame, hand_lms, mp_hands.HAND_CONNECTIONS)

                count = 0
                if hand_lms.landmark[4].x < hand_lms.landmark[3].x:
                    count += 1

                finger_tips = [8, 12, 16, 20]
                for tip in finger_tips:
                    if hand_lms.landmark[tip].y < hand_lms.landmark[tip - 2].y:
                        count += 1

                results_dict[target_player] = count

        return results_dict
