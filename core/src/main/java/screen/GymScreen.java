package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import controller.GymerController;
import entity.Gymer;
import main.Main;
import util.Constants;

public class GymScreen extends ScreenAdapter {
    private final Main game;
    private Texture backgroundGym;
    private Texture messiEating;
    private Texture messiDrinking;
    private Texture messiSad;
    private Texture concentric;
    private Texture eccentric;
    private Texture ronalSiu;
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    private Gymer gymer;
    private GymerController gymerController;
    private boolean isGameOver = false;
    private int gameOverSelected = 0;

    private boolean messiIsEating = true;
    private float messiStateTimer = 0f;
    private static final float MESSI_STATE_INTERVAL = 2f;

    public GymScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        backgroundGym = new Texture("images/background/bakgroundGym.jpg");
        messiEating = new Texture("images/gym/messiEating.png");
        messiDrinking = new Texture("images/gym/messiDrinking.png");
        messiSad = new Texture("images/gym/messiSad.png");
        concentric = new Texture("images/gym/Concentric.png");
        eccentric = new Texture("images/gym/Eccentric.png");
        ronalSiu = new Texture("images/gym/ronalSiu.png");

        font = new BitmapFont();
        font.getData().setScale(1.4f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();

        gymer = new Gymer();
        gymerController = new GymerController(gymer);
    }

    @Override
    public void render(float delta) {
        if (isGameOver) {
            handleGameOverInput();
        } else {
            handleGlobalInput();
            gymerController.update();
            updateMessiState(delta);
            if (gymer.isExhausted()) {
                isGameOver = true;
                gameOverSelected = 0;
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture centerTexture = isGameOver ? ronalSiu : (gymer.isConcentric() ? concentric : eccentric);
        float centerW = 400f;
        float centerH = 400f;
        float centerX = (Constants.APP_WIDTH - centerW) / 2f;
        float centerY = (Constants.APP_HEIGHT - centerH) / 2f - 20f;

        float messiW = 220f * 2.2f;
        float messiH = 220f * 2.2f;
        float messiX = Constants.APP_WIDTH - messiW - 20f;
        float messiY = 20f;

        Texture messiTexture = isGameOver ? messiSad : (messiIsEating ? messiEating : messiDrinking);

        game.batch.begin();
        game.batch.draw(backgroundGym, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        game.batch.draw(messiTexture, messiX, messiY, messiW, messiH);
        game.batch.draw(centerTexture, centerX, centerY, centerW, centerH);

        drawCenterText("GYM MODE", Constants.APP_HEIGHT - 30, Color.GOLD);
        drawCenterText("Ronaldo Training: " + gymer.getStateLabel(), 65, Color.WHITE);
        drawCenterText("[ ENTER ] CONCENTRIC | [ SPACE ] ECCENTRIC | [ ESC ] MENU", 35, Color.LIGHT_GRAY);

        if (isGameOver) {
            drawCenterText("GYMER HET SUC!", 170, Color.SCARLET);
            drawCenterText(gameOverSelected == 0 ? "> CHOI TIEP <" : "CHOI TIEP", 130, gameOverSelected == 0 ? Color.GOLD : Color.WHITE);
            drawCenterText(gameOverSelected == 1 ? "> THOAT RA MENU <" : "THOAT RA MENU", 95, gameOverSelected == 1 ? Color.GOLD : Color.WHITE);
            drawCenterText("Nhan ESC de ve MENU", 60, Color.LIGHT_GRAY);
        }
        game.batch.end();

        drawHpBar();
    }

    private void handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
        }
    }

    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            gameOverSelected = (gameOverSelected - 1 + 2) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            gameOverSelected = (gameOverSelected + 1) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (gameOverSelected == 0) {
                gymer.reset();
                messiIsEating = true;
                messiStateTimer = 0f;
                isGameOver = false;
            } else {
                game.setScreen(new MenuGame(game));
            }
        }
    }

    private void updateMessiState(float delta) {
        messiStateTimer += delta;
        if (messiStateTimer >= MESSI_STATE_INTERVAL) {
            messiStateTimer = 0f;
            messiIsEating = !messiIsEating;
        }
    }

    private void drawHpBar() {
        float x = 30f;
        float y = Constants.APP_HEIGHT - 70f;
        float width = 260f;
        float height = 20f;
        float hpRatio = (float) gymer.getHp() / gymer.getMaxHp();

        shapeRenderer.setProjectionMatrix(game.batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, width * hpRatio, height);
        shapeRenderer.end();

        game.batch.begin();
        font.setColor(Color.WHITE);
        font.draw(game.batch, "GYMER HP: " + gymer.getHp() + "/" + gymer.getMaxHp(), x, y - 8f);
        game.batch.end();
    }

    private void drawCenterText(String text, float y, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        float x = (Constants.APP_WIDTH - layout.width) / 2f;
        font.draw(game.batch, text, x, y);
    }

    @Override
    public void dispose() {
        if (backgroundGym != null) backgroundGym.dispose();
        if (messiEating != null) messiEating.dispose();
        if (messiDrinking != null) messiDrinking.dispose();
        if (messiSad != null) messiSad.dispose();
        if (concentric != null) concentric.dispose();
        if (eccentric != null) eccentric.dispose();
        if (ronalSiu != null) ronalSiu.dispose();
        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
