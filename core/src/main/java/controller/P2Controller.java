package controller;

import entity.Fighter;
import input.InputController;

// P2 controller specialization (player two binding).
public class P2Controller extends FighterController {
    public P2Controller(Fighter fighter, InputController input) {
        super(fighter, input);
    }
}
