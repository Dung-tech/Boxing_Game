package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HealthBar {
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;
    private Color barColor;
    private float maxWidth = 300;
    private float height = 40;

    public HealthBar(Color color) {
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);
        this.layout = new GlyphLayout();
        this.barColor = color;
    }

    public void draw(SpriteBatch batch, float current, float max, float x, float y) {
        // 1. Vẽ hình khối (Phải kết thúc batch trước khi dùng ShapeRenderer)
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Vẽ Nền (Xám tối)
        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        shapeRenderer.rect(x, y, maxWidth, height);

        // Vẽ Thanh chỉ số (Máu/Mana)
        float ratio = current / max;
        shapeRenderer.setColor(barColor);
        shapeRenderer.rect(x, y, maxWidth * ratio, height);

        shapeRenderer.end();

        // Vẽ viền đen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, maxWidth, height);
        shapeRenderer.end();

        // 2. Vẽ Chữ (Bật lại batch)
        batch.begin();
        String text = (int)current + " / " + (int)max;
        layout.setText(font, text);
        // Căn giữa chữ vào thanh
        font.draw(batch, text, x + (maxWidth - layout.width) / 2, y + (height + layout.height) / 2);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
