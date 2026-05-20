package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import screen.GameScreen;
import screen.MenuGame;
import sound.SoundManager;
import util.CameraRuntimeManager;

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
        CameraRuntimeManager.shutdownAll();
        if (batch != null) batch.dispose();
        if (image != null) image.dispose();
        if (soundManager != null) soundManager.dispose();
    }
}
