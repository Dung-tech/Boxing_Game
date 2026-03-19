package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import screen.GameScreen;

public class Main extends Game {
    public SpriteBatch batch; // PHẢI LÀ PUBLIC

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
