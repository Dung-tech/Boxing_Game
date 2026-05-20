package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AIControllerLauncher {
    private AIControllerLauncher() {
    }

    public static void launch(String aiMode) {
        Thread pythonThread = new Thread(() -> {
            try {
                Path appRoot = resolveAppRoot();
                Path packagedExe = appRoot.resolve("AI_Controller.exe");
                Path pythonControllerDir = appRoot.resolve("python_controller");
                Path packagedExeDirInDev = pythonControllerDir.resolve("dist").resolve("AI_Controller");
                Path packagedExeInDev = packagedExeDirInDev.resolve("AI_Controller.exe");
                Path packagedExeLegacyInDev = pythonControllerDir.resolve("dist").resolve("AI_Controller.exe");
                Path scriptPath = pythonControllerDir.resolve("core").resolve("main.py");

                // In dev, prioritize script to avoid stale bundled exe mismatches.
                if (Files.exists(scriptPath)) {
                    String pythonExe = resolvePythonExecutable(appRoot);
                    ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath.toString(), aiMode);
                    pb.directory(pythonControllerDir.toFile());
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    applyCameraPreviewEnv(pb);

                    try {
                        pb.start();
                    } catch (Exception firstError) {
                        ProcessBuilder fallbackPb = new ProcessBuilder("py", "-3", scriptPath.toString(), aiMode);
                        fallbackPb.directory(pythonControllerDir.toFile());
                        fallbackPb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        fallbackPb.redirectError(ProcessBuilder.Redirect.DISCARD);
                        applyCameraPreviewEnv(fallbackPb);
                        fallbackPb.start();
                    }
                    System.out.println(buildKickstartMessage(aiMode));
                } else if (Files.exists(packagedExe)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExe.toString(), aiMode);
                    exePb.directory(appRoot.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    applyCameraPreviewEnv(exePb);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe: " + packagedExe);
                } else if (Files.exists(packagedExeInDev)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExeInDev.toString(), aiMode);
                    exePb.directory(pythonControllerDir.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    applyCameraPreviewEnv(exePb);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe (dev): " + packagedExeInDev);
                } else if (Files.exists(packagedExeLegacyInDev)) {
                    ProcessBuilder exePb = new ProcessBuilder(packagedExeLegacyInDev.toString(), aiMode);
                    exePb.directory(pythonControllerDir.toFile());
                    exePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    exePb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    applyCameraPreviewEnv(exePb);
                    exePb.start();
                    System.out.println("[System] Da bat AI_Controller.exe (dev legacy): " + packagedExeLegacyInDev);
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

    private static String buildKickstartMessage(String aiMode) {
        if ("CAMERA_GYM_POSE".equals(aiMode)) {
            return "[System] Da tu dong kick-start Python AI (GYM POSE)!";
        }
        return "[System] Da tu dong kick-start Python AI!";
    }

    private static void applyCameraPreviewEnv(ProcessBuilder pb) {
        pb.environment().put("AI_PREVIEW_STREAM", "1");
        pb.environment().put("AI_PREVIEW_PORT", "65434");
        pb.environment().put("AI_PREVIEW_FPS", "12");
        pb.environment().put("AI_PREVIEW_WIDTH", "320");
        pb.environment().put("AI_PREVIEW_JPEG_QUALITY", "70");
        pb.environment().put("AI_PREVIEW_SHOW_WINDOW", "0");
    }

    private static Path resolveAppRoot() {
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

    private static Path resolveRuntimeBaseDir() {
        try {
            Path codePath = Paths.get(AIControllerLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
            return Files.isRegularFile(codePath) ? codePath.getParent() : codePath;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isAppRoot(Path dir) {
        if (dir == null) return false;
        return Files.exists(dir.resolve("AI_Controller.exe"))
            || Files.exists(dir.resolve("python_controller").resolve("core").resolve("main.py"));
    }

    private static String resolvePythonExecutable(Path appRoot) {
        String pythonFromEnv = System.getenv("PYTHON_EXE");
        if (pythonFromEnv != null && !pythonFromEnv.trim().isEmpty()) {
            return pythonFromEnv.trim();
        }

        Path venvPython = appRoot.resolve(".venv").resolve("Scripts").resolve("python.exe");
        if (Files.exists(venvPython)) {
            return venvPython.toString();
        }

        Path controllerVenvPython = appRoot.resolve("python_controller")
            .resolve(".venv")
            .resolve("Scripts")
            .resolve("python.exe");
        if (Files.exists(controllerVenvPython)) {
            return controllerVenvPython.toString();
        }

        return "python";
    }
}
