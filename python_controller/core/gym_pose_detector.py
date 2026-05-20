import cv2
import mediapipe as mp

from mediapipe_utils import select_pose_model_complexity

mp_pose = mp.solutions.pose


class GymPoseDetector:
    # Single-player pose detector with minimal overlay for gym exercises.
    def __init__(self):
        model_complexity = select_pose_model_complexity(default=0)
        self.pose = mp_pose.Pose(
            static_image_mode=False,
            model_complexity=model_complexity,
            smooth_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )

    def detect(self, frame):
        # Run pose detection on full frame.
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        rgb.flags.writeable = False

        result = self.pose.process(rgb)
        # Draw only upper arms to keep preview clean.
        if result.pose_landmarks:
            self._draw_upper_arms_only(frame, result.pose_landmarks)

        return result.pose_landmarks

    def _draw_upper_arms_only(self, frame, pose_landmarks):
        # Custom lightweight skeleton for shoulders/elbows/wrists only.
        h, w, _ = frame.shape
        # Only keep shoulders, elbows, wrists (left/right).
        key_ids = [11, 12, 13, 14, 15, 16]
        edges = [(11, 12), (11, 13), (13, 15), (12, 14), (14, 16)]

        coords = {}
        for idx in key_ids:
            lm = pose_landmarks.landmark[idx]
            if lm.visibility < 0.3:
                continue
            x = int(lm.x * w)
            y = int(lm.y * h)
            coords[idx] = (x, y)

        for a, b in edges:
            if a in coords and b in coords:
                cv2.line(frame, coords[a], coords[b], (0, 255, 255), 2)

        for idx in key_ids:
            if idx in coords:
                cv2.circle(frame, coords[idx], 5, (0, 255, 0), -1)
