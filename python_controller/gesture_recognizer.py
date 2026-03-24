class GestureRecognizer:
    def __init__(self):
        self.ready_for_next = True
        self.finger_history = []
        # required_frames = 2 giúp nhận diện cực nhạy mà không bị nhiễu
        self.required_frames = 2 

    def recognize(self, finger_count):
        # Nếu không thấy tay, reset trạng thái ngay lập tức
        if finger_count == -1:
            self.finger_history.clear()
            self.ready_for_next = True
            return "NONE"

        self.finger_history.append(finger_count)
        if len(self.finger_history) > self.required_frames:
            self.finger_history.pop(0)

        # Kiểm tra độ ổn định của ngón tay (tất cả frame trong history phải giống nhau)
        if len(self.finger_history) == self.required_frames and len(set(self.finger_history)) == 1:
            stable_count = self.finger_history[0]
            
            # SỬA LỖI SPAM: Khi nắm tay (0 ngón) thì mới Reset trạng thái "SẴN SÀNG"
            if stable_count <= 0:
                self.ready_for_next = True
                return "NONE"

            # Chỉ ra chiêu khi ở trạng thái Ready
            if self.ready_for_next:
                mapping = {1: "PUNCH", 2: "KICK", 3: "BLOCK_PUNCH", 4: "BLOCK_KICK", 5: "USE_SKILL"}
                action = mapping.get(stable_count, "NONE")

                if action != "NONE":
                    # Khóa lại ngay lập tức, bắt người chơi phải nắm tay lại mới đấm được phát thứ 2
                    self.ready_for_next = False 
                    return action
        
        return "NONE"
