package input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GymGestureReceiver implements Runnable {
    private static GymGestureReceiver instance;

    private volatile boolean running = true;
    private Thread thread;
    private volatile String gymAction = "NONE";

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65433;

    private GymGestureReceiver() {}

    public static synchronized GymGestureReceiver getInstance() {
        if (instance == null) {
            instance = new GymGestureReceiver();
        }
        return instance;
    }

    public synchronized void start() {
        if (thread == null || !thread.isAlive()) {
            running = true;
            gymAction = "NONE";
            thread = new Thread(this, "GymGestureReceiver");
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void run() {
        System.out.println("[GymGestureReceiver] Dang doi ket noi gym pose...");

        while (running) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(HOST, PORT), 2000);
                socket.setSoTimeout(0);
                System.out.println("[GymGestureReceiver] DA KET NOI VOI GYM POSE SERVER!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while (running && (line = in.readLine()) != null) {
                    processIncomingData(line.trim().toUpperCase());
                }
            } catch (Exception e) {
                System.err.println("[GymGestureReceiver] Mat ket noi gym pose, thu lai: " + e.getClass().getSimpleName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void processIncomingData(String data) {
        if (!data.startsWith("GYM:")) return;
        String[] parts = data.split(":", 2);
        if (parts.length < 2) return;

        String action = parts[1];
        if ("CONCENTRIC".equals(action) || "ECCENTRIC".equals(action)) {
            gymAction = action;
        }
    }

    public String getGymAction() {
        return gymAction;
    }

    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        gymAction = "NONE";
    }
}

