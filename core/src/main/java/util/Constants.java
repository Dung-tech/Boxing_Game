package util;

public final class Constants {

    private Constants() {}

    public enum Action {
        IDLE,       // Đứng yên/Ko Thủ và Ăn đòn
        PUNCH,      // Đấm
        KICK,       // Đá
        DUCK,       // Cúi người né
        BLOCK,      // Đỡ đòn thành công
        HIT,        // Bị trúng đòn (Bật ra)
        DEAD ,       // Đo sàn (KO)
        SKILL
    }

    // Chỉ số Damage
    public static final int DAMAGE_PUNCH = 1;
    public static final int DAMAGE_KICK = 1;
    public static final int DAMAGE_SKILL = 5;

    // Chỉ số Mana
    public static final int MANA_MAX = 10; // Đủ 10 là dùng được Skill
    public static final int MANA_GAIN_PUNCH = 1;
    public static final int MANA_GAIN_KICK = 1;
    public static final int MANA_COST_SKILL = 10;

    public enum GameState {
        MENU,       // Đang ở màn hình chính
        PLAYING,    // Đang trong trận đấu
        PAUSED,     // Tạm dừng
        GAME_OVER   // Kết thúc trận đấu
    }

    public enum Side {
        LEFT, RIGHT
    }

    public static final String GAME_TITLE = "HUST Boxingame";
    public static final int APP_WIDTH = 1280;
    public static final int APP_HEIGHT = 720;
    public static final int TARGET_FPS = 60;

    // THÔNG SỐ GAMEPLAY (Vật lý & Chỉ số)
    public static final int MAX_HP = 100;
    public static final int MAX_MANA = 100;

    public static final float PLAYER_SPEED = 300f;     // Tốc độ di chuyển (pixels/sec)
    public static final float PUNCH_DAMAGE = 10f;      // Sát thương cú đấm
    public static final float KICK_DAMAGE = 15f;       // Sát thương cú đá
    public static final int MANA_REGEN_PER_HIT = 5;    // Mana cộng thêm khi đấm trúng

    public static final float ROUND_TIME = 180f;       // 3 phút mỗi hiệp (giây)
// --- ĐƯỜNG DẪN TÀI NGUYÊN (Assets Paths) ---
    // Lưu ý: Các đường dẫn này tính từ folder 'assets/' của LibGDX

    // Hình ảnh & Animation
    public static final String TEXTURE_ATLAS = "images/boxing_game.atlas"; // File gom tất cả ảnh nhân vật
    public static final String BG_MENU = "images/background/menu_bg.png";
    public static final String BG_GAME = "images/background/arena_bg.png";

    // Âm thanh (SFX)
    public static final String SFX_PUNCH = "sounds/sfx/punch.mp3";
    public static final String SFX_KICK  = "sounds/sfx/kick.wav";
    public static final String SFX_BLOCK = "sounds/sfx/block.mp3";
    public static final String SFX_HIT   = "sounds/sfx/hit.mp3";

    // Nhạc nền (BGM)
    public static final String BGM_MENU  = "sounds/bgm/menu_theme.mp3";
    public static final String BGM_FIGHT = "sounds/bgm/fight_theme.ogg";


}
