package entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import util.Constants;

public class Fighter {
    private Constants.Side side;
    private Texture texture;
    private float x, y;

    public Fighter(Constants.Side side, String imgPath) {
        this.side = side;
        this.texture = new Texture(imgPath);
        this.y = Constants.GROUND_Y; // 100f từ Constants

        // P1 bên trái (25% màn hình), P2 bên phải (75% màn hình)
        if (side == Constants.Side.LEFT) {
            this.x = Constants.APP_WIDTH * 0.25f - Constants.CHAR_SIZE / 2;
        } else {
            this.x = Constants.APP_WIDTH * 0.75f - Constants.CHAR_SIZE / 2;
        }
    }

    public void draw(SpriteBatch batch) {
        // Nếu là P2 (Bên PHẢI) thì lật ngang ảnh (flipX = true) để nhìn sang TRÁI
        boolean flipX = (side == Constants.Side.RIGHT);

        // Vẽ với đầy đủ tham số để dùng được tính năng lật ảnh
        batch.draw(texture,
            x, y,
            Constants.CHAR_SIZE, Constants.CHAR_SIZE, // 200x200
            0, 0,
            texture.getWidth(), texture.getHeight(),
            flipX, false);
    }

    public void dispose() {
        texture.dispose();
    }

    public void performAction(Constants.Action action) {
    }
}

