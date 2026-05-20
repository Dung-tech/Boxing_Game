package input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GestureReceiver implements InputController, Runnable {
    private static GestureReceiver instance;

    // Tạo 2 "ngăn chứa" riêng cho 2 người chơi
    private String p1Action = "NONE";
    private String p2Action = "NONE";

    private boolean running = true;
    private Thread thread;

    private final String HOST = "127.0.0.1";
    private final int PORT = 65432;

    private GestureReceiver() {}

    public static GestureReceiver getInstance() {
        if (instance == null) {
            instance = new GestureReceiver();
        }
        return instance;
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            running = true;
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }


    @Override
    public void run() {
        System.out.println("[GestureReceiver] Dang doi ket noi tu Python AI (2 Player Mode)...");
        logAIControllerVisibility();

        while (running) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(HOST, PORT), 2000);
                socket.setSoTimeout(3000);
                System.out.println("[GestureReceiver] DA KET NOI VOI AI SERVER!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;

                while (running && (line = in.readLine()) != null) {
                    // line se co dang "P1:PUNCH" hoac "P2:KICK"
                    processIncomingData(line.trim().toUpperCase());
                }
            } catch (SocketTimeoutException e) {
                // No line arrived in timeout window; keep waiting on same connection.
            } catch (Exception e) {
                System.err.println("[GestureReceiver] Mat ket noi AI, thu ket noi lai: " + e.getClass().getSimpleName());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void logAIControllerVisibility() {
        try {
            Path runtimePath = Paths.get(GestureReceiver.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
            Path appDir = Files.isRegularFile(runtimePath) ? runtimePath.getParent() : runtimePath;
            Path aiExe = appDir.resolve("AI_Controller.exe");

            if (Files.exists(aiExe)) {
                System.out.println("[GestureReceiver] Tim thay AI_Controller.exe tai: " + aiExe);
            } else {
                System.out.println("[GestureReceiver] Khong tim thay AI_Controller.exe canh app: " + aiExe);
            }
        } catch (Exception e) {
            System.out.println("[GestureReceiver] Khong xac dinh duoc vi tri AI_Controller.exe: " + e.getMessage());
        }
    }

    /**
     * Logic tach chuoi de phan loai lenh cho tung Player
     */
    private void processIncomingData(String data) {
        if (!data.contains(":")) return;

        String[] parts = data.split(":");
        if (parts.length < 2) return;

        String playerTag = parts[0]; // "P1" hoac "P2"
        String action = parts[1];    // "PUNCH", "KICK",...

        synchronized (this) {
            if (playerTag.equals("P1")) {
                p1Action = action;
            } else if (playerTag.equals("P2")) {
                p2Action = action;
            }
        }
    }

    /**
     * Ham moi: Lay hanh dong theo ID cua tung nguoi choi
     */
    public synchronized String getActionForPlayer(int playerNum) {
        String action = (playerNum == 1) ? p1Action : p2Action;

        // CHỈ RESET nêú là chiêu thức (Signal) - Đấm/Đá/Skill
        if (action.equals("PUNCH") || action.equals("KICK") || action.equals("SKILL")) {
            if (playerNum == 1) p1Action = "NONE";
            else p2Action = "NONE";
        }

        // Nếu là BLOCK, DUCK hoặc IDLE, ta giữ nguyên để Fighter duy trì tư thế
        return action;
    }

    /**
     * Ham mac dinh tu Interface (Fallback cho P1 neu can)
     */
    @Override
    public synchronized String getNextAction() {
        return getActionForPlayer(1);
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    @Override
    public boolean kick() {
        return false;
    }

    @Override
    public boolean duck() {
        return false;
    }

    @Override
    public boolean punch() {
        return false;
    }

    @Override
    public boolean block() {
        return false;
    }

    @Override
    public boolean skill() {
        return false;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void reset() {

    }
}
