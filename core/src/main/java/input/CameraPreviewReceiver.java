package input;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class CameraPreviewReceiver implements Runnable {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65434;
    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final int MAX_FRAME_BYTES = 2_500_000;

    private static CameraPreviewReceiver instance;

    private volatile boolean running = true;
    private Thread thread;
    private final AtomicReference<byte[]> latestFrame = new AtomicReference<>();

    private CameraPreviewReceiver() {}

    public static synchronized CameraPreviewReceiver getInstance() {
        if (instance == null) {
            instance = new CameraPreviewReceiver();
        }
        return instance;
    }

    public synchronized void start() {
        if (thread == null || !thread.isAlive()) {
            running = true;
            thread = new Thread(this, "CameraPreviewReceiver");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        latestFrame.set(null);
    }

    public byte[] pollFrame() {
        return latestFrame.getAndSet(null);
    }

    @Override
    public void run() {
        while (running) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT_MS);
                socket.setSoTimeout(READ_TIMEOUT_MS);
                System.out.println("[CameraPreviewReceiver] Da ket noi preview stream.");

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                while (running) {
                    int length = in.readInt();
                    if (length <= 0 || length > MAX_FRAME_BYTES) {
                        throw new IllegalStateException("Frame size out of range: " + length);
                    }
                    byte[] buffer = new byte[length];
                    in.readFully(buffer);
                    latestFrame.set(buffer);
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("[CameraPreviewReceiver] Mat ket noi preview, thu lai: " + e.getClass().getSimpleName());
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}
