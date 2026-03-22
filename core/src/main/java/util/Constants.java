package util;

import com.badlogic.gdx.Input;

public final class Constants {

    private Constants() {}

    // --- CẤU HÌNH HỆ THỐNG ---
    public static final String GAME_TITLE = "HUST Boxingame";
    public static final int APP_WIDTH     = 1280;
    public static final int APP_HEIGHT    = 720;
    public static final int TARGET_FPS    = 60;

    // --- CHỈ SỐ VÕ SĨ (20 MÁU - 10 MANA) ---
    public static final int   MAX_HP            = 30;    // Đấm 20 phát là đi
    public static final float MAX_MANA          = 10f;   // Đủ 10 là nổ Skill
    public static final float GROUND_Y          = 100f;  // Đứng cố định trên sàn
    public static final float CHAR_SIZE         = 200f;  // Cho nhân vật to lên nhìn cho sướng (vì ko di chuyển)

    // --- CHỈ SỐ SÁT THƯƠNG (1 MÁU/ĐÒN) ---
    public static final float DAMAGE_PUNCH      = 1f;
    public static final float DAMAGE_KICK       = 1f;
    public static final float DAMAGE_SKILL      = 5f;

    // --- CHỈ SỐ NĂNG LƯỢNG ---
    public static final float MANA_GAIN_PER_HIT = 1f;    // Trúng 1 đòn hồi 1 mana
    public static final float MANA_COST_SKILL   = 10f;

    // --- THÔNG SỐ TRẬN ĐẤU ---
    public static final float ROUND_TIME        = 180f;
    public static final int   PORT_UDP_PYTHON   = 5005;

    // --- 5 HÀNH ĐỘNG DUY NHẤT ---
    public enum Action {
        IDLE,       // Đứng yên
        PUNCH,      // Kỹ năng 1
        KICK,       // Kỹ năng 2
        DUCK,       // Kỹ năng 3 (Né đòn cao)
        BLOCK,      // Kỹ năng 4 (Giảm dame/Vô hiệu hóa)
        SKILL,      // Tuyệt kỹ (Dùng khi đủ 10 mana)
        HIT,        // Bị dính đòn (Trạng thái thụ động)
        DEAD        // Thua
    }
    // Thêm vào Constants.java
    public enum Side { LEFT, RIGHT }
    public enum GameState { MENU, PLAYING, PAUSED, GAME_OVER }

    // --- ASSETS PATHS ---
    public static final String TEXTURE_ATLAS = "images/boxing_game.atlas";
    public static final String BG_GAME       = "images/background/arena_bg.png";
    public static final String SFX_PUNCH     = "sounds/sfx/punch.mp3";
    public static final String SFX_KICK      = "sounds/sfx/kick.wav";
    public static final String SFX_SKILL     = "sounds/sfx/ultimate.mp3";
    public static final String SFX_HIT       = "sounds/sfx/hit.mp3";
}
