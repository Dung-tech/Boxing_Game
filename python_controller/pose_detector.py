import cv2
import mediapipe as mp

mp_pose = mp.solutions.pose
mp_drawing = mp.solutions.drawing_utils


class PoseDetector:
    def __init__(self):
        # Separate models per side to keep tracking stable for each player half.
        self.pose_p1 = mp_pose.Pose(
            static_image_mode=False,
            model_complexity=0,
            smooth_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )
        self.pose_p2 = mp_pose.Pose(
            static_image_mode=False,
            model_complexity=0,
            smooth_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )

    def detect(self, frame):
        h, w, _ = frame.shape
        mid = w // 2

        p1_roi = frame[:, :mid]
        p2_roi = frame[:, mid:]

        p1_rgb = cv2.cvtColor(p1_roi, cv2.COLOR_BGR2RGB)
        p2_rgb = cv2.cvtColor(p2_roi, cv2.COLOR_BGR2RGB)
        p1_rgb.flags.writeable = False
        p2_rgb.flags.writeable = False

        p1_result = self.pose_p1.process(p1_rgb)
        p2_result = self.pose_p2.process(p2_rgb)

        if p1_result.pose_landmarks:
            mp_drawing.draw_landmarks(p1_roi, p1_result.pose_landmarks, mp_pose.POSE_CONNECTIONS)
        if p2_result.pose_landmarks:
            mp_drawing.draw_landmarks(p2_roi, p2_result.pose_landmarks, mp_pose.POSE_CONNECTIONS)

        return {
            "P1": p1_result.pose_landmarks,
            "P2": p2_result.pose_landmarks,
        }

