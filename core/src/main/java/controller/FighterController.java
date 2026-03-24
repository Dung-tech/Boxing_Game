package controller;

import entity.Fighter;
import input.GestureReceiver;

public abstract class FighterController {
    protected Fighter fighter;
    protected int playerId; // 1 cho P1, 2 cho P2

    public FighterController(Fighter fighter, int playerId) {
        this.fighter = fighter;
        this.playerId = playerId;
    }

    /**
     * Hàm này được gọi liên tục trong vòng lặp Render của Game (LibGDX)
     */
    public void update(float delta) {
        if (fighter == null) return;

        // Lấy hành động từ "ngăn chứa" riêng của Player này trong GestureReceiver
        String action = GestureReceiver.getInstance().getActionForPlayer(playerId);

        // Nếu có hành động thực sự (khác NONE) thì mới xử lý
        if (action != null && !action.equals("NONE")) {
            handleAction(action);
        }
    }

    // Mỗi người chơi có thể có logic xử lý khác nhau nếu cần
    protected abstract void handleAction(String action);
}
