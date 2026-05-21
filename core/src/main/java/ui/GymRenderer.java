package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import entity.Gymer;
import system.GymState;
import util.Constants;

public class GymRenderer {
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final ShapeRenderer shapeRenderer;

    public GymRenderer() {
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, GymAssets assets, GymState state, Gymer gymer, CameraPreviewOverlay previewOverlay) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture centerTexture = state.isGameOver()
            ? assets.getRonalSiu()
            : (gymer.isConcentric() ? assets.getConcentric() : assets.getEccentric());
        float centerW = 400f;
        float centerH = 400f;
        if (state.isGameOver()) {
            centerW *= 1.5f;
            centerH *= 1.5f;
        }
        float centerX = (Constants.APP_WIDTH - centerW) / 2f;
        float centerY = (Constants.APP_HEIGHT - centerH) / 2f - 20f;

        float messiW = 220f * 2.2f;
        float messiH = 220f * 2.2f;
        float messiX = Constants.APP_WIDTH - messiW - 20f;
        float messiY = 20f;

        Texture messiTexture = state.isGameOver()
            ? assets.getMessiSad()
            : (state.isMessiEating() ? assets.getMessiEating() : assets.getMessiDrinking());

        batch.begin();
        batch.draw(assets.getBackgroundGym(), 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        batch.draw(messiTexture, messiX, messiY, messiW, messiH);
        batch.draw(centerTexture, centerX, centerY, centerW, centerH);

        drawCenterText(batch, "GYM MODE", Constants.APP_HEIGHT - 30, Color.GOLD);
        drawCenterText(batch, "Ronaldo Training: " + gymer.getStateLabel(), 65, Color.WHITE);
        drawCenterText(batch, "[ENTER/SPACE] KEYBOARD + CAMERA GYMPOSE | [ESC] MENU", 35, Color.LIGHT_GRAY);

        if (state.isGameOver()) {
            int selected = state.getGameOverSelected();
            drawCenterText(batch, "GYMER HET SUC!", 170, Color.SCARLET);
            drawCenterText(batch, selected == 0 ? "> CHOI TIEP <" : "CHOI TIEP", 130, selected == 0 ? Color.GOLD : Color.WHITE);
            drawCenterText(batch, selected == 1 ? "> THOAT RA MENU <" : "THOAT RA MENU", 95, selected == 1 ? Color.GOLD : Color.WHITE);
            drawCenterText(batch, "Nhan ESC de ve MENU", 60, Color.LIGHT_GRAY);
        }

        previewOverlay.update();
        previewOverlay.draw(batch);
        batch.end();

        drawHpBar(batch, gymer);
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }

    private void drawHpBar(SpriteBatch batch, Gymer gymer) {
        float x = 30f;
        float y = Constants.APP_HEIGHT - 70f;
        float width = 260f;
        float height = 20f;
        float hpRatio = (float) gymer.getHp() / gymer.getMaxHp();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, width * hpRatio, height);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "GYMER HP: " + gymer.getHp() + "/" + gymer.getMaxHp(), x, y - 8f);
        batch.end();
    }

    private void drawCenterText(SpriteBatch batch, String text, float y, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        float x = (Constants.APP_WIDTH - layout.width) / 2f;
        font.draw(batch, text, x, y);
    }
}
