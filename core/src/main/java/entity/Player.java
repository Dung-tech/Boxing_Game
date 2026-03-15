package entity;

import util.Constants.Action;

public class Player {
    private int hp = 10;
    private int mana = 0;
    private Action currentAction = Action.IDLE;

    public void update(float delta) {
        // Logic tự hồi mana hoặc giảm cooldown nếu cần
    }

    // Các hàm để anh em gọi
    public void takeDamage(int dmg) { this.hp -= dmg; }
    public void addMana(int amount) { this.mana = Math.min(mana + amount, 10); }
    public void setAction(Action action) { this.currentAction = action; }

    public int getHp() { return hp; }
    public Action getAction() { return currentAction; }
}
