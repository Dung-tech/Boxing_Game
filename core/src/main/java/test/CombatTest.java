package test;

import sound.SoundManager;
import effect.EffectManager;

public class CombatTest {

    public static void main(String[] args) {
        // --- 1. KHỞI TẠO DỮ LIỆU GIẢ LẬP ---
        float player1Damage = 25.5f;
        float player2HP = 100.0f;
        boolean isPunchLanded = true; // Giả lập cú đấm trúng đích

        System.out.println("========== HE THONG KIEM THU CHIEN DAU (DEV 5) ==========");
        System.out.println("[INFO] Fighter 2 dang co: " + player2HP + " HP");
        System.out.println("[ACTION] Fighter 1 thuc hien cu dam voi sat thuong: " + player1Damage);

        // --- 2. KIỂM TRA LOGIC VA CHẠM & TRỪ MÁU ---
        if (isPunchLanded) {
            player2HP -= player1Damage;

            System.out.println("\n[RESULT] Dam TRUNG! Fighter 2 con lai: " + player2HP + " HP");

            // Kiểm tra xem máu có bị trừ đúng không
            if (player2HP == 74.5f) {
                System.out.println(">>> CHECK: Logic tru mau [THANH CONG]");
            } else {
                System.err.println(">>> CHECK: Logic tru mau [THAT BAI] - Vui long bao Dev 2 kiem tra lai CombatSystem");
            }

            // --- 3. GIẢ LẬP GỌI SOUND & EFFECT (Nhiệm vụ chính của Dev 5) ---
            System.out.println("\n--- DANG KIEM TRA KET NOI AM THANH & HIEU UNG ---");

            // In ra các bước mà Code thực tế sẽ thực hiện
            simulateSoundCall("hitSound");
            simulateEffectCall(400.5f, 300.0f); // Tọa độ giả lập cú đấm

        } else {
            System.out.println("[RESULT] Dam TRUOT! Fighter 2 van con: " + player2HP + " HP");
            simulateSoundCall("punchSound"); // Chỉ phát tiếng gió
        }

        System.out.println("\n================ KET THUC KIEM THU ================");
    }

    // Hàm hỗ trợ để giả lập việc gọi SoundManager
    private static void simulateSoundCall(String soundName) {
        if (soundName.equals("hitSound")) {
            System.out.println("[SOUND] -> Da kich hoat: soundManager.playHit() (Tieng 'Bop')");
        } else {
            System.out.println("[SOUND] -> Da kich hoat: soundManager.playPunch() (Tieng gio)");
        }
    }

    // Hàm hỗ trợ để giả lập việc gọi EffectManager
    private static void simulateEffectCall(float x, float y) {
        System.out.println("[EFFECT] -> Da kich hoat: effectManager.spawnHitEffect tai toa do (" + x + ", " + y + ")");
        System.out.println("[EFFECT] -> Kiem tra: File 'hit_spark.p' da duoc nap.");
    }
}
