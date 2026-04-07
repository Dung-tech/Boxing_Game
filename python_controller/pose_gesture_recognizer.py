from collections import deque
import math
import time


# Shared tuning constants (global profile for both players/camera halves)
POSTURE_HISTORY_SIZE = 4
POSTURE_STABILITY_MIN = 2  # 2/4 for posture stabilization

VISIBILITY_MIN = 0.30
LOSS_HOLD_SECONDS = 0.22
BURST_COOLDOWN = 0.22
BURST_DEADBAND_SECONDS = 0.08

BASELINE_IDLE_WINDOW = 0.12
BASELINE_ALPHA = 0.06

GUARD_X_FACTOR = 0.55
GUARD_Y_FACTOR = 0.45

BLOCK_UP_Y_FACTOR = 0.04
BLOCK_CENTER_FACTOR = 0.42
BLOCK_CLOSE_FACTOR = 0.72

DUCK_HIP_FACTOR = 0.10
DUCK_HEAD_FACTOR = 0.06

PUNCH_EXT_FACTOR = 0.72
PUNCH_ELBOW_EXT_FACTOR = 0.26
PUNCH_MOTION_MIN = 0.020
PUNCH_Z_BASE_MIN = 0.055
PUNCH_Z_DELTA_MIN = 0.022

KICK_BOTH_EXT_FACTOR = 0.85
KICK_Y_ALIGN_FACTOR = 0.20
KICK_ARM_HEIGHT_FACTOR = 0.30


class PoseGestureRecognizer:
    def __init__(self):
        self.posture_history = deque(maxlen=POSTURE_HISTORY_SIZE)
        self.ready_for_next = True
        self.last_burst_time = 0.0
        self.burst_cooldown = BURST_COOLDOWN
        self.last_stable_posture = "IDLE"
        self.last_seen_time = time.monotonic()
        self.loss_hold_seconds = LOSS_HOLD_SECONDS
        self.deadband_until = 0.0

        self.base_hip_y = None
        self.base_nose_y = None
        self.base_left_wrist_z = None
        self.base_right_wrist_z = None

        self.prev_left_wrist_x = None
        self.prev_right_wrist_x = None
        self.prev_left_wrist_y = None
        self.prev_right_wrist_y = None
        self.prev_left_wrist_z = None
        self.prev_right_wrist_z = None
        # ...existing code...

    def _lm(self, lms, idx):
        lm = lms.landmark[idx]
        return lm.x, lm.y, lm.z, lm.visibility

    def _dist(self, a, b):
        return math.hypot(a[0] - b[0], a[1] - b[1])

    def recognize(self, pose_landmarks):
        now = time.monotonic()
        if pose_landmarks is None:
            self.posture_history.clear()
            # User rule: if body disappears from camera, treat as DUCK.
            self.ready_for_next = True
            self.last_stable_posture = "DUCK"
            return "DUCK"

        lms = pose_landmarks

        # MediaPipe Pose landmark indexes
        NOSE = 0
        LEFT_SHOULDER, RIGHT_SHOULDER = 11, 12
        LEFT_ELBOW, RIGHT_ELBOW = 13, 14
        LEFT_WRIST, RIGHT_WRIST = 15, 16
        LEFT_HIP, RIGHT_HIP = 23, 24
        # ...existing code...

        nose = self._lm(lms, NOSE)
        ls = self._lm(lms, LEFT_SHOULDER)
        rs = self._lm(lms, RIGHT_SHOULDER)
        le = self._lm(lms, LEFT_ELBOW)
        re = self._lm(lms, RIGHT_ELBOW)
        lw = self._lm(lms, LEFT_WRIST)
        rw = self._lm(lms, RIGHT_WRIST)
        lh = self._lm(lms, LEFT_HIP)
        rh = self._lm(lms, RIGHT_HIP)
        # ...existing code...

        critical_vis = [ls[3], rs[3], lw[3], rw[3], lh[3], rh[3]]
        if min(critical_vis) < VISIBILITY_MIN:
            # Low visibility is treated as a successful hide/duck.
            self.last_stable_posture = "DUCK"
            return "DUCK"

        self.last_seen_time = now

        shoulder_width = max(1e-4, self._dist(ls, rs))
        mid_shoulder_y = (ls[1] + rs[1]) * 0.5
        mid_hip_y = (lh[1] + rh[1]) * 0.5
        torso_len = max(1e-4, abs(mid_hip_y - mid_shoulder_y))
        chest_y = mid_shoulder_y + 0.25 * torso_len
        center_x = (ls[0] + rs[0]) * 0.5

        # Update running baselines in stable posture only.
        if self.base_hip_y is None:
            self.base_hip_y = mid_hip_y
            self.base_nose_y = nose[1]
            self.base_left_wrist_z = lw[2]
            self.base_right_wrist_z = rw[2]
        else:
            idleish = abs(mid_hip_y - self.base_hip_y) < BASELINE_IDLE_WINDOW and abs(nose[1] - self.base_nose_y) < BASELINE_IDLE_WINDOW
            if idleish:
                alpha = BASELINE_ALPHA
                self.base_hip_y = (1 - alpha) * self.base_hip_y + alpha * mid_hip_y
                self.base_nose_y = (1 - alpha) * self.base_nose_y + alpha * nose[1]
                self.base_left_wrist_z = (1 - alpha) * self.base_left_wrist_z + alpha * lw[2]
                self.base_right_wrist_z = (1 - alpha) * self.base_right_wrist_z + alpha * rw[2]

        # -------- Posture states --------
        left_guard = abs(lw[0] - ls[0]) < GUARD_X_FACTOR * shoulder_width and abs(lw[1] - chest_y) < GUARD_Y_FACTOR * torso_len
        right_guard = abs(rw[0] - rs[0]) < GUARD_X_FACTOR * shoulder_width and abs(rw[1] - chest_y) < GUARD_Y_FACTOR * torso_len
        is_idle = left_guard and right_guard

        block_hands_up = lw[1] < (chest_y + BLOCK_UP_Y_FACTOR * torso_len) and rw[1] < (chest_y + BLOCK_UP_Y_FACTOR * torso_len)
        block_hands_center = abs((lw[0] + rw[0]) * 0.5 - nose[0]) < BLOCK_CENTER_FACTOR * shoulder_width
        block_hands_close = abs(lw[0] - rw[0]) < BLOCK_CLOSE_FACTOR * shoulder_width
        is_block = block_hands_up and block_hands_center and block_hands_close

        duck_hip = mid_hip_y > (self.base_hip_y + DUCK_HIP_FACTOR * torso_len)
        duck_head = nose[1] > (self.base_nose_y + DUCK_HEAD_FACTOR * torso_len)
        is_duck = duck_hip and duck_head

        posture = "NONE"
        # Block-first precedence for ambiguous overlap.
        if is_block:
            posture = "BLOCK"
        elif is_duck:
            posture = "DUCK"
        elif is_idle:
            posture = "IDLE"

        self.posture_history.append(posture)
        stable_posture = "NONE"
        if len(self.posture_history) == self.posture_history.maxlen:
            block_count = self.posture_history.count("BLOCK")
            duck_count = self.posture_history.count("DUCK")
            idle_count = self.posture_history.count("IDLE")

            # Explicit 2/4 stabilization for BLOCK/DUCK.
            if block_count >= POSTURE_STABILITY_MIN:
                stable_posture = "BLOCK"
            elif duck_count >= POSTURE_STABILITY_MIN:
                stable_posture = "DUCK"
            elif idle_count >= POSTURE_STABILITY_MIN:
                stable_posture = "IDLE"

        if stable_posture == "IDLE":
            self.ready_for_next = True

        if stable_posture in {"IDLE", "BLOCK", "DUCK"}:
            self.last_stable_posture = stable_posture

        # Match finger-mode gate: only IDLE re-arms burst actions.
        if stable_posture == "IDLE":
            self.ready_for_next = True

        can_burst = (
            self.ready_for_next
            and now >= self.deadband_until
            and (now - self.last_burst_time) >= self.burst_cooldown
        )

        # -------- Burst gestures --------
        fists_together = self._dist(lw, rw) < 0.28 * shoulder_width and abs(((lw[1] + rw[1]) * 0.5) - chest_y) < 0.35 * torso_len

        prev_lx = lw[0] if self.prev_left_wrist_x is None else self.prev_left_wrist_x
        prev_rx = rw[0] if self.prev_right_wrist_x is None else self.prev_right_wrist_x
        prev_ly = lw[1] if self.prev_left_wrist_y is None else self.prev_left_wrist_y
        prev_ry = rw[1] if self.prev_right_wrist_y is None else self.prev_right_wrist_y
        prev_lz = lw[2] if self.prev_left_wrist_z is None else self.prev_left_wrist_z
        prev_rz = rw[2] if self.prev_right_wrist_z is None else self.prev_right_wrist_z

        left_ext = self._dist(lw, ls) > PUNCH_EXT_FACTOR * torso_len and self._dist(lw, le) > PUNCH_ELBOW_EXT_FACTOR * torso_len
        right_ext = self._dist(rw, rs) > PUNCH_EXT_FACTOR * torso_len and self._dist(rw, re) > PUNCH_ELBOW_EXT_FACTOR * torso_len
        left_thrust = (self.base_left_wrist_z - lw[2]) > PUNCH_Z_BASE_MIN or (prev_lz - lw[2]) > PUNCH_Z_DELTA_MIN
        right_thrust = (self.base_right_wrist_z - rw[2]) > PUNCH_Z_BASE_MIN or (prev_rz - rw[2]) > PUNCH_Z_DELTA_MIN
        left_motion = abs(lw[0] - prev_lx) + abs(lw[1] - prev_ly) > PUNCH_MOTION_MIN
        right_motion = abs(rw[0] - prev_rx) + abs(rw[1] - prev_ry) > PUNCH_MOTION_MIN

        # Make punch stricter: one hand must be clearly extended and moving.
        is_left_punch = left_ext and (left_motion or left_thrust)
        is_right_punch = right_ext and (right_motion or right_thrust)
        is_punch = is_left_punch or is_right_punch

        # Kick rule: both arms straight in front/at chest level.
        left_kick_ext = self._dist(lw, ls) > KICK_BOTH_EXT_FACTOR * torso_len
        right_kick_ext = self._dist(rw, rs) > KICK_BOTH_EXT_FACTOR * torso_len
        arms_aligned = abs(lw[1] - rw[1]) < KICK_Y_ALIGN_FACTOR * torso_len
        arms_not_low = lw[1] < (chest_y + KICK_ARM_HEIGHT_FACTOR * torso_len) and rw[1] < (chest_y + KICK_ARM_HEIGHT_FACTOR * torso_len)
        is_kick_substitute = left_kick_ext and right_kick_ext and arms_aligned and arms_not_low

        self.prev_left_wrist_x = lw[0]
        self.prev_right_wrist_x = rw[0]
        self.prev_left_wrist_y = lw[1]
        self.prev_right_wrist_y = rw[1]
        self.prev_left_wrist_z = lw[2]
        self.prev_right_wrist_z = rw[2]
        # ...existing code...

        if can_burst:
            if fists_together:
                self.ready_for_next = False
                self.last_burst_time = now
                self.deadband_until = now + BURST_DEADBAND_SECONDS
                return "SKILL"
            if is_punch:
                self.ready_for_next = False
                self.last_burst_time = now
                self.deadband_until = now + BURST_DEADBAND_SECONDS
                return "PUNCH"
            if is_kick_substitute:
                self.ready_for_next = False
                self.last_burst_time = now
                self.deadband_until = now + BURST_DEADBAND_SECONDS
                return "KICK"

        if stable_posture != "NONE":
            return stable_posture

        # Any non-matching posture falls back to default IDLE state.
        return "IDLE"

