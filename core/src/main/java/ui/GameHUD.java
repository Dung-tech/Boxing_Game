package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entity.Fighter;
import system.RoundSystem;
import util.Constants;

public class GameHUD {
    private HealthBar hpBar;
    private ManaBar manaBar;
    private BitmapFont roundFont;

    public GameHUD() {
        hpBar = new HealthBar(Color.RED);
        manaBar = new ManaBar();
        roundFont = new BitmapFont();
        roundFont.getData().setScale(1.1f);
    }

    public void render(SpriteBatch batch, Fighter p1, Fighter p2, RoundSystem roundSystem) {
        hpBar.draw(batch, p1.getHp(), Constants.MAX_HP, 50, 650);
        manaBar.draw(batch, p1.getMana(), 50, 610);

        hpBar.draw(batch, p2.getHp(), Constants.MAX_HP, Constants.APP_WIDTH - 350, 650);
        manaBar.draw(batch, p2.getMana(), Constants.APP_WIDTH - 350, 610);

        String roundText = "Round " + roundSystem.getCurrentRound() +
            "  |  Wins: P1 " + roundSystem.getP1RoundWins() + " - " + roundSystem.getP2RoundWins() + " P2";
        roundFont.draw(batch, roundText, Constants.APP_WIDTH / 2f - 150, 690);
    }

    public void dispose() {
        hpBar.dispose();
        manaBar.dispose();
        roundFont.dispose();
    }
}
