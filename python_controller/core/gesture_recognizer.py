class GestureRecognizer:
    def __init__(self):
        self.ready_for_next = True
        self.finger_history = []
        self.required_frames = 2

    def recognize(self, finger_count):
        if finger_count == -1: return "IDLE" # Không thấy tay thì về IDLE

        self.finger_history.append(finger_count)
        if len(self.finger_history) > self.required_frames:
            self.finger_history.pop(0)

        if len(self.finger_history) == self.required_frames and len(set(self.finger_history)) == 1:
            stable_count = self.finger_history[0]

            # --- NHÓM TRẠNG THÁI (Gửi liên tục) ---
            if stable_count == 0:
                self.ready_for_next = True # Reset để chuẩn bị đấm phát tiếp theo
                return "IDLE"
            if stable_count == 3: return "BLOCK"
            if stable_count == 4: return "DUCK"

            # --- NHÓM CHIÊU THỨC (Chỉ gửi 1 lần duy nhất) ---
            if self.ready_for_next:
                if stable_count == 1:
                    self.ready_for_next = False
                    return "PUNCH"
                if stable_count == 2:
                    self.ready_for_next = False
                    return "KICK"
                if stable_count == 5:
                    self.ready_for_next = False
                    return "SKILL"

        return "NONE" # Không có thay đổi gì đặc biệt
