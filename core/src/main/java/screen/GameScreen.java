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
import ui.CameraPreviewOverlay;
import ui.GameHUD;
import util.Constants;
import system.GameStateManager;
import system.RoundSystem;
import java.util.HashMap;
import java.util.Map;

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

        boolean waitForRoundTransition = roundSystem.isRoundEnded() && !roundSystem.isTransitionReady();
        if (!waitForRoundTransition) {
            if (p1Controller != null) p1Controller.update(delta);
            if (p2Controller != null) p2Controller.update(delta);

            if (showSkillCutsceneIfNeeded()) {
                return;
            }

            p1.update(delta);
            p2.update(delta);
            combatSystem.update(p1, p2);
        }
        roundSystem.update(delta, p1, p2);

        if (roundSystem.isRoundEnded() && !roundSystem.isMatchEnded()) {
            if (roundSystem.isTransitionReady()) {
                p1.reset();
                p2.reset();
                roundSystem.nextRound();
            }
        }

        gameStateManager.update(p1, p2, roundSystem);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        p1.draw(game.batch);
        p2.draw(game.batch);
        effectManager.draw(game.batch, delta);
        hud.render(game.batch, p1, p2, roundSystem);
        previewOverlay.update();
        previewOverlay.draw(game.batch);
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
            game.setScreen(new SkillCutsceneScreen(game, this, "P1"));
            return true;
        }
        if (p2.consumeSkillCutsceneTrigger()) {
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
            com.badlogic.gdx.Input.Keys.D,
            com.badlogic.gdx.Input.Keys.A,
            com.badlogic.gdx.Input.Keys.S,
            com.badlogic.gdx.Input.Keys.W,
            com.badlogic.gdx.Input.Keys.SPACE
        );
        p1Controller = new P1Controller(p1, p1Input);
        KeyboardInput p2Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.RIGHT,
            com.badlogic.gdx.Input.Keys.LEFT,
            com.badlogic.gdx.Input.Keys.DOWN,
            com.badlogic.gdx.Input.Keys.UP,
            com.badlogic.gdx.Input.Keys.ENTER
        );
        p2Controller = new P2Controller(p2, p2Input);
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
        previewOverlay.stop();
        previewOverlay.dispose();
    }
}
