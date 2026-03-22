package test;

public class InputTest {

    public static void main(String[] args) {
        System.out.println("========== HE THONG KIEM TRA DAU VAO (DEV 5) ==========");

        // Giả lập danh sách các tín hiệu từ Python AI gửi về
        String[] aiSignals = {"PUNCH", "BLOCK", "MOVE_LEFT", "UNKNOWN", "PUNCH"};

        for (int i = 0; i < aiSignals.length; i++) {
            String signal = aiSignals[i];
            System.out.println("\n[STEP " + (i + 1) + "] Nhan tin hieu tu AI: " + signal);

            processInput(signal);
        }

        System.out.println("\n================ KET THUC KIEM TRA INPUT ================");
    }

    /**
     * Hàm giả lập logic xử lý Input để kết nối với Sound & Effect
     */
    private static void processInput(String input) {
        switch (input) {
            case "PUNCH":
                System.out.println("   => HANH DONG: Dam");
                System.out.println("   => GOI AM THANH: soundManager.playPunch()");
                System.out.println("   => GOI HIEU UNG: (Neu trung dich) effectManager.spawnHitEffect()");
                break;

            case "BLOCK":
                System.out.println("   => HANH DONG: Do don");
                System.out.println("   => GOI AM THANH: soundManager.playBlock() (Neu co)");
                break;

            case "MOVE_LEFT":
            case "MOVE_RIGHT":
                System.out.println("   => HANH DONG: Di chuyen");
                System.out.println("   => GHI CHU: Khong kich hoat am thanh chien dau.");
                break;

            default:
                System.out.println("   [WARNING] Tin hieu khong hop le! He thong se bo qua.");
                break;
        }
    }
}
