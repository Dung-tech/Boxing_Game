package screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import main.Main;
import util.Constants;

public class SkillCutsceneScreen extends ScreenAdapter {

    private final Main game;
    private final ScreenAdapter returnScreen;
    private final String playerSide;

    private Array<Texture> frames = new Array<>();
    private float frameTimer = 0f;
    private int currentFrame = 0;
    private final float FRAME_DURATION = 0.1f;   // 10 fps - nhẹ và mượt hơn
    private boolean finished = false;
    private boolean framesLoaded = false;

    public SkillCutsceneScreen(Main game, ScreenAdapter returnScreen, String playerSide) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.playerSide = playerSide;
    }

    @Override
    public void show() {
        loadFrames();
        framesLoaded = true;
    }

    private void loadFrames() {
        String folder = playerSide.equals("P1") ? "videos/skill/p1" : "videos/skill/p2";

        System.out.println("[SkillCutscene] Đang load frame cho " + playerSide + " từ: " + folder);

        int i = 1;
        while (true) {
            String path = folder + "/frame_" + String.format("%04d", i) + ".png";
            if (Gdx.files.internal(path).exists()) {
                frames.add(new Texture(Gdx.files.internal(path)));
                i++;
            } else {
                break;
            }
        }

        System.out.println("[SkillCutscene] Đã load xong " + frames.size + " frame cho " + playerSide);
    }

    @Override
    public void render(float delta) {
        if (!framesLoaded || frames.isEmpty()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            return;
        }

        frameTimer += delta;
        if (frameTimer >= FRAME_DURATION && currentFrame < frames.size - 1) {
            frameTimer = 0;
            currentFrame++;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        if (currentFrame < frames.size) {
            Texture current = frames.get(currentFrame);
            float scale = Math.min(
                Constants.APP_WIDTH / (float) current.getWidth(),
                Constants.APP_HEIGHT / (float) current.getHeight()
            );
            float drawW = current.getWidth() * scale;
            float drawH = current.getHeight() * scale;
            float x = (Constants.APP_WIDTH - drawW) / 2f;
            float y = (Constants.APP_HEIGHT - drawH) / 2f;

            game.batch.draw(current, x, y, drawW, drawH);
        }

        game.batch.end();

        // Nhấn phím bất kỳ để skip cutscene
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || currentFrame >= frames.size - 1) {
            returnToGame();
        }
    }

    private void returnToGame() {
        if (finished) return;
        finished = true;
        game.setScreen(returnScreen);
        dispose();
    }

    @Override
    public void dispose() {
        for (Texture t : frames) {
            if (t != null) t.dispose();
        }
        frames.clear();
    }
}
