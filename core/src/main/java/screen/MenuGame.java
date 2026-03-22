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
import main.Main;
import util.Constants;

public class MenuGame extends ScreenAdapter {
    private final Main game;
    private Texture background;
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    // 4 mục menu theo yêu cầu của bạn
    private String[] menuItems = {"FIGHT", "MANUAL", "SETTINGS", "QUIT GAME"};
    private int selected = 0;

    // Trạng thái hiển thị Overlay
    private boolean isManualVisible = false;
    private boolean isSettingsVisible = false;

    public MenuGame(Main game) {
        this.game = game;
        background = new Texture("images/background/background.jpg");
        font = new BitmapFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // 1. XỬ LÝ LOGIC ĐIỀU KHIỂN
        handleInput();

        // 2. XÓA MÀN HÌNH
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- VẼ BACKGROUND ---
        game.batch.begin();
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        game.batch.end();

        // --- VẼ KHUNG MENU VÀ NÚT ---
        drawMenuUI();

        // --- VẼ OVERLAY (MANUAL HOẶC SETTINGS) ---
        if (isManualVisible) {
            drawManualOverlay();
        } else if (isSettingsVisible) {
            drawSettingsOverlay();
        }
    }

    private void handleInput() {
        // Nếu đang hiện Manual hoặc Settings, nhấn ESC để quay lại
        if (isManualVisible || isSettingsVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                isManualVisible = false;
                isSettingsVisible = false;
            }
            return; // Khóa các phím điều khiển menu chính khi đang hiện overlay
        }

        // Điều khiển lên xuống
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selected = (selected - 1 + menuItems.length) % menuItems.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selected = (selected + 1) % menuItems.length;
        }

        // Xử lý khi nhấn ENTER
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (selected) {
                case 0: // FIGHT
                    game.setScreen(new GameScreen(game));
                    break;
                case 1: // MANUAL
                    isManualVisible = true;
                    break;
                case 2: // SETTINGS
                    isSettingsVisible = true;
                    break;
                case 3: // QUIT GAME
                    Gdx.app.exit();
                    break;
            }
        }
    }

    private void drawMenuUI() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(game.batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Overlay tối cho toàn màn hình
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);

        float menuX = Constants.APP_WIDTH / 2f - 160;
        float menuY = 200;

        // Khung Menu chính
        shapeRenderer.setColor(new Color(30/255f, 30/255f, 40/255f, 1f));
        shapeRenderer.rect(menuX, menuY, 320, 320);

        // Highlight mục được chọn (Màu đỏ giống bản Swing của bạn)
        shapeRenderer.setColor(new Color(180/255f, 30/255f, 30/255f, 1f));
        float btnY = menuY + 235 - selected * 70; // Căn chỉnh lại khoảng cách
        shapeRenderer.rect(menuX + 20, btnY - 5, 280, 50);

        shapeRenderer.end();

        // Vẽ chữ Menu
        game.batch.begin();
        font.getData().setScale(4f); // Title to hơn
        drawCenter(game.batch, "BOXING GAME", Constants.APP_WIDTH, 650, Color.GOLD);

        font.getData().setScale(2f); // Chữ menu nhỏ hơn
        for (int i = 0; i < menuItems.length; i++) {
            float textY = menuY + 270 - i * 70;
            drawCenter(game.batch, menuItems[i], Constants.APP_WIDTH, textY, Color.WHITE);
        }
        game.batch.end();
    }

    private void drawManualOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.9f); // Nền đen mờ đậm hơn
        shapeRenderer.rect(100, 100, Constants.APP_WIDTH - 200, Constants.APP_HEIGHT - 200);
        shapeRenderer.end();

        game.batch.begin();
        drawCenter(game.batch, "--- HOW TO PLAY ---", Constants.APP_WIDTH, 550, Color.CYAN);
        drawCenter(game.batch, "P1: W, A, S, D to Move | J, K to Punch/Kick", Constants.APP_WIDTH, 450, Color.WHITE);
        drawCenter(game.batch, "P2: Arrow Keys to Move | 1, 2 to Punch/Kick", Constants.APP_WIDTH, 380, Color.WHITE);
        drawCenter(game.batch, "Press ESC to go back", Constants.APP_WIDTH, 200, Color.GRAY);
        game.batch.end();
    }

    private void drawSettingsOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(20/255f, 20/255f, 30/255f, 0.95f);
        shapeRenderer.rect(200, 150, Constants.APP_WIDTH - 400, Constants.APP_HEIGHT - 300);
        shapeRenderer.end();

        game.batch.begin();
        drawCenter(game.batch, "--- SETTINGS ---", Constants.APP_WIDTH, 500, Color.ORANGE);
        drawCenter(game.batch, "Audio Volume: 100% (Coming Soon)", Constants.APP_WIDTH, 400, Color.WHITE);
        drawCenter(game.batch, "Press ESC to go back", Constants.APP_WIDTH, 250, Color.GRAY);
        game.batch.end();
    }

    private void drawCenter(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, String text, float width, float y, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        float x = (width - layout.width) / 2;
        font.draw(batch, text, x, y);
    }

    @Override
    public void dispose() {
        background.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
