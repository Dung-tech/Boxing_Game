package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import screen.GameScreen;
import screen.MenuGame;
import sound.SoundManager;
import util.CameraRuntimeManager;
import util.Constants;

// LibGDX entry point that wires up batch and sound.
public class Main extends Game {
    public SpriteBatch batch;
    public Texture image;
    public SoundManager soundManager;
    private Texture aiLabelTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();
        soundManager = new SoundManager();
        soundManager.load();
        aiLabelTexture = new Texture("images/AILabel.png");
        this.setScreen(new MenuGame(this));
    }

    @Override
    public void render() {
        super.render();
        if (batch == null || aiLabelTexture == null) return;
        float margin = 12f;
        float scale = 0.45f;
        float drawW = aiLabelTexture.getWidth() * scale;
        float drawH = aiLabelTexture.getHeight() * scale;
        float x = Constants.APP_WIDTH - drawW - margin;
        float y = margin;
        batch.begin();
        batch.draw(aiLabelTexture, x, y, drawW, drawH);
        batch.end();
    }

    @Override
    public void dispose() {
        CameraRuntimeManager.shutdownAll();
        if (batch != null) batch.dispose();
        if (image != null) image.dispose();
        if (soundManager != null) soundManager.dispose();
        if (aiLabelTexture != null) aiLabelTexture.dispose();
    }
}
