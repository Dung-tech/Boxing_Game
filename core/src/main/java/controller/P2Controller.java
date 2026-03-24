package controller;

import entity.Fighter;

public class P2Controller extends FighterController {

    public P2Controller(Fighter fighter) {
        super(fighter, 2); // Đăng ký là Player 2
    }

    @Override
    protected void handleAction(String action) {
        System.out.println("[P2 Logic] Thực thi: " + action);
        
        switch (action) {
            case "PUNCH":
                fighter.punch();
                break;
            case "KICK":
                fighter.kick();
                break;
            case "BLOCK_PUNCH":
                fighter.blockHigh();
                break;
            case "BLOCK_KICK":
                fighter.blockLow();
                break;
            case "USE_SKILL":
                fighter.useSpecialMove();
                break;
        }
    }
}