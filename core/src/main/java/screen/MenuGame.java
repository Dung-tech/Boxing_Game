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
import util.CameraRuntimeManager;
import util.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MenuGame extends ScreenAdapter {
    private final Main game;
    private Texture background;
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

    public MenuGame(Main game) {
        this.game = game;
        background = new Texture("images/background/background2.png");
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
                        startPythonAI("CAMERA_AI");
                        game.setScreen(new GameScreen(game, "CAMERA_AI"));
                    } else if (fightSelected == 2) {
                        input.GestureReceiver.getInstance().start();
                        startPythonAI("CAMERA_POSE");
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

        float menuX = Constants.APP_WIDTH / 2f - 160;
        float menuY = 200;

        // Khung Menu chính
        shapeRenderer.setColor(new Color(30/255f, 30/255f, 40/255f, 1f));
        shapeRenderer.rect(menuX, menuY, 320, 390);

        // Highlight mục được chọn
        shapeRenderer.setColor(new Color(180/255f, 30/255f, 30/255f, 1f));
        float btnY = menuY + 305 - selected * 70;
        shapeRenderer.rect(menuX + 20, btnY - 5, 280, 50);

        shapeRenderer.end();

        game.batch.begin();
        font.getData().setScale(4f);
        drawCenter(game.batch, "BOXING GAME", Constants.APP_WIDTH, 650, Color.GOLD);

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
                Path appRoot = resolveAppRoot();
                Path packagedExe = appRoot.resolve("AI_Controller.exe");
                Path pythonControllerDir = appRoot.resolve("python_controller");
                Path packagedExeInDev = pythonControllerDir.resolve("dist").resolve("AI_Controller.exe");
                Path scriptPath = pythonControllerDir.resolve("core").resolve("main.py");

                // In dev, prioritize script to avoid stale bundled exe mismatches.
                if (Files.exists(scriptPath)) {
                    String pythonExe = resolvePythonExecutable(appRoot);
                    ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath.toString(), aiMode);
                    pb.directory(pythonControllerDir.toFile());
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);

                    try {
                        pb.start();
                    } catch (Exception firstError) {
                        ProcessBuilder fallbackPb = new ProcessBuilder("py", "-3", scriptPath.toString(), aiMode);
                        fallbackPb.directory(pythonControllerDir.toFile());
                        fallbackPb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        fallbackPb.redirectError(ProcessBuilder.Redirect.DISCARD);
                        fallbackPb.start();
                    }
                    System.out.println("[System] Da tu dong kick-start Python AI!");
                } else if (Files.exists(packagedExe)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExe.toString(), aiMode);
                    exePb.directory(appRoot.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe: " + packagedExe);
                } else if (Files.exists(packagedExeInDev)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExeInDev.toString(), aiMode);
                    exePb.directory(pythonControllerDir.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe (dev): " + packagedExeInDev);
                } else {
                    System.err.println("[Loi System] Khong tim thay AI_Controller.exe hoac Python script.");
                }

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

    private Path resolveAppRoot() {
        Path runtimeBase = resolveRuntimeBaseDir();
        if (runtimeBase != null && isAppRoot(runtimeBase)) {
            return runtimeBase;
        }

        Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        // user.dir may point to module folders (e.g. /core or /lwjgl3) when run from IDE/Gradle.
        for (int i = 0; i < 6 && dir != null; i++) {
            if (isAppRoot(dir)) {
                return dir;
            }
            dir = dir.getParent();
        }

        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private Path resolveRuntimeBaseDir() {
        try {
            Path codePath = Paths.get(MenuGame.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
            return Files.isRegularFile(codePath) ? codePath.getParent() : codePath;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isAppRoot(Path dir) {
        if (dir == null) return false;
        return Files.exists(dir.resolve("AI_Controller.exe"))
            || Files.exists(dir.resolve("python_controller").resolve("core").resolve("main.py"));
    }

    private String resolvePythonExecutable(Path appRoot) {
        String pythonFromEnv = System.getenv("PYTHON_EXE");
        if (pythonFromEnv != null && !pythonFromEnv.trim().isEmpty()) {
            return pythonFromEnv.trim();
        }

        Path venvPython = appRoot.resolve(".venv").resolve("Scripts").resolve("python.exe");
        if (Files.exists(venvPython)) {
            return venvPython.toString();
        }

        return "python";
    }


    @Override
    public void dispose() {
        background.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
