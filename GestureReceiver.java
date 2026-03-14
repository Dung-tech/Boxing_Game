import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class GestureReceiver implements Runnable {
    private String lastGesture = "NONE";
    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try (Socket socket = new Socket("127.0.0.1", 5000);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                System.out.println("Java: Da ket noi den Python!");
                String line;
                while (running && (line = in.readLine()) != null) {
                    // Cập nhật lệnh mới nhất nhận được
                    lastGesture = line.trim();
                }
            } catch (Exception e) {
                System.out.println("Java: Dang doi Python Server (reconnect)...");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    // Hàm để Game lấy lệnh ra dùng
    public synchronized String getGesture() {
        String temp = lastGesture;
        lastGesture = "NONE"; // Lấy xong reset về NONE
        return temp;
    }
}
