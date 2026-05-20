import cv2
import mediapipe as mp

# Khởi tạo các công cụ của MediaPipe một lần duy nhất ở cấp độ module
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils

class HandDetector:
    def __init__(self):
        # max_num_hands=2 để AI quét cả 2 vùng màn hình
        self.hands = mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=2,
            min_detection_confidence=0.7,
            min_tracking_confidence=0.7
        )

    def count_fingers(self, frame):
        # Reset kết quả là -1 (No Hand) mỗi frame
        results_dict = {"P1": -1, "P2": -1}
        img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = self.hands.process(img_rgb)

        if results.multi_hand_landmarks:
            for hand_lms in results.multi_hand_landmarks:
                # Lấy tọa độ x của cổ tay (Landmark 0) để phân loại P1/P2
                wrist_x = hand_lms.landmark[0].x

                # PHÂN LOẠI P1/P2 (Fix lỗi ghi đè dữ liệu)
                # Dựa trên vạch kẻ giữa màn hình (0.5)
                target_player = "NONE"
                if wrist_x < 0.5:
                    target_player = "P1" # Bên trái màn hình
                else:
                    target_player = "P2" # Bên phải màn hình

                # Nếu slot người chơi đó đã có tay rồi, thì bỏ qua tay này
                # Cách này giúp khóa đúng 2 tay ở 2 vùng riêng biệt
                if results_dict[target_player] != -1:
                    continue

                # Vẽ xương bàn tay cho người chơi được nhận diện
                mp_drawing.draw_landmarks(frame, hand_lms, mp_hands.HAND_CONNECTIONS)

                # Logic đếm ngón tay chuẩn: Tip cao hơn PIP (khớp giữa)
                count = 0
                # Ngón cái (Landmark 4)
                if hand_lms.landmark[4].x < hand_lms.landmark[3].x:
                    count += 1

                # 4 ngón còn lại: Chỉ đếm là MỞ khi đầu ngón (Tip) cao hơn khớp giữa (PIP)
                finger_tips = [8, 12, 16, 20]
                for tip in finger_tips:
                    # y càng nhỏ càng cao
                    if hand_lms.landmark[tip].y < hand_lms.landmark[tip - 2].y:
                        count += 1

                results_dict[target_player] = count

        return results_dict
