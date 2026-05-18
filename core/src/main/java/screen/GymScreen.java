package screen;

import com.badlogic.gdx.ScreenAdapter;
import input.CameraPreviewReceiver;
import input.GymGestureReceiver;
import main.Main;
import ui.CameraPreviewOverlay;
import ui.GymAssets;
import ui.GymRenderer;
import system.GymSession;
import util.AIControllerLauncher;
import util.CameraRuntimeManager;

public class GymScreen extends ScreenAdapter {
    private final Main game;
    private final CameraPreviewOverlay previewOverlay =
        new CameraPreviewOverlay(CameraPreviewReceiver.getInstance());
    private GymAssets assets;
    private GymRenderer renderer;
    private GymSession session;

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
    }

    @Override
    public void hide() {
        previewOverlay.stop();
    }

    @Override
    public void render(float delta) {
        session.update(delta, () -> game.setScreen(new MenuGame(game)));
        renderer.render(game.batch, assets, session.getState(), session.getGymer(), previewOverlay);
    }


    @Override
    public void dispose() {
        CameraRuntimeManager.shutdownAll();
        if (assets != null) assets.dispose();
        if (renderer != null) renderer.dispose();
        previewOverlay.stop();
        previewOverlay.dispose();
    }
}
