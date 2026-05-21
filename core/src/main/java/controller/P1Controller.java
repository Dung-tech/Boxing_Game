package controller;

import entity.Fighter;
import input.InputController;

// P1 controller specialization (player one binding).
public class P1Controller extends FighterController {
    public P1Controller(Fighter fighter, InputController input) {
        super(fighter, input);
    }
}
