package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import controller.P1Controller;
import controller.P2Controller;
import effect.EffectManager;
import input.CameraPreviewReceiver;
import input.GestureReceiver;
import input.KeyboardInput;
import main.Main;
import entity.Fighter;
import system.CombatSystem;
import sound.SoundManager;
import ui.CameraPreviewOverlay;
import ui.GameHUD;
import util.Constants;
import system.GameStateManager;
import system.RoundSystem;
import java.util.HashMap;
import java.util.Map;

// Main fight screen handling gameplay loop, HUD, and round flow.
public class GameScreen extends ScreenAdapter {
    private CombatSystem combatSystem;
    private GameHUD hud;
    private final Main game;
    private Texture background;
    private Fighter p1, p2;
    private EffectManager effectManager;
    private P1Controller p1Controller;
    private P2Controller p2Controller;
    private String controlMode;
    private ShapeRenderer overlayRenderer;
    private BitmapFont scoreboardFont;
    private BitmapFont scoreboardSmallFont;
    private GlyphLayout scoreboardLayout;
    private Map<String, Texture> scoreboardTextures;
    private final CameraPreviewOverlay previewOverlay =
        new CameraPreviewOverlay(CameraPreviewReceiver.getInstance());

    private RoundSystem roundSystem;
    private GameStateManager gameStateManager;
    private Texture[] countdownTextures;
    private float countdownTimer = 0f;
    private boolean countdownActive = false;
    private boolean countdownPlayed = false;
    private int countdownIndex = 0;
    private float countdownStepTimer = 0f;
    private static final float COUNTDOWN_STEP_SECONDS = 1.0f;
    private static final float COUNTDOWN_FIRST_HOLD_SECONDS = 1.0f;
    private boolean restMusicActive = false;
    private boolean matchEndSilenced = false;

    public GameScreen(Main game, String mode) {
        this.game = game;
        this.controlMode = mode;
        background = new Texture("images/background/background.jpg");
        effectManager = new EffectManager();
        effectManager.load();
        hud = new GameHUD();
        combatSystem = new CombatSystem(effectManager, game.soundManager);
        p1 = new Fighter(Constants.Side.LEFT, "images/p1",mode);
        p2 = new Fighter(Constants.Side.RIGHT, "images/p2", mode);
        roundSystem = new RoundSystem();
        gameStateManager = new GameStateManager(game,mode);
        overlayRenderer = new ShapeRenderer();
        scoreboardFont = new BitmapFont();
        scoreboardFont.getData().setScale(2.2f);
        scoreboardSmallFont = new BitmapFont();
        scoreboardSmallFont.getData().setScale(1.3f);
        scoreboardLayout = new GlyphLayout();
        scoreboardTextures = new HashMap<>();
        loadScoreboardTextures();
        loadCountdownTextures();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
            return;
        }

        // KIỂM TRA CUTSCENE PHẢI Ở ĐẦU TIÊN - trước mọi update
        if (showSkillCutsceneIfNeeded()) {
            return;
        }

        updateCountdown(delta);

        boolean waitForRoundTransition = roundSystem.isRoundEnded() && !roundSystem.isTransitionReady();
        boolean gameplayBlocked = countdownActive;
        if (!gameplayBlocked && !waitForRoundTransition) {
            if (p1Controller != null) p1Controller.update(delta);
            if (p2Controller != null) p2Controller.update(delta);

            if (showSkillCutsceneIfNeeded()) {
                return;
            }

            p1.update(delta);
            p2.update(delta);

            // Chặn không cho 2 võ sĩ đi xuyên qua nhau (P1 bên trái, P2 bên phải)
            float minDistance = 150f; // Khoảng cách tối thiểu giữa 2 võ sĩ
            if (p1.getX() + minDistance > p2.getX()) {
                float midpoint = (p1.getX() + p2.getX()) / 2.0f;
                p1.setX(midpoint - minDistance / 2.0f);
                p2.setX(midpoint + minDistance / 2.0f);
            }

            // Đảm bảo giới hạn biên màn hình lại một lần nữa
            if (p1.getX() < 0) p1.setX(0);
            if (p2.getX() > Constants.APP_WIDTH - p2.getWidth()) p2.setX(Constants.APP_WIDTH - p2.getWidth());

            combatSystem.update(p1, p2);
        }
        if (!gameplayBlocked) {
            roundSystem.update(delta, p1, p2);

            if (roundSystem.isRoundEnded() && !roundSystem.isMatchEnded()) {
                if (roundSystem.isTransitionReady()) {
                    p1.reset();
                    p2.reset();
                    roundSystem.nextRound();
                }
            }

            updateRestMusicState();
            gameStateManager.update(p1, p2, roundSystem);
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        p1.draw(game.batch);
        p2.draw(game.batch);
        effectManager.draw(game.batch, delta);
        hud.render(game.batch, p1, p2, roundSystem);
        previewOverlay.update();
        previewOverlay.draw(game.batch);
        renderCountdown();
        game.batch.end();

        if (roundSystem.isRoundEnded() && !roundSystem.isTransitionReady()) {
            renderRoundScoreboard();
        }
    }

    private void loadScoreboardTextures() {
        addScoreboardTexture("0-1", "scoreboard/0-1.png");
        addScoreboardTexture("0-2", "scoreboard/0-2.png");
        addScoreboardTexture("1-0", "scoreboard/1-0.png");
        addScoreboardTexture("1-1-P1", "scoreboard/1-1ronaldoWin.png");
        addScoreboardTexture("1-1-P2", "scoreboard/1-1messiWin.png");
        addScoreboardTexture("1-2", "scoreboard/1-2.png");
        addScoreboardTexture("2-0", "scoreboard/2-0.png");
        addScoreboardTexture("2-1", "scoreboard/2-1.png");
    }

    private void loadCountdownTextures() {
        countdownTextures = new Texture[4];
        countdownTextures[0] = loadCountdownTexture("images/countDown/3.png", null);
        countdownTextures[1] = loadCountdownTexture("images/countDown/2.png", null);
        countdownTextures[2] = loadCountdownTexture("images/countDown/1.png", null);
        countdownTextures[3] = loadCountdownTexture("images/countDown/fight.png", "images/countDown/figth.png");
    }

    private Texture loadCountdownTexture(String primary, String fallback) {
        FileHandle file = Gdx.files.internal(primary);
        if (!file.exists() && fallback != null) {
            file = Gdx.files.internal(fallback);
        }
        return file.exists() ? new Texture(file) : null;
    }

    private void updateCountdown(float delta) {
        if (!countdownActive) return;
        countdownTimer += delta;
        countdownStepTimer += delta;

        while (countdownStepTimer >= COUNTDOWN_STEP_SECONDS) {
            countdownStepTimer -= COUNTDOWN_STEP_SECONDS;
            int nextIndex = countdownIndex + 1;
            if (nextIndex < countdownTextures.length) {
                countdownIndex = nextIndex;
                playCountdownTickIfNumber(countdownIndex);
            } else {
                countdownIndex = countdownTextures.length - 1;
                countdownActive = false;
                break;
            }
        }
    }

    private void startCountdown() {
        if (countdownPlayed) return;
        countdownPlayed = true;
        countdownTimer = 0f;
        countdownStepTimer = -COUNTDOWN_FIRST_HOLD_SECONDS;
        countdownIndex = 0;
        countdownActive = countdownTextures != null && countdownTextures.length > 0;
        if (countdownActive) {
            playCountdownTickIfNumber(countdownIndex);
        }
    }

    private void playCountdownTickIfNumber(int index) {
        if (game.soundManager == null) return;
        if (index >= 0 && index <= 2) {
            game.soundManager.playCountDown();
        }
    }

    private Texture getCountdownTexture() {
        if (!countdownActive || countdownTextures == null || countdownTextures.length == 0) return null;
        if (countdownIndex < 0) countdownIndex = 0;
        if (countdownIndex >= countdownTextures.length) countdownIndex = countdownTextures.length - 1;
        return countdownTextures[countdownIndex];
    }

    private void renderCountdown() {
        Texture texture = getCountdownTexture();
        if (texture == null) return;
        float maxHeight = Constants.APP_HEIGHT * 0.45f;
        float maxWidth = Constants.APP_WIDTH * 0.6f;
        float scale = Math.min(
            maxWidth / (float) texture.getWidth(),
            maxHeight / (float) texture.getHeight()
        );
        float drawW = texture.getWidth() * scale;
        float drawH = texture.getHeight() * scale;
        float x = (Constants.APP_WIDTH - drawW) / 2f;
        float y = (Constants.APP_HEIGHT - drawH) / 2f;
        game.batch.draw(texture, x, y, drawW, drawH);
    }

    private void updateRestMusicState() {
        if (game.soundManager == null) return;
        boolean roundEnded = roundSystem.isRoundEnded();
        boolean matchEnded = roundSystem.isMatchEnded();

        if (roundEnded && matchEnded) {
            if (!matchEndSilenced) {
                game.soundManager.transitionToMusicState(SoundManager.MusicState.NONE);
                matchEndSilenced = true;
            }
            restMusicActive = false;
            return;
        }

        matchEndSilenced = false;
        if (roundEnded) {
            if (!restMusicActive) {
                game.soundManager.playRestMusic();
                restMusicActive = true;
            }
        } else if (restMusicActive) {
            game.soundManager.playMusic();
            restMusicActive = false;
        }
    }

    private void addScoreboardTexture(String key, String path) {
        FileHandle file = Gdx.files.internal(path);
        if (file.exists()) {
            scoreboardTextures.put(key, new Texture(file));
        }
    }

    private void renderRoundScoreboard() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        overlayRenderer.setProjectionMatrix(game.batch.getProjectionMatrix());
        overlayRenderer.begin(ShapeRenderer.ShapeType.Filled);
        overlayRenderer.setColor(0, 0, 0, 0.75f);
        overlayRenderer.rect(0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        overlayRenderer.end();

        Texture scoreboardTexture = getScoreboardTexture();
        game.batch.begin();
        if (scoreboardTexture != null) {
            float scale = Math.min(
                Constants.APP_WIDTH / (float) scoreboardTexture.getWidth(),
                Constants.APP_HEIGHT / (float) scoreboardTexture.getHeight()
            );
            float drawW = scoreboardTexture.getWidth() * scale;
            float drawH = scoreboardTexture.getHeight() * scale;
            float x = (Constants.APP_WIDTH - drawW) / 2f;
            float y = (Constants.APP_HEIGHT - drawH) / 2f;
            game.batch.draw(scoreboardTexture, x, y, drawW, drawH);
        } else {
            String resultText;
            RoundSystem.RoundResult result = roundSystem.getLastRoundResult();
            if (result == RoundSystem.RoundResult.P1) {
                resultText = "P1 WINS ROUND";
            } else if (result == RoundSystem.RoundResult.P2) {
                resultText = "P2 WINS ROUND";
            } else {
                resultText = "DRAW ROUND";
            }

            String roundText = "ROUND " + roundSystem.getCurrentRound() + " RESULT";
            String scoreText = "SCORE: P1 " + roundSystem.getP1RoundWins() + " - " +
                roundSystem.getP2RoundWins() + " P2";

            drawCentered(scoreboardFont, roundText, 520, Color.GOLD);
            drawCentered(scoreboardFont, resultText, 440, Color.WHITE);
            drawCentered(scoreboardSmallFont, scoreText, 360, Color.LIGHT_GRAY);
        }
        game.batch.end();
    }

    private Texture getScoreboardTexture() {
        int p1Wins = roundSystem.getP1RoundWins();
        int p2Wins = roundSystem.getP2RoundWins();
        if (p1Wins == 1 && p2Wins == 1) {
            RoundSystem.RoundResult result = roundSystem.getLastRoundResult();
            if (result == RoundSystem.RoundResult.P1) {
                Texture tex = scoreboardTextures.get("1-1-P1");
                if (tex != null) return tex;
            } else if (result == RoundSystem.RoundResult.P2) {
                Texture tex = scoreboardTextures.get("1-1-P2");
                if (tex != null) return tex;
            }
        }
        return scoreboardTextures.get(p1Wins + "-" + p2Wins);
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        scoreboardLayout.setText(font, text);
        font.setColor(color);
        float x = (Constants.APP_WIDTH - scoreboardLayout.width) / 2f;
        font.draw(game.batch, text, x, y);
    }

    private boolean showSkillCutsceneIfNeeded() {
        if (p1.consumeSkillCutsceneTrigger()) {
            if (game.soundManager != null) {
                game.soundManager.playSiuu();
            }
            game.setScreen(new SkillCutsceneScreen(game, this, "P1"));
            return true;
        }
        if (p2.consumeSkillCutsceneTrigger()) {
            if (game.soundManager != null) {
                game.soundManager.playAnkaraMessi();
            }
            game.setScreen(new SkillCutsceneScreen(game, this, "P2"));
            return true;
        }
        return false;
    }
    @Override
    public void show() {
        GestureReceiver.getInstance().start();
        if (controlMode != null && controlMode.startsWith("CAMERA")) {
            previewOverlay.start();
        }
        game.soundManager.playMusic();
        KeyboardInput p1Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.H,      // Đấm: H
            -1,                                 // Đấm phụ
            com.badlogic.gdx.Input.Keys.J,      // Đá: J
            -1,                                 // Đá phụ
            com.badlogic.gdx.Input.Keys.S,      // Cúi: S (phím xuống trong cụm WASD)
            -1,                                 // Cúi phụ
            com.badlogic.gdx.Input.Keys.W,      // Đỡ: W (phím lên trong cụm WASD)
            -1,                                 // Đỡ phụ
            com.badlogic.gdx.Input.Keys.K,      // Tuyệt chiêu: K
            com.badlogic.gdx.Input.Keys.L,      // Tuyệt chiêu phụ: L
            com.badlogic.gdx.Input.Keys.A,      // Di chuyển trái: A
            com.badlogic.gdx.Input.Keys.D       // Di chuyển phải: D
        );
        p1Controller = new P1Controller(p1, p1Input);
        KeyboardInput p2Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.NUMPAD_1, // Đấm chính: Numpad 1
            com.badlogic.gdx.Input.Keys.NUM_1,    // Đấm phụ: Phím số 1
            com.badlogic.gdx.Input.Keys.NUMPAD_2, // Đá chính: Numpad 2
            com.badlogic.gdx.Input.Keys.NUM_2,    // Đá phụ: Phím số 2
            com.badlogic.gdx.Input.Keys.DOWN,     // Cúi chính: Phím mũi tên Xuống
            com.badlogic.gdx.Input.Keys.NUM_3,    // Cúi phụ: Phím số 3
            com.badlogic.gdx.Input.Keys.UP,       // Đỡ chính: Phím mũi tên Lên
            com.badlogic.gdx.Input.Keys.NUM_4,    // Đỡ phụ: Phím số 4
            com.badlogic.gdx.Input.Keys.NUMPAD_5, // Tuyệt chiêu chính: Numpad 5
            com.badlogic.gdx.Input.Keys.NUM_5,    // Tuyệt chiêu phụ: Phím số 5
            com.badlogic.gdx.Input.Keys.LEFT,     // Di chuyển trái: Phím mũi tên Trái
            com.badlogic.gdx.Input.Keys.RIGHT     // Di chuyển phải: Phím mũi tên Phải
        );
        p2Controller = new P2Controller(p2, p2Input);
        startCountdown();
    }

    @Override
    public void hide() {
        previewOverlay.stop();
    }

    @Override
    public void dispose() {
        background.dispose();
        p1.dispose();
        p2.dispose();
        effectManager.dispose();
        hud.dispose();
        if (overlayRenderer != null) overlayRenderer.dispose();
        if (scoreboardFont != null) scoreboardFont.dispose();
        if (scoreboardSmallFont != null) scoreboardSmallFont.dispose();
        if (scoreboardTextures != null) {
            for (Texture tex : scoreboardTextures.values()) {
                if (tex != null) tex.dispose();
            }
        }
        if (countdownTextures != null) {
            for (Texture tex : countdownTextures) {
                if (tex != null) tex.dispose();
            }
        }
        previewOverlay.stop();
        previewOverlay.dispose();
    }
}
