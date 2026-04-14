from collections import deque
import math


VISIBILITY_MIN = 0.35
HISTORY_SIZE = 5
STABLE_MIN = 3

# Elbow-angle thresholds for push-up phases (slightly looser for real-time noise).
CONCENTRIC_MIN_ANGLE = 160.0
ECCENTRIC_MAX_ANGLE = 140.0
ECCENTRIC_MIN_ANGLE = 50.0


class GymPoseRecognizer:
	def __init__(self):
		self.state_history = deque(maxlen=HISTORY_SIZE)
		self.last_stable_state = "CONCENTRIC"

	def _lm(self, lms, idx):
		lm = lms.landmark[idx]
		return lm.x, lm.y, lm.z, lm.visibility

	def _dist(self, a, b):
		return math.hypot(a[0] - b[0], a[1] - b[1])

	def _angle(self, a, b, c):
		ba = (a[0] - b[0], a[1] - b[1])
		bc = (c[0] - b[0], c[1] - b[1])
		norm_ba = math.hypot(ba[0], ba[1])
		norm_bc = math.hypot(bc[0], bc[1])
		if norm_ba < 1e-6 or norm_bc < 1e-6:
			return 180.0
		cos_val = max(-1.0, min(1.0, (ba[0] * bc[0] + ba[1] * bc[1]) / (norm_ba * norm_bc)))
		return math.degrees(math.acos(cos_val))

	def recognize(self, pose_landmarks):
		if pose_landmarks is None:
			self.state_history.clear()
			return "NONE"

		LEFT_SHOULDER, RIGHT_SHOULDER = 11, 12
		LEFT_ELBOW, RIGHT_ELBOW = 13, 14
		LEFT_WRIST, RIGHT_WRIST = 15, 16

		ls = self._lm(pose_landmarks, LEFT_SHOULDER)
		rs = self._lm(pose_landmarks, RIGHT_SHOULDER)
		le = self._lm(pose_landmarks, LEFT_ELBOW)
		re = self._lm(pose_landmarks, RIGHT_ELBOW)
		lw = self._lm(pose_landmarks, LEFT_WRIST)
		rw = self._lm(pose_landmarks, RIGHT_WRIST)

		critical_vis = [ls[3], rs[3], le[3], re[3], lw[3], rw[3]]
		if min(critical_vis) < VISIBILITY_MIN:
			return "NONE"

		shoulder_width = max(1e-4, self._dist(ls, rs))
		if shoulder_width < 0.06:
			return "NONE"

		shoulder_level_delta = abs(ls[1] - rs[1])
		shoulder_level_ok = shoulder_level_delta < (0.45 * shoulder_width)

		left_angle = self._angle(ls, le, lw)
		right_angle = self._angle(rs, re, rw)
		avg_angle = (left_angle + right_angle) * 0.5

		candidate = "NONE"
		if shoulder_level_ok and avg_angle >= CONCENTRIC_MIN_ANGLE:
			candidate = "CONCENTRIC"
		elif shoulder_level_ok and ECCENTRIC_MIN_ANGLE <= avg_angle <= ECCENTRIC_MAX_ANGLE:
			candidate = "ECCENTRIC"

		if candidate != "NONE":
			self.state_history.append(candidate)

		if len(self.state_history) == self.state_history.maxlen:
			concentric_count = self.state_history.count("CONCENTRIC")
			eccentric_count = self.state_history.count("ECCENTRIC")
			if concentric_count >= STABLE_MIN:
				self.last_stable_state = "CONCENTRIC"
			elif eccentric_count >= STABLE_MIN:
				self.last_stable_state = "ECCENTRIC"

		if candidate == "NONE":
			return "NONE"

		return self.last_stable_state

