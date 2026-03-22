package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import util.Constants;

public class ManaBar {
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;

    private final float width = 300;
    private final float height = 30; // Thấp hơn thanh máu một chút cho đẹp

    public ManaBar() {
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f); // Font nhỏ hơn thanh máu
        this.layout = new GlyphLayout();
    }

    public void draw(SpriteBatch batch, float currentMana, float x, float y) {
        // 1. Tạm dừng vẽ Texture để vẽ hình khối (Shape)
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Vẽ Nền (Màu xám tối)
        shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.2f, 1f));
        shapeRenderer.rect(x, y, width, height);

        // Vẽ Mana (Màu xanh Cyan)
        float ratio = currentMana / Constants.MAX_MANA;
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, width * ratio, height);

        shapeRenderer.end();

        // Vẽ Viền đen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // 2. Tiếp tục vẽ Chữ (Bật lại batch)
        batch.begin();
        String text = (int)currentMana + " MANA";
        layout.setText(font, text);

        // Căn giữa chữ
        font.setColor(Color.WHITE);
        font.draw(batch, text, x + (width - layout.width) / 2, y + (height + layout.height) / 2);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
