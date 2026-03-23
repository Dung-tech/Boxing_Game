package controller;

import entity.Fighter;
import input.InputController;
import util.Constants;
import util.Constants.Action;

public abstract class FighterController {
    protected Fighter fighter;
    protected InputController input;

    public FighterController(Fighter fighter, InputController input) {
        this.fighter = fighter;
        this.input = input;
    }

    public void update(float delta) {
        // 1. Cập nhật trạng thái input (đọc phím hoặc nhận dữ liệu từ Camera)
        input.update(delta);

        // 2. Kiểm tra lệnh và ra lệnh cho Fighter thực hiện
        // Thứ tự ưu tiên có thể tùy chỉnh: Skill > Punch > Kick...
        if (input.skill()) {
            fighter.performAction(Action.SKILL);
        } else if (input.punch()) {
            fighter.performAction(Action.PUNCH);
        } else if (input.kick()) {
            fighter.performAction(Action.KICK);
        } else if (input.duck()) {
            fighter.performAction(Action.DUCK);
        } else if (input.block()) {
            fighter.performAction(Action.BLOCK);
        }
        else {
            // [THÊM] Khi không bấm gì, trả nhân vật về IDLE (để thoát trạng thái BLOCK/DUCK)
            fighter.performAction(Action.IDLE);
        }

        // 3. Reset trạng thái input để tránh việc bấm 1 lần mà đấm mãi mãi
        input.reset();
    }
}
