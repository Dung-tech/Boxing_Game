import os

import mediapipe as mp


def select_pose_model_complexity(default=0):
    base_dir = os.path.dirname(mp.__file__)
    lite_path = os.path.join(base_dir, "modules", "pose_landmark", "pose_landmark_lite.tflite")
    if os.path.exists(lite_path):
        return default

    full_path = os.path.join(base_dir, "modules", "pose_landmark", "pose_landmark_full.tflite")
    if os.path.exists(full_path):
        return 1

    return default
