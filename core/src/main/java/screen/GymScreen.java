package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import input.CameraPreviewReceiver;
import input.GymGestureReceiver;
import main.Main;
import ui.CameraPreviewOverlay;
import ui.GymAssets;
import ui.GymRenderer;
import system.GymSession;
import util.AIControllerLauncher;
import util.CameraRuntimeManager;
import util.Constants;

public class GymScreen extends ScreenAdapter {
    private static final float COUNTDOWN_TOTAL_SECONDS = 4f;
    private final Main game;
    private final CameraPreviewOverlay previewOverlay =
        new CameraPreviewOverlay(CameraPreviewReceiver.getInstance());
    private GymAssets assets;
    private GymRenderer renderer;
    private GymSession session;
    private boolean countdownActive = true;
    private float countdownTimer = 0f;
    private Texture countdown3;
    private Texture countdown2;
    private Texture countdown1;
    private Texture countdownGo;

    public GymScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        assets = new GymAssets();
        assets.load();
        renderer = new GymRenderer();
        session = new GymSession();

        GymGestureReceiver.getInstance().start();
        previewOverlay.start();
        AIControllerLauncher.launch("CAMERA_GYM_POSE");
        loadCountdownTextures();
        startCountdown();
    }

    @Override
    public void hide() {
        previewOverlay.stop();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
            return;
        }

        if (countdownActive) {
            updateCountdown(delta);
        } else {
            boolean prevGameOver = session.getState().isGameOver();
            session.update(delta, () -> game.setScreen(new MenuGame(game)));
            boolean nowGameOver = session.getState().isGameOver();
            if (prevGameOver && !nowGameOver) {
                startCountdown();
            }
        }

        renderer.render(game.batch, assets, session.getState(), session.getGymer(), previewOverlay);
        if (countdownActive) {
            drawCountdownOverlay();
        }
    }

    private void loadCountdownTextures() {
        if (countdown3 != null) return;
        countdown3 = loadCountdownTexture("images/countDown/3.png");
        countdown2 = loadCountdownTexture("images/countDown/2.png");
        countdown1 = loadCountdownTexture("images/countDown/1.png");
        countdownGo = loadCountdownTexture("images/countDown/go.png");
    }

    private Texture loadCountdownTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(path);
        }
        return null;
    }

    private void startCountdown() {
        if (countdown3 == null && countdown2 == null && countdown1 == null && countdownGo == null) {
            countdownActive = false;
            return;
        }
        countdownActive = true;
        countdownTimer = 0f;
        if (game.soundManager != null) {
            game.soundManager.playCountDownGym();
        }
    }

    private void updateCountdown(float delta) {
        countdownTimer += delta;
        if (countdownTimer >= COUNTDOWN_TOTAL_SECONDS) {
            countdownActive = false;
        }
    }

    private void drawCountdownOverlay() {
        Texture current = getCountdownTexture();
        if (current == null) return;
        float scale = Math.min(
            Constants.APP_WIDTH / (float) current.getWidth(),
            Constants.APP_HEIGHT / (float) current.getHeight()
        );
        float drawW = current.getWidth() * scale;
        float drawH = current.getHeight() * scale;
        float x = (Constants.APP_WIDTH - drawW) / 2f;
        float y = (Constants.APP_HEIGHT - drawH) / 2f;

        game.batch.begin();
        game.batch.draw(current, x, y, drawW, drawH);
        game.batch.end();
    }

    private Texture getCountdownTexture() {
        if (countdownTimer < 1f) return countdown3;
        if (countdownTimer < 2f) return countdown2;
        if (countdownTimer < 3f) return countdown1;
        return countdownGo;
    }

    @Override
    public void dispose() {
        CameraRuntimeManager.shutdownAll();
        if (assets != null) assets.dispose();
        if (renderer != null) renderer.dispose();
        if (countdown3 != null) countdown3.dispose();
        if (countdown2 != null) countdown2.dispose();
        if (countdown1 != null) countdown1.dispose();
        if (countdownGo != null) countdownGo.dispose();
        previewOverlay.stop();
        previewOverlay.dispose();
    }
}
