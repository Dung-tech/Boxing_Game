package util;

import input.GestureReceiver;
import input.GymGestureReceiver;

public final class CameraRuntimeManager {
    private CameraRuntimeManager() {
    }

    public static void shutdownAll() {
        GestureReceiver.getInstance().stop();
        GymGestureReceiver.getInstance().stop();

        ProcessHandle.allProcesses()
            .filter(CameraRuntimeManager::isCameraRuntimeProcess)
            .forEach(process -> {
                try {
                    if (!process.destroy() || process.isAlive()) {
                        process.destroyForcibly();
                    }
                } catch (Exception ignored) {
                    // Ignore stale handles/race conditions while shutting down app.
                }
            });
    }

    private static boolean isCameraRuntimeProcess(ProcessHandle process) {
        String command = process.info().command().orElse("").toLowerCase();
        if (command.endsWith("ai_controller.exe")) {
            return true;
        }

        String cmdLine = process.info().commandLine().orElse("").toLowerCase();
        boolean isPython = command.endsWith("python.exe") || command.endsWith("py.exe");
        boolean isOurController =
            cmdLine.contains("boxing_game")
                && cmdLine.contains("python_controller")
                && cmdLine.contains("core")
                && cmdLine.contains("main.py");

        return isPython && isOurController;
    }
}

