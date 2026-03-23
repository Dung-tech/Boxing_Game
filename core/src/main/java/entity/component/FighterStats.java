package entity.component;

import util.Constants;
import util.Constants.Action;

public class FighterStats {
    public int hp = Constants.MAX_HP;
    public float mana = 0;
    public boolean hasHit = false;

    public Action current = Action.IDLE;
    public float timer = 0;
    private final float DURATION = 0.5f;

    public void takeDamage(float damage) {
        this.hp = (int) Math.max(0, this.hp - damage);
    }

    public void addMana(float amount) {
        this.mana = Math.min(Constants.MAX_MANA, this.mana + amount);
    }
    public void update(float delta) {
        if (isTriggerAction()) {
            timer += delta;
            if (timer >= DURATION) {
                reset();
            }
        }
    }

    public boolean isTriggerAction() {
        return current == Action.PUNCH || current == Action.KICK || current == Action.SKILL;
    }

    public void reset() {
        current = Action.IDLE;
        timer = 0;
    }
}
