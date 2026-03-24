package controller;

import entity.Fighter;

public class P1Controller extends FighterController {
    
    public P1Controller(Fighter fighter) {
        super(fighter, 1); // Đăng ký là Player 1
    }

    @Override
    protected void handleAction(String action) {
        System.out.println("[P1 Logic] Thực thi: " + action);
        
        switch (action) {
            case "PUNCH":
                fighter.punch();
                break;
            case "KICK":
                fighter.kick();
                break;
            case "BLOCK_PUNCH":
                fighter.blockHigh(); // Giả sử Fighter có hàm đỡ cao
                break;
            case "BLOCK_KICK":
                fighter.blockLow();  // Giả sử Fighter có hàm đỡ thấp
                break;
            case "USE_SKILL":
                fighter.useSpecialMove();
                break;
        }
    }
}
