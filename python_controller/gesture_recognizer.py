class GestureRecognizer:
    def __init__(self):
        self.ready_for_next = True
        self.finger_history = []
        self.required_frames = 3 # Phải giữ nguyên số ngón trong 3 frame mới nhận

    def recognize(self, finger_count):
        # Bộ lọc làm mượt (Smoothing)
        self.finger_history.append(finger_count)
        if len(self.finger_history) > self.required_frames:
            self.finger_history.pop(0)

        # Kiểm tra nếu tất cả frame trong lịch sử đều cùng 1 số ngón
        if len(self.finger_history) == self.required_frames and len(set(self.finger_history)) == 1:
            stable_count = self.finger_history[0]
            
            # Reset khi nắm tay (0 ngón)
            if stable_count <= 0:
                self.ready_for_next = True
                return "NONE"

            # Kích hoạt hành động
            if self.ready_for_next:
                action = "NONE"
                if stable_count == 1: action = "PUNCH"
                elif stable_count == 2: action = "KICK"
                elif stable_count == 3: action = "BLOCK_PUNCH"
                elif stable_count == 4: action = "BLOCK_KICK"
                elif stable_count == 5: action = "USE_SKILL"

                if action != "NONE":
                    self.ready_for_next = False
                    return action
        
        return "NONE"