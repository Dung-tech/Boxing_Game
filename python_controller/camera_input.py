# camera_input.py
import cv2
import time
import logging

class CameraInput:
    def __init__(self, camera_id=0, width=640, height=480, reconnect_interval=5):
        self.camera_id = camera_id
        self.cap = None
        self.width = width
        self.height = height
        self.reconnect_interval = reconnect_interval
        logging.basicConfig(level=logging.INFO)

    def open_camera(self):
        if self.cap is not None:
            self.cap.release()
        self.cap = cv2.VideoCapture(self.camera_id)
        # Cấu hình độ phân giải
        self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, self.width)
        self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, self.height)
        if not self.cap.isOpened():
            logging.error(f"Không mở được camera #{self.camera_id}")
            return False
        logging.info(f"Camera #{self.camera_id} đã kết nối.")
        return True

    def get_frame(self):
        # Mở camera nếu chưa mở
        if self.cap is None or not self.cap.isOpened():
            if not self.open_camera():
                return None
        # Đọc frame
        ret, frame = self.cap.read()
        if not ret:
            logging.warning("Không đọc được frame từ camera, thử kết nối lại.")
            self.cap.release()
            time.sleep(self.reconnect_interval)
            if self.open_camera():
                ret, frame = self.cap.read()
            if not ret:
                return None
        return frame
