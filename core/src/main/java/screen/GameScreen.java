package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import main.Main;
import entity.Fighter;
import util.Constants;

public class GameScreen extends ScreenAdapter {
    private final Main game;
    private Texture background;
    private Fighter p1, p2;

    public GameScreen(Main game) {
        this.game = game;
        // Load lại cái nền xịn của ông giáo
        background = new Texture("images/background/background.jpg");

        // Khởi tạo 2 Ronaldo đối đầu
        p1 = new Fighter(Constants.Side.LEFT, "images/p1/idle.png");
        p2 = new Fighter(Constants.Side.RIGHT, "images/p2/idle.png");
    }

    @Override
    public void render(float delta) {
        // Xóa màn hình
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        // 1. Vẽ nền trước (Phủ kín màn hình 1280x720)
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);

        // 2. Vẽ 2 võ sĩ (Tự động đứng đúng chỗ và đối mặt nhau)
        p1.draw(game.batch);
        p2.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void dispose() {
        background.dispose();
        p1.dispose();
        p2.dispose();
    }
}
