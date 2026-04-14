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
import controller.GymerController;
import entity.Gymer;
import input.GymGestureReceiver;
import main.Main;
import util.CameraRuntimeManager;
import util.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GymScreen extends ScreenAdapter {
    private final Main game;
    private Texture backgroundGym;
    private Texture messiEating;
    private Texture messiDrinking;
    private Texture messiSad;
    private Texture concentric;
    private Texture eccentric;
    private Texture ronalSiu;
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    private Gymer gymer;
    private GymerController gymerController;
    private boolean isGameOver = false;
    private int gameOverSelected = 0;

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
        messiSad = new Texture("images/gym/messiSad.png");
        concentric = new Texture("images/gym/Concentric.png");
        eccentric = new Texture("images/gym/Eccentric.png");
        ronalSiu = new Texture("images/gym/ronalSiu.png");

        font = new BitmapFont();
        font.getData().setScale(1.4f);
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();

        gymer = new Gymer();
        gymerController = new GymerController(gymer);

        GymGestureReceiver.getInstance().start();
        startPythonAI();
    }

    @Override
    public void render(float delta) {
        if (isGameOver) {
            handleGameOverInput();
        } else {
            handleGlobalInput();
            gymerController.update();
            updateMessiState(delta);
            if (gymer.isExhausted()) {
                isGameOver = true;
                gameOverSelected = 0;
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture centerTexture = isGameOver ? ronalSiu : (gymer.isConcentric() ? concentric : eccentric);
        float centerW = 400f;
        float centerH = 400f;
        float centerX = (Constants.APP_WIDTH - centerW) / 2f;
        float centerY = (Constants.APP_HEIGHT - centerH) / 2f - 20f;

        float messiW = 220f * 2.2f;
        float messiH = 220f * 2.2f;
        float messiX = Constants.APP_WIDTH - messiW - 20f;
        float messiY = 20f;

        Texture messiTexture = isGameOver ? messiSad : (messiIsEating ? messiEating : messiDrinking);

        game.batch.begin();
        game.batch.draw(backgroundGym, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        game.batch.draw(messiTexture, messiX, messiY, messiW, messiH);
        game.batch.draw(centerTexture, centerX, centerY, centerW, centerH);

        drawCenterText("GYM MODE", Constants.APP_HEIGHT - 30, Color.GOLD);
        drawCenterText("Ronaldo Training: " + gymer.getStateLabel(), 65, Color.WHITE);
        drawCenterText("[ENTER/SPACE] KEYBOARD + CAMERA GYMPOSE | [ESC] MENU", 35, Color.LIGHT_GRAY);

        if (isGameOver) {
            drawCenterText("GYMER HET SUC!", 170, Color.SCARLET);
            drawCenterText(gameOverSelected == 0 ? "> CHOI TIEP <" : "CHOI TIEP", 130, gameOverSelected == 0 ? Color.GOLD : Color.WHITE);
            drawCenterText(gameOverSelected == 1 ? "> THOAT RA MENU <" : "THOAT RA MENU", 95, gameOverSelected == 1 ? Color.GOLD : Color.WHITE);
            drawCenterText("Nhan ESC de ve MENU", 60, Color.LIGHT_GRAY);
        }
        game.batch.end();

        drawHpBar();
    }

    private void handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
        }
    }

    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuGame(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            gameOverSelected = (gameOverSelected - 1 + 2) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            gameOverSelected = (gameOverSelected + 1) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (gameOverSelected == 0) {
                gymer.reset();
                messiIsEating = true;
                messiStateTimer = 0f;
                isGameOver = false;
            } else {
                game.setScreen(new MenuGame(game));
            }
        }
    }

    private void updateMessiState(float delta) {
        messiStateTimer += delta;
        if (messiStateTimer >= MESSI_STATE_INTERVAL) {
            messiStateTimer = 0f;
            messiIsEating = !messiIsEating;
        }
    }

    private void drawHpBar() {
        float x = 30f;
        float y = Constants.APP_HEIGHT - 70f;
        float width = 260f;
        float height = 20f;
        float hpRatio = (float) gymer.getHp() / gymer.getMaxHp();

        shapeRenderer.setProjectionMatrix(game.batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, width * hpRatio, height);
        shapeRenderer.end();

        game.batch.begin();
        font.setColor(Color.WHITE);
        font.draw(game.batch, "GYMER HP: " + gymer.getHp() + "/" + gymer.getMaxHp(), x, y - 8f);
        game.batch.end();
    }

    private void drawCenterText(String text, float y, Color color) {
        layout.setText(font, text);
        font.setColor(color);
        float x = (Constants.APP_WIDTH - layout.width) / 2f;
        font.draw(game.batch, text, x, y);
    }

    private void startPythonAI() {
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
                    ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath.toString(), "CAMERA_GYM_POSE");
                    pb.directory(pythonControllerDir.toFile());
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);

                    try {
                        pb.start();
                    } catch (Exception firstError) {
                        ProcessBuilder fallbackPb = new ProcessBuilder("py", "-3", scriptPath.toString(), "CAMERA_GYM_POSE");
                        fallbackPb.directory(pythonControllerDir.toFile());
                        fallbackPb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        fallbackPb.redirectError(ProcessBuilder.Redirect.DISCARD);
                        fallbackPb.start();
                    }
                    System.out.println("[System] Da tu dong kick-start Python AI (GYM POSE)!");
                } else if (Files.exists(packagedExe)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExe.toString(), "CAMERA_GYM_POSE");
                    exePb.directory(appRoot.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe: " + packagedExe);
                } else if (Files.exists(packagedExeInDev)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExeInDev.toString(), "CAMERA_GYM_POSE");
                    exePb.directory(pythonControllerDir.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe (dev): " + packagedExeInDev);
                } else {
                    System.err.println("[Loi System] Khong tim thay AI_Controller.exe hoac Python script.");
                }
            } catch (Exception e) {
                System.err.println("[Loi System] Khong the tu dong bat Python: " + e.getMessage());
            }
        });
        pythonThread.setDaemon(true);
        pythonThread.start();
    }

    private Path resolveAppRoot() {
        Path runtimeBase = resolveRuntimeBaseDir();
        if (isAppRoot(runtimeBase)) {
            return runtimeBase;
        }

        Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
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
            Path codePath = Paths.get(GymScreen.class.getProtectionDomain().getCodeSource().getLocation().toURI())
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
        CameraRuntimeManager.shutdownAll();
        if (backgroundGym != null) backgroundGym.dispose();
        if (messiEating != null) messiEating.dispose();
        if (messiDrinking != null) messiDrinking.dispose();
        if (messiSad != null) messiSad.dispose();
        if (concentric != null) concentric.dispose();
        if (eccentric != null) eccentric.dispose();
        if (ronalSiu != null) ronalSiu.dispose();
        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
