package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entity.Fighter;
import util.Constants;

public class GameHUD {
    private HealthBar hpBar;
    private ManaBar manaBar;

    public GameHUD() {
        hpBar = new HealthBar(Color.RED);
        manaBar = new ManaBar();
    }

    public void render(SpriteBatch batch, Fighter p1, Fighter p2) {
        hpBar.draw(batch, p1.getHp(), Constants.MAX_HP, 50, 650);
        manaBar.draw(batch, p1.getMana(), 50, 610);

        hpBar.draw(batch, p2.getHp(), Constants.MAX_HP, Constants.APP_WIDTH - 350, 650);
        manaBar.draw(batch, p2.getMana(), Constants.APP_WIDTH - 350, 610);
    }

    public void dispose() {
        hpBar.dispose();
        manaBar.dispose();
    }
}
