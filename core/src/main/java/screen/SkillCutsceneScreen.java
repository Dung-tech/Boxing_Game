package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import main.Main;
import util.Constants;
import java.util.ArrayList;
import java.util.Collections;

// Plays the skill cutscene frames and then returns to gameplay.
public class SkillCutsceneScreen extends ScreenAdapter {

    private final Main game;
    private final ScreenAdapter returnScreen;
    private final String playerSide;

    private Array<Texture> frames = new Array<>();
    private Array<String> framePaths = new Array<>();
    private float frameTimer = 0f;
    private int currentFrame = 0;
    private static final float FRAME_DURATION = 0.1f;   // 10 fps - nhẹ và mượt hơn
    private static final int MAX_FRAMES_PER_TICK = 2;   // Hạn chế load mỗi frame để tránh khựng
    private int nextFrameToLoad = 0;
    private boolean videoFinished = false;
    private boolean finished = false;
    private boolean framesLoaded = false;
    private InputProcessor previousInputProcessor;
    private final InputAdapter inputBlocker = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            return true;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return true;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return true;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return true;
        }
    };

    public SkillCutsceneScreen(Main game, ScreenAdapter returnScreen, String playerSide) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.playerSide = playerSide;
    }

    @Override
    public void show() {
        previousInputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(inputBlocker);
        discoverFrames();
        framesLoaded = !framePaths.isEmpty();
        if (game.soundManager != null) {
            if ("P1".equals(playerSide)) {
                game.soundManager.playSiuu();
            } else if ("P2".equals(playerSide)) {
                game.soundManager.playAnkaraMessi();
            }
        }
    }

    private void discoverFrames() {
        String folder = playerSide.equals("P1") ? "videos/skill/p1" : "videos/skill/p2";

        System.out.println("[SkillCutscene] Đang load frame cho " + playerSide + " từ: " + folder);

        framePaths.clear();
        FileHandle dir = Gdx.files.internal(folder);
        if (!hasPngFrames(dir)) {
            System.out.println("[SkillCutscene] Không tìm thấy PNG trong classpath folder mặc định, thử tìm tại assets/...");
            dir = Gdx.files.internal("assets/" + folder);
        }
        if (!hasPngFrames(dir)) {
            System.out.println("[SkillCutscene] Thử tìm tại ../assets/...");
            dir = Gdx.files.internal("../assets/" + folder);
        }

        if (dir.exists() && dir.isDirectory()) {
            ArrayList<String> collected = new ArrayList<>();
            FileHandle[] files = dir.list();
            if (files != null) {
                for (FileHandle file : files) {
                    String name = file.name();
                    if (name.startsWith("frame_") && name.endsWith(".png")) {
                        collected.add(file.path());
                    }
                }
            }
            Collections.sort(collected);
            for (String path : collected) {
                framePaths.add(path);
            }
        }

        frames.clear();
        for (int idx = 0; idx < framePaths.size; idx++) {
            frames.add(null);
        }
        if (!framePaths.isEmpty()) {
            loadFrame(0);
            nextFrameToLoad = 1;
        }

        System.out.println("[SkillCutscene] Đã tìm thấy " + framePaths.size + " frame cho " + playerSide);
    }

    private boolean hasPngFrames(FileHandle dir) {
        if (!dir.exists() || !dir.isDirectory()) return false;
        try {
            FileHandle[] list = dir.list();
            if (list == null) return false;
            for (FileHandle f : list) {
                if (f.name().startsWith("frame_") && f.name().endsWith(".png")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi listing
        }
        return false;
    }

    private void loadFrame(int index) {
        if (index < 0 || index >= framePaths.size) return;
        if (frames.get(index) != null) return;
        Texture texture = new Texture(Gdx.files.internal(framePaths.get(index)));
        frames.set(index, texture);
    }

    private void prefetchFrames() {
        int loaded = 0;
        while (nextFrameToLoad < framePaths.size && loaded < MAX_FRAMES_PER_TICK) {
            loadFrame(nextFrameToLoad);
            nextFrameToLoad++;
            loaded++;
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToMenu();
            return;
        }

        if (!framesLoaded || framePaths.isEmpty()) {
            System.out.println("[SkillCutscene] Không tìm thấy khung hình cắt cảnh. Tự động quay lại game.");
            returnToGame();
            return;
        }

        prefetchFrames();

        if (!videoFinished) {
            frameTimer += delta;
            if (frameTimer >= FRAME_DURATION) {
                int nextFrame = currentFrame + 1;
                if (nextFrame < frames.size && frames.get(nextFrame) != null) {
                    frameTimer -= FRAME_DURATION;
                    currentFrame = nextFrame;
                } else {
                    frameTimer = 0f;
                }
            }

            if (currentFrame >= frames.size - 1 && frames.get(currentFrame) != null) {
                videoFinished = true;
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        Texture current = null;
        if (currentFrame < frames.size) {
            current = frames.get(currentFrame);
        }

        if (current == null) {
            game.batch.end();
            return;
        }
        float scale = Math.min(
            Constants.APP_WIDTH / (float) current.getWidth(),
            Constants.APP_HEIGHT / (float) current.getHeight()
        );
        float drawW = current.getWidth() * scale;
        float drawH = current.getHeight() * scale;
        float x = (Constants.APP_WIDTH - drawW) / 2f;
        float y = (Constants.APP_HEIGHT - drawH) / 2f;

        game.batch.draw(current, x, y, drawW, drawH);

        game.batch.end();

        if (videoFinished) {
            returnToGame();
        }
    }

    private void returnToGame() {
        if (finished) return;
        finished = true;
        stopCutsceneSounds();
        restoreInputProcessor();

        // Phát tiếng dính đòn (hit.mp3) sau khi kết thúc skill
        if (game.soundManager != null) {
            game.soundManager.playHit();
        }

        game.setScreen(returnScreen);
        dispose();
    }

    private void returnToMenu() {
        if (finished) return;
        finished = true;
        stopCutsceneSounds();
        restoreInputProcessor();
        game.setScreen(new MenuGame(game));
        dispose();
    }

    private void stopCutsceneSounds() {
        if (game.soundManager != null) {
            game.soundManager.stopSiuu();
            game.soundManager.stopAnkaraMessi();
        }
    }

    @Override
    public void dispose() {
        restoreInputProcessor();
        for (Texture t : frames) {
            if (t != null) t.dispose();
        }
        frames.clear();
        framePaths.clear();
    }

    private void restoreInputProcessor() {
        if (Gdx.input.getInputProcessor() == inputBlocker) {
            Gdx.input.setInputProcessor(previousInputProcessor);
        }
    }
}
