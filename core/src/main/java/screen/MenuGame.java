package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.Main;
import ui.Manual;
import util.AIControllerLauncher;
import util.CameraRuntimeManager;
import util.Constants;

// Main menu screen with fight modes, gym, manual, and settings.
public class MenuGame extends ScreenAdapter {
    private final Main game;
    private Texture background;
    private Texture titleLogo;
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;
    private boolean isFightOptionsVisible = false; // Trạng thái hiện bảng chọn Mode
    private String[] fightOptions = {"KEYBOARD", "CAMERA AI", "CAMERA POSE"};
    private int fightSelected = 0; // Biến chọn riêng cho bảng này


    // 5 mục menu
    private String[] menuItems = {"FIGHT", "GYM", "MANUAL", "SETTINGS", "QUIT GAME"};
    private int selected = 0;

    // Trạng thái hiển thị Overlay
    private boolean isManualVisible = false;
    private boolean isSettingsVisible = false;
    private final Manual manualUI;
    private int settingsSelected = 0;
    private static final float VOLUME_STEP = 0.05f;

    public MenuGame(Main game) {
        this.game = game;
        background = new Texture("images/background/background2.png");
        titleLogo = new Texture("logo/Boxing_game.png");
        font = createReadableFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        manualUI = new Manual();
    }

    private BitmapFont createReadableFont() {
        String[] windowsFonts = {
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/segoeui.ttf"
        };

        for (String fontPath : windowsFonts) {
            try {
                if (java.nio.file.Files.exists(java.nio.file.Paths.get(fontPath))) {
                    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(fontPath));
                    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                    parameter.size = 20;
                    parameter.minFilter = Texture.TextureFilter.Linear;
                    parameter.magFilter = Texture.TextureFilter.Linear;
                    parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                        + "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠ"
                        + "àáâãèéêìíòóôõùúăđĩũơ"
                        + "ƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềể"
                        + "ỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừ"
                        + "ỬỮỰỲỴỶỸửữựỳỵỷỹ";
                    BitmapFont generated = generator.generateFont(parameter);
                    generator.dispose();
                    return generated;
                }
            } catch (Exception ignored) {
                // Fallback to default bitmap font if system TTF cannot be loaded.
            }
        }

        return new BitmapFont();
    }

    @Override
    public void show() {
        shutdownCameraRuntime();
        if(game.soundManager != null){
            game.soundManager.playMenuMusic();
        }
    }

    private void shutdownCameraRuntime() {
        CameraRuntimeManager.shutdownAll();
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

            if (isManualVisible) {
                manualUI.handleTabInput();
                return;
            }

            if (isSettingsVisible) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    settingsSelected = (settingsSelected - 1 + 2) % 2;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    settingsSelected = (settingsSelected + 1) % 2;
                }

                float delta = 0f;
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                    delta = -VOLUME_STEP;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    delta = VOLUME_STEP;
                }
                if (delta != 0f && game.soundManager != null) {
                    if (settingsSelected == 0) {
                        game.soundManager.setMusicVolume(game.soundManager.getMusicVolume() + delta);
                    } else {
                        game.soundManager.setSfxVolume(game.soundManager.getSfxVolume() + delta);
                    }
                }
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
                        input.GestureReceiver.getInstance().start();
                        AIControllerLauncher.launch("CAMERA_AI");
                        game.setScreen(new GameScreen(game, "CAMERA_AI"));
                    } else if (fightSelected == 2) {
                        input.GestureReceiver.getInstance().start();
                        AIControllerLauncher.launch("CAMERA_POSE");
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
                case 1: // GYM
                    game.setScreen(new GymScreen(game));
                    break;
                case 2: // MANUAL
                    manualUI.reset();
                    isManualVisible = true;
                    break;
                case 3: // SETTINGS
                    isSettingsVisible = true;
                    settingsSelected = 0;
                    break;
                case 4: // QUIT GAME
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

        float menuW = 320f;
        float menuH = 390f;
        float menuX = Constants.APP_WIDTH / 2f - menuW / 2f;
        float menuY = 140f;

        // Khung Menu chính
        shapeRenderer.setColor(new Color(30/255f, 30/255f, 40/255f, 1f));
        shapeRenderer.rect(menuX, menuY, menuW, menuH);

        // Highlight mục được chọn
        shapeRenderer.setColor(new Color(180/255f, 30/255f, 30/255f, 1f));
        float btnY = menuY + 305 - selected * 70;
        shapeRenderer.rect(menuX + 20, btnY - 5, 280, 50);

        shapeRenderer.end();

        game.batch.begin();
        float maxTitleWidth = 700f;
        float maxTitleHeight = 160f;
        float titleScale = Math.min(
            maxTitleWidth / titleLogo.getWidth(),
            maxTitleHeight / titleLogo.getHeight()
        ) * 2f;
        float titleW = titleLogo.getWidth() * titleScale;
        float titleH = titleLogo.getHeight() * titleScale;
        float titleX = (Constants.APP_WIDTH - titleW) / 2f;
        float titleY = Constants.APP_HEIGHT - titleH + 6f;
        game.batch.draw(titleLogo, titleX, titleY, titleW, titleH);

        font.getData().setScale(2f);
        for (int i = 0; i < menuItems.length; i++) {
            float textY = menuY + 340 - i * 70;
            drawCenter(game.batch, menuItems[i], Constants.APP_WIDTH, textY, Color.WHITE);
        }
        game.batch.end();
    }

    private void drawManualOverlay() {
        manualUI.draw(shapeRenderer, game.batch, font);
    }

    private void drawSettingsOverlay() {
        float panelX = 200;
        float panelY = 150;
        float panelW = Constants.APP_WIDTH - 400;
        float panelH = Constants.APP_HEIGHT - 300;

        float musicY = 440;
        float sfxY = 380;
        float highlightY = (settingsSelected == 0) ? musicY - 30 : sfxY - 30;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(20/255f, 20/255f, 30/255f, 0.95f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        shapeRenderer.setColor(new Color(180/255f, 30/255f, 30/255f, 1f));
        shapeRenderer.rect(panelX + 60, highlightY, panelW - 120, 40);
        shapeRenderer.end();

        int musicPct = 100;
        int sfxPct = 100;
        if (game.soundManager != null) {
            musicPct = Math.round(game.soundManager.getMusicVolume() * 100f);
            sfxPct = Math.round(game.soundManager.getSfxVolume() * 100f);
        }

        game.batch.begin();
        drawCenter(game.batch, "--- SETTINGS ---", Constants.APP_WIDTH, 520, Color.ORANGE);
        drawCenter(game.batch, "Music Volume: " + musicPct + "%", Constants.APP_WIDTH, musicY, Color.WHITE);
        drawCenter(game.batch, "SFX Volume: " + sfxPct + "%", Constants.APP_WIDTH, sfxY, Color.WHITE);
        drawCenter(game.batch, "Use LEFT/RIGHT to adjust | UP/DOWN to select | ESC to back",
            Constants.APP_WIDTH, 260, Color.GRAY);
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
        titleLogo.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
