class GestureRecognizer:
    def __init__(self):
        self.last_stable_action = "IDLE"
        self.finger_history = []
        self.required_frames = 2 # Độ trễ cực thấp cho máy 16GB RAM

    def recognize(self, finger_count):
        # 1. Bộ lọc làm mượt
        self.finger_history.append(finger_count)
        if len(self.finger_history) > self.required_frames:
            self.finger_history.pop(0)

        # 2. Kiểm tra tính ổn định
        if len(self.finger_history) == self.required_frames and len(set(self.finger_history)) == 1:
            stable_count = self.finger_history[0]

            # Map số ngón sang tên hành động
            current_action = "IDLE"
            if stable_count == 1: current_action = "PUNCH"
            elif stable_count == 2: current_action = "KICK"
            elif stable_count == 3: current_action = "BLOCK"
            elif stable_count == 4: current_action = "DUCK"
            elif stable_count == 5: current_action = "SKILL"
            elif stable_count <= 0: current_action = "IDLE"

            # 3. Logic phân loại Tín hiệu vs Trạng thái
            triggers = ["PUNCH", "KICK", "SKILL"]
            states = ["BLOCK", "DUCK", "IDLE"]

            # Nếu là HÀNH ĐỘNG (Trigger): Chỉ gửi khi nó KHÁC với hành động trước đó
            if current_action in triggers:
                if current_action != self.last_stable_action:
                    self.last_stable_action = current_action
                    return current_action
                else:
                    return "IDLE" # Đã gửi rồi thì trả về IDLE để tránh spam đấm

            # Nếu là TRẠNG THÁI (State): Gửi liên tục
            if current_action in states:
                self.last_stable_action = current_action
                return current_action

        return "IDLE"
