package system;

import util.Constants;
import entity.Fighter;

public class RoundSystem {
    private int currentRound = 1;
    private float timeLeft = Constants.ROUND_TIME;
    private boolean roundEnded = false;
    private int p1RoundWins = 0;
    private int p2RoundWins = 0;

    public void update(float delta, Fighter p1, Fighter p2) {
        if (roundEnded) return;

        timeLeft -= delta;

        if (timeLeft <= 0 || p1.isDead() || p2.isDead()) {
            endCurrentRound(p1, p2);
        }
    }

    private void endCurrentRound(Fighter p1, Fighter p2) {
        roundEnded = true;

        if (p1.isDead() && !p2.isDead()) {
            p2RoundWins++;
        } else if (p2.isDead() && !p1.isDead()) {
            p1RoundWins++;
        } else if (p1.getHp() > p2.getHp()) {
            p1RoundWins++;
        } else if (p2.getHp() > p1.getHp()) {
            p2RoundWins++;
        }
        // Hòa thì không ai được điểm
    }

    public boolean isRoundEnded() {
        return roundEnded;
    }

    public boolean isMatchEnded() {
        return currentRound >= Constants.TOTAL_ROUNDS ||
            p1RoundWins >= 2 || p2RoundWins >= 2;  // thắng 2 hiệp trước là thắng luôn
    }

    public void nextRound() {
        if (isMatchEnded()) return;

        currentRound++;
        timeLeft = Constants.ROUND_TIME;
        roundEnded = false;
    }

    // Getters
    public int getCurrentRound() { return currentRound; }
    public float getTimeLeft() { return timeLeft; }
    public int getP1RoundWins() { return p1RoundWins; }
    public int getP2RoundWins() { return p2RoundWins; }
    public void reset() {
        currentRound = 1;
        timeLeft = Constants.ROUND_TIME;
        roundEnded = false;
        p1RoundWins = 0;
        p2RoundWins = 0;
    }}
