package entity;

public class Gymer {
	private static final int MAX_HP = 10;

	public enum TrainingState {
		CONCENTRIC,
		ECCENTRIC
	}

	private TrainingState trainingState = TrainingState.CONCENTRIC;
	private int hp = MAX_HP;
	private boolean eccentricPhaseDone = false;

	public void setConcentric() {
		if (trainingState == TrainingState.ECCENTRIC && eccentricPhaseDone && hp > 0) {
			hp--;
			eccentricPhaseDone = false;
		}
		trainingState = TrainingState.CONCENTRIC;
	}

	public void setEccentric() {
		eccentricPhaseDone = true;
		trainingState = TrainingState.ECCENTRIC;
	}

	public boolean isConcentric() {
		return trainingState == TrainingState.CONCENTRIC;
	}

	public String getStateLabel() {
		return trainingState.name();
	}

	public int getHp() {
		return hp;
	}

	public int getMaxHp() {
		return MAX_HP;
	}

	public boolean isExhausted() {
		return hp <= 0;
	}

	public void reset() {
		hp = MAX_HP;
		trainingState = TrainingState.CONCENTRIC;
		eccentricPhaseDone = false;
	}
}
