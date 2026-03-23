package entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import util.Constants;
import util.Constants.Action;

import java.util.HashMap;
import java.util.Map;

public class Fighter {
    private int hp = Constants.MAX_HP;
    private float mana = 0;
    private Constants.Side side;
    private Texture texture;
    private float x, y;
    private Map<Action, Texture> textures = new HashMap<>();



    // --- LOGIC TRẠNG THÁI (MỚI) ---
    private Action currentState = Action.IDLE; // Mặc định là đứng yên
    private float stateTimer = 0;              // Đếm thời gian trôi qua của một hành động
    private final float ACTION_DURATION = 0.5f; // Mỗi cú đấm/đá kéo dài 0.5 giây
    public int getHp() { return hp; }
    public float getMana() { return mana; }
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);}
    private boolean hasHit = false; // Kiểm tra đòn đánh đã trúng chưa

    public void recordHit() {
        this.mana += 1f; // 10 đòn trúng mới nhận 1 mana
        if (this.mana > Constants.MAX_MANA) this.mana = Constants.MAX_MANA;
    }

    public boolean hasHit() { return hasHit; }
    public void setHit(boolean hit) { this.hasHit = hit; }
    public Fighter(Constants.Side side, String imgPath) {
        this.side = side;
        this.texture = new Texture(imgPath);
        this.y = Constants.GROUND_Y; // Lấy giá trị 100f từ Constants

        // P1 bên trái (25% màn hình), P2 bên phải (75% màn hình)
        if (side == Constants.Side.LEFT) {
            this.x = Constants.APP_WIDTH * 0.25f - Constants.CHAR_SIZE / 2;
        } else {
            this.x = Constants.APP_WIDTH * 0.75f - Constants.CHAR_SIZE / 2;
        }
    }

    /**
     * Hàm ra lệnh: Khi Controller nhấn phím, nó sẽ gọi hàm này.
     */
    // [SỬA] Hàm ra lệnh: Cho phép BLOCK/DUCK đè lên IDLE ngay lập tức và ngược lại
    public void performAction(Action action) {
        // Nếu là các đòn đánh (Trigger)
        if (action == Action.PUNCH || action == Action.KICK || action == Action.SKILL) {
            if (this.currentState == Action.IDLE) {
                this.currentState = action;
                this.stateTimer = 0;
            }
        } else {
            // Nếu là trạng thái (States: BLOCK, DUCK, IDLE)
            // Cho phép thay đổi bất cứ lúc nào (ví dụ đang BLOCK chuyển sang DUCK)
            if (currentState == Action.IDLE || currentState == Action.BLOCK || currentState == Action.DUCK) {
                this.currentState = action;
            }
        }
    }

    /**
     * Hàm cập nhật: Được gọi liên tục 60 FPS trong GameScreen.
     */
    public void update(float delta) {
        // Tự động bật SKILL khi đủ 10 Mana
        if (this.mana >= Constants.MAX_MANA && currentState == Action.IDLE) {
            performAction(Action.SKILL);
            this.mana = 0;
        }

        if (currentState == Action.PUNCH || currentState == Action.KICK || currentState == Action.SKILL) {
            stateTimer += delta;
            if (stateTimer >= ACTION_DURATION) {
                currentState = Action.IDLE;
                stateTimer = 0;
                hasHit = false; // Reset để đòn đánh sau có thể gây sát thương
            }
        }
    }

    public void draw(SpriteBatch batch) {
        // Nếu là P2 (Bên PHẢI) thì lật ngang ảnh (flipX = true) để nhìn sang TRÁI
        boolean flipX = (side == Constants.Side.RIGHT);

        // Vẽ với kích thước chuẩn 200x200 từ Constants
        batch.draw(texture,
            x, y,
            Constants.CHAR_SIZE, Constants.CHAR_SIZE,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            flipX, false);
    }

    public void dispose() {
        texture.dispose();
    }

    // Getter để các hệ thống khác (như Collision) check trạng thái
    public Action getCurrentState() {
        return currentState;
    }
}
