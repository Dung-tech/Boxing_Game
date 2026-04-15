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
    private final String playerSide;   // "P1" hoặc "P2"

    private Array<Texture> frames = new Array<>();
    private float frameTimer = 0f;
    private int currentFrame = 0;
    private final float FRAME_DURATION = 0.08f;   // ~12.5 fps
    private boolean finished = false;

    public SkillCutsceneScreen(Main game, ScreenAdapter returnScreen, String playerSide) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.playerSide = playerSide;
        loadFrames();
    }

    private void loadFrames() {
        String folder = playerSide.equals("P1") ? "videos/skill/p1" : "videos/skill/p2";

        for (int i = 1; i <= 200; i++) {   // tối đa 200 frame
            String path = folder + "/frame_" + String.format("%04d", i) + ".png";
            if (Gdx.files.internal(path).exists()) {
                frames.add(new Texture(Gdx.files.internal(path)));
            } else {
                break;   // hết frame thì dừng
            }
        }

        System.out.println("[SkillCutscene] Loaded " + frames.size + " frames for " + playerSide);
    }

    @Override
    public void render(float delta) {
        frameTimer += delta;

        if (frameTimer >= FRAME_DURATION && currentFrame < frames.size - 1) {
            frameTimer = 0;
            currentFrame++;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        if (currentFrame < frames.size) {
            Texture frame = frames.get(currentFrame);
            // Vẽ full màn hình, giữ tỷ lệ gốc
            float scale = Math.min(
                Constants.APP_WIDTH / (float) frame.getWidth(),
                Constants.APP_HEIGHT / (float) frame.getHeight()
            );
            float drawW = frame.getWidth() * scale;
            float drawH = frame.getHeight() * scale;
            float x = (Constants.APP_WIDTH - drawW) / 2f;
            float y = (Constants.APP_HEIGHT - drawH) / 2f;

            game.batch.draw(frame, x, y, drawW, drawH);
        }

        game.batch.end();

        // Nhấn bất kỳ phím nào để skip cutscene
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
        for (Texture t : frames) t.dispose();
        frames.clear();
    }
}
