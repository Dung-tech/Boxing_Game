package system;

import entity.Gymer;

public class GymState {
    private static final float MESSI_STATE_INTERVAL = 2f;

    private boolean gameOver = false;
    private int gameOverSelected = 0;
    private boolean messiIsEating = true;
    private float messiStateTimer = 0f;

    public void update(float delta, Gymer gymer) {
        if (gameOver) {
            return;
        }

        updateMessiState(delta);
        if (gymer.isExhausted()) {
            gameOver = true;
            gameOverSelected = 0;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getGameOverSelected() {
        return gameOverSelected;
    }

    public boolean isMessiEating() {
        return messiIsEating;
    }

    public boolean isContinueSelected() {
        return gameOverSelected == 0;
    }

    public void selectPreviousOption() {
        gameOverSelected = (gameOverSelected - 1 + 2) % 2;
    }

    public void selectNextOption() {
        gameOverSelected = (gameOverSelected + 1) % 2;
    }

    public void resetAfterRestart() {
        gameOver = false;
        gameOverSelected = 0;
        messiIsEating = true;
        messiStateTimer = 0f;
    }

    private void updateMessiState(float delta) {
        messiStateTimer += delta;
        if (messiStateTimer >= MESSI_STATE_INTERVAL) {
            messiStateTimer = 0f;
            messiIsEating = !messiIsEating;
        }
    }
}
