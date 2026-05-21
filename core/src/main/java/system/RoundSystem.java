package system;

import util.Constants;
import entity.Fighter;

// Round timer, win tracking, and end-of-round transitions.
public class RoundSystem {
    public enum RoundResult {
        P1,
        P2,
        DRAW
    }

    private int currentRound = 1;
    private float timeLeft = Constants.ROUND_TIME;
    private boolean roundEnded = false;
    private int p1RoundWins = 0;
    private int p2RoundWins = 0;
    private RoundResult lastRoundResult = RoundResult.DRAW;
    private boolean transitionReady = true;
    private float endDelayTimer = 0f;
    private float endDelayDuration = 0f;

    public void update(float delta, Fighter p1, Fighter p2) {
        if (roundEnded) {
            if (!transitionReady) {
                endDelayTimer += delta;
                if (endDelayTimer >= endDelayDuration) {
                    transitionReady = true;
                }
            }
            return;
        }

        timeLeft -= delta;

        if (timeLeft <= 0 || p1.isDead() || p2.isDead()) {
            endCurrentRound(p1, p2);
        }
    }

    private void endCurrentRound(Fighter p1, Fighter p2) {
        roundEnded = true;

        if (p1.isDead() && !p2.isDead()) {
            p2RoundWins++;
            lastRoundResult = RoundResult.P2;
        } else if (p2.isDead() && !p1.isDead()) {
            p1RoundWins++;
            lastRoundResult = RoundResult.P1;
        } else if (p1.getHp() > p2.getHp()) {
            p1RoundWins++;
            lastRoundResult = RoundResult.P1;
        } else if (p2.getHp() > p1.getHp()) {
            p2RoundWins++;
            lastRoundResult = RoundResult.P2;
        } else {
            lastRoundResult = RoundResult.DRAW;
        }
        // Hòa thì không ai được điểm

        boolean skillKO = (p1.isDead() && p1.wasLastHitBySkill()) ||
            (p2.isDead() && p2.wasLastHitBySkill());
        float delay = Constants.ROUND_SCOREBOARD_SECONDS;
        if (skillKO) {
            delay = Math.max(delay, Constants.SKILL_ROUND_END_DELAY_SECONDS);
        }
        startEndDelay(delay);
    }

    private void startEndDelay(float delaySeconds) {
        endDelayDuration = delaySeconds;
        endDelayTimer = 0f;
        transitionReady = endDelayDuration <= 0f;
    }

    public boolean isRoundEnded() {
        return roundEnded;
    }

    public boolean isTransitionReady() {
        return transitionReady;
    }

    public RoundResult getLastRoundResult() {
        return lastRoundResult;
    }

    public boolean isMatchEnded() {
        return p1RoundWins >= Constants.ROUNDS_TO_WIN ||
            p2RoundWins >= Constants.ROUNDS_TO_WIN ||
            (roundEnded && currentRound >= Constants.TOTAL_ROUNDS); // Kết thúc sau hiệp 3 nếu chưa có ai đủ 2 hiệp
    }

    public void nextRound() {
        if (isMatchEnded()) return;

        currentRound++;
        timeLeft = Constants.ROUND_TIME;
        roundEnded = false;
        lastRoundResult = RoundResult.DRAW;
        transitionReady = true;
        endDelayTimer = 0f;
        endDelayDuration = 0f;
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
        lastRoundResult = RoundResult.DRAW;
        transitionReady = true;
        endDelayTimer = 0f;
        endDelayDuration = 0f;
    }}
