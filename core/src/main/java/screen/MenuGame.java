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
    private boolean isFightOptionsVisible = false; // Trạng thái hiện bảng chọn Mode
    private String[] fightOptions = {"KEYBOARD", "CAMERA AI", "CAMERA POSE"};
    private int fightSelected = 0; // Biến chọn riêng cho bảng này


    // 4 mục menu theo yêu cầu của bạn
    private String[] menuItems = {"FIGHT", "MANUAL", "SETTINGS", "QUIT GAME"};
    private int selected = 0;

    // Trạng thái hiển thị Overlay
    private boolean isManualVisible = false;
    private boolean isSettingsVisible = false;

    public MenuGame(Main game) {
        this.game = game;
        background = new Texture("images/background/background2.png");
        font = new BitmapFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        if(game.soundManager != null){
            game.soundManager.playMenuMusic();
        }
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
        else if (isFightOptionsVisible) drawFightOptionsOverlay();
    }

    private void handleInput() {
        // Nếu đang hiện Manual hoặc Settings, nhấn ESC để quay lại
        if (isManualVisible || isSettingsVisible || isFightOptionsVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                isManualVisible = isSettingsVisible = isFightOptionsVisible = false;
                return;
            }

            // Điều khiển trong bảng chọn Mode
            if (isFightOptionsVisible) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    fightSelected = (fightSelected - 1 + fightOptions.length) % fightOptions.length;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    fightSelected = (fightSelected + 1) % fightOptions.length;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    if (fightSelected == 1) {
                        startPythonAI("CAMERA_AI");
                        // [BẬT AI] Kích hoạt Socket trước khi vào game
                        input.GestureReceiver.getInstance().start();
                        game.setScreen(new GameScreen(game, "CAMERA_AI"));
                    } else if (fightSelected == 2) {
                        startPythonAI("CAMERA_POSE");
                        input.GestureReceiver.getInstance().start();
                        game.setScreen(new GameScreen(game, "CAMERA_POSE"));
                    } else {
                        game.setScreen(new GameScreen(game, "KEYBOARD"));
                    }
                }
            }
            return;
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
                    isFightOptionsVisible = true;
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
    private void drawFightOptionsOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(Constants.APP_WIDTH/2f - 230, 220, 460, 320);

        // Highlight mục được chọn trong bảng Mode
        shapeRenderer.setColor(Color.FIREBRICK);
        float highlightY = 440 - fightSelected * 70;
        shapeRenderer.rect(Constants.APP_WIDTH/2f - 200, highlightY, 400, 50);
        shapeRenderer.end();

        game.batch.begin();
        drawCenter(game.batch, "SELECT CONTROL MODE", Constants.APP_WIDTH, 520, Color.GOLD);
        drawCenter(game.batch, "KEYBOARD (Classic)", Constants.APP_WIDTH, 475, Color.WHITE);
        drawCenter(game.batch, "CAMERA AI (Finger)", Constants.APP_WIDTH, 405, Color.WHITE);
        drawCenter(game.batch, "CAMERA POSE (Upper Body)", Constants.APP_WIDTH, 335, Color.WHITE);
        drawCenter(game.batch, "Press ESC to Cancel", Constants.APP_WIDTH, 230, Color.GRAY);
        game.batch.end();
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
        drawCenter(game.batch, "P1: D, A to Punch/Kick; W,S to BLOCK, DUCK; SPACE to SKILL", Constants.APP_WIDTH, 450, Color.WHITE);
        drawCenter(game.batch, "P2: ->,<- to Punch/Kick; UP, DOWN to BLOCK, DUCK; ENTER to SKILL", Constants.APP_WIDTH, 380, Color.WHITE);
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
    private void startPythonAI(String aiMode) {
        Thread pythonThread = new Thread(() -> {
            try {
                // Đường dẫn đến file python trong môi trường ảo của ông giáo
                String pythonExe = "D:/Boxing_Game/.venv/Scripts/python.exe";
                // Đường dẫn đến file script chính
                String scriptPath = "D:/Boxing_Game/python_controller/main.py";

                ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath, aiMode);
                // Thiết lập thư mục làm việc để Python tìm đúng các file import (hand_detector,...)
                pb.directory(new java.io.File("D:/Boxing_Game/python_controller"));
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);

                Process process = pb.start();
                System.out.println("[System] Da tu dong kick-start Python AI!");

                // (Tùy chọn) Đọc log từ Python nếu ông giáo muốn debug ngay trong Console của Java
            /*
            java.util.Scanner s = new java.util.Scanner(process.getInputStream());
            while (s.hasNextLine()) System.out.println("Python: " + s.nextLine());
            */

            } catch (Exception e) {
                System.err.println("[Lỗi System] Khong the tu dong bat Python: " + e.getMessage());
            }
        });
        pythonThread.setDaemon(true);
        pythonThread.start();
    }


    @Override
    public void dispose() {
        background.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
