package controller;

import entity.Fighter;
import input.InputController;
import util.Constants.Action;

public abstract class FighterController {
    protected Fighter fighter;
    protected InputController input;

    public FighterController(Fighter fighter, InputController input) {
        this.fighter = fighter;
        this.input = input;
    }

    public void update(float delta) {
        input.update(delta);
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
        else fighter.performAction(Action.IDLE);

        input.reset();
    }
}
