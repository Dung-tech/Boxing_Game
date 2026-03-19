package boxinggame.input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Lead: Bảo
 * Module: GestureReceiver - Nhận lệnh từ Python AI qua Socket.
 * Cách dùng: 
 * 1. Gọi GestureReceiver.getInstance().start() khi bắt đầu Game.
 * 2. Gọi GestureReceiver.getInstance().getGesture() trong vòng lặp update của PlayerController.
 */
public class GestureReceiver implements Runnable {
    private static GestureReceiver instance;
    private String lastGesture = "NONE";
    private boolean running = true;
    private Thread thread;

    // Cấu hình Socket
    private final String HOST = "127.0.0.1";
    private final int PORT = 5000;

    private GestureReceiver() {}

    public static GestureReceiver getInstance() {
        if (instance == null) {
            instance = new GestureReceiver();
        }
        return instance;
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setDaemon(true); // Tự động đóng khi Game tắt
            thread.start();
        }
    }

    @Override
    public void run() {
        System.out.println("[GestureReceiver] Thread started.");
        
        while (running) {
            try (Socket socket = new Socket()) {
                // Thử kết nối với timeout 2 giây
                socket.connect(new InetSocketAddress(HOST, PORT), 2000);
                System.out.println("[GestureReceiver] Connected to Python AI!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                
                while (running && (line = in.readLine()) != null) {
                    synchronized (this) {
                        lastGesture = line.trim().toUpperCase();
                        // Log để dev khác dễ debug
                        System.out.println("[GestureInput] Received: " + lastGesture);
                    }
                }
            } catch (Exception e) {
                // Khi Python chưa bật hoặc mất mạng, chờ 2s rồi thử lại
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Dev 3 gọi hàm này để lấy hành động mới nhất.
     * Sau khi lấy, hành động sẽ được reset về NONE để tránh lặp lại lệnh cũ.
     */
    public synchronized String getGesture() {
        String temp = lastGesture;
        lastGesture = "NONE"; 
        return temp;
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }
}
