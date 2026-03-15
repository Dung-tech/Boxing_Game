package controller;

import entity.Player;
import input.InputController;
import util.Constants.Action;

public class PlayerController {
    private Player player;
    private InputController input;

    public PlayerController(Player player, InputController input) {
        this.player = player;
        this.input = input;
    }

    public void handleInput(float delta) {
        input.update(delta); // AI hoặc Phím cập nhật dữ liệu

        if (input.punch()) player.setAction(Action.PUNCH);
        else if (input.kick()) player.setAction(Action.KICK);
        else if (input.duck()) player.setAction(Action.DUCK);
        else if (input.block()) player.setAction(Action.BLOCK);
        else if (input.skill()) player.setAction(Action.SKILL);
        else player.setAction(Action.IDLE);
    }
}
