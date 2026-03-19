package entity;

import util.Constants;
import util.Constants.Action;
import util.Constants.Side;

public class Fighter {
    private Side side;
    private Action currentState;
    private float hp, mana;

    public Fighter(Side side) {
        this.side = side;
        this.hp = Constants.MAX_HP; // 20 máu
        this.mana = 0;
        this.currentState = Action.IDLE;
    }

    public void update(float delta) {
        // Tạm thời để trống cho Dev 2 làm logic đếm ngược thời gian đấm
    }

    public void performAction(Action action) {
        this.currentState = action;
        System.out.println(side + " thực hiện: " + action);
    }

    public void takeDamage(float damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;
    }

    // Getters để vẽ UI và tính va chạm
    public float getHp() { return hp; }
    public Action getCurrentState() { return currentState; }
}
