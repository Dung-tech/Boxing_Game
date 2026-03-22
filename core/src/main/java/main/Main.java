package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import screen.GameScreen;
import screen.MenuGame;
import sound.SoundManager;

public class Main extends Game {
    public SpriteBatch batch;
    public Texture image;
    public SoundManager soundManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        soundManager = new SoundManager();
        soundManager.load();
        this.setScreen(new MenuGame(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        soundManager.dispose();
    }
}
