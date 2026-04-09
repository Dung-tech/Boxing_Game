package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import main.Main;
import util.Constants;

public class GymScreen extends ScreenAdapter {
    private final Main game;
    private Texture backgroundGym;
    private Texture messiEating;
    private Texture messiDrinking;
    private Texture concentric;
    private Texture eccentric;
    private BitmapFont font;
    private GlyphLayout layout;

    private boolean concentricState = true;
    private float stateTimer = 0f;
    private static final float STATE_INTERVAL = 0.5f;

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
        concentric = new Texture("images/gym/Concentric.png");
        eccentric = new Texture("images/gym/Eccentric.png");

        font = new BitmapFont();
        font.getData().setScale(1.4f);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        handleInput();
        updateState(delta);
        updateMessiState(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture centerTexture = concentricState ? concentric : eccentric;
        float centerW = 400f;
        float centerH = 400f;
        float centerX = (Constants.APP_WIDTH - centerW) / 2f;
        float centerY = (Constants.APP_HEIGHT - centerH) / 2f - 20f;

        float messiW = 220f * 2.2f;
        float messiH = 220f * 2.2f;
        float messiX = Constants.APP_WIDTH - messiW - 20f;
        float messiY = 20f;

        Texture messiTexture = messiIsEating ? messiEating : messiDrinking;

        game.batch.begin();
        game.batch.draw(backgroundGym, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        game.batch.draw(messiTexture, messiX, messiY, messiW, messiH);
        game.batch.draw(centerTexture, centerX, centerY, centerW, centerH);

        drawCenterText("GYM MODE", Constants.APP_HEIGHT - 30, Color.GOLD);
        drawCenterText("Ronaldo Training: " + (concentricState ? "Concentric" : "Eccentric"), 65, Color.WHITE);
        drawCenterText("Press [ SPACE ] to switch | [ ESC ] to menu", 35, Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            concentricState = !concentricState;
            stateTimer = 0f;
        }
    }

    private void updateState(float delta) {
        stateTimer += delta;
        if (stateTimer >= STATE_INTERVAL) {
            stateTimer = 0f;
            concentricState = !concentricState;
        }
    }

    private void updateMessiState(float delta) {
        messiStateTimer += delta;
        if (messiStateTimer >= MESSI_STATE_INTERVAL) {
            messiStateTimer = 0f;
            messiIsEating = !messiIsEating;
        }
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
        if (concentric != null) concentric.dispose();
        if (eccentric != null) eccentric.dispose();
        if (font != null) font.dispose();
    }
}
