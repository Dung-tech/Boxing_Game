package entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entity.component.FighterStats;
import input.GestureReceiver;
import util.Constants;
import util.Constants.Action;
import java.util.HashMap;
import java.util.Map;

public class Fighter {
    private String controlMode;
    private float x, y;
    private Constants.Side side;
    private FighterStats stats;
    private Map<Action, Texture> textures = new HashMap<>();

    // Trạng thái logic
    private Action currentState = Action.IDLE;
    private float stateTimer = 0;
    private final float ACTION_DURATION = 0.5f;

    public Fighter(Constants.Side side, String folderPath, String mode) {
        this.side = side;
        this.controlMode = mode;
        this.stats = new FighterStats();
        this.y = Constants.GROUND_Y;
        this.x = (side == Constants.Side.LEFT) ?
            Constants.APP_WIDTH * 0.25f - Constants.CHAR_SIZE / 2 :
            Constants.APP_WIDTH * 0.75f - Constants.CHAR_SIZE / 2;

        loadTextures(folderPath);
    }

    private void loadTextures(String path) {
        // Chỉ liệt kê những Action đã có ảnh chuẩn
        Action[] supportedActions = {
            Action.IDLE,
            Action.PUNCH,
            Action.KICK,
            Action.BLOCK,
            Action.DUCK,
            Action.SKILL
        };

        for (Action action : supportedActions) {
            String fileName = path + "/" + action.name().toLowerCase() + ".png";
            textures.put(action, new Texture(fileName));
        }

    }
    private String getManualInput() {
        if (side == Constants.Side.LEFT) {
            if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A)) return "PUNCH";
            if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) return "KICK";
            // ... thêm các phím khác của P1 ...
        } else {
            // ... check phím của P2 ...
        }
        return "NONE";
    }

    public void update(float delta) {
        // --- PHẦN 1: XỬ LÝ ĐẦU VÀO (INPUT HANDLING) ---
        String actionStr = "NONE";

        if ("CAMERA_AI".equals(this.controlMode)) {
            // Lấy lệnh từ AI (Dựa trên playerId)
            int playerId = (side == Constants.Side.LEFT) ? 1 : 2;
            actionStr = GestureReceiver.getInstance().getActionForPlayer(playerId);
        } else {
            // Lấy lệnh từ Bàn phím (Ông giáo viết hàm getManualInput bên dưới nhé)
            actionStr = getManualInput();
        }

        // Thực hiện hành động nếu có (Từ AI hoặc Bàn phím)
        if (!actionStr.equals("NONE")) {
            try {
                Action inputAction = Action.valueOf(actionStr);
                performAction(inputAction);
            } catch (IllegalArgumentException e) {
                // Bỏ qua nếu chuỗi không khớp Enum Action
            }
        }

        // --- PHẦN 2: LOGIC TRẠNG THÁI (STATE LOGIC - GIỮ NGUYÊN 100%) ---
        // Tự động hồi chiêu (Reset trạng thái tấn công)
        if (isAttacking()) {
            stateTimer += delta;
            if (stateTimer >= ACTION_DURATION) {
                resetToIdle();
            }
        }

        // --- PHẦN 3: LOGIC TỰ ĐỘNG (AUTO LOGIC - GIỮ NGUYÊN 100%) ---
        // Auto-Skill logic khi đủ Mana
        if (stats.mana >= Constants.MAX_MANA && currentState == Action.IDLE) {
            performAction(Action.SKILL);
            stats.mana = 0;
        }
    }

    public void performAction(Action action) {
        // Nếu là hành động tấn công/di chuyển, chỉ cho phép khi đang đứng yên
        if (action == Action.PUNCH || action == Action.KICK || action == Action.SKILL) {
            if (this.currentState == Action.IDLE) {
                this.currentState = action;
                this.stateTimer = 0;
                this.stats.hasHit = false; // Bắt đầu đòn mới, reset flag trúng đòn
            }
        } else {
            // Các trạng thái giữ phím (Block, Duck)
            if (!isAttacking()) this.currentState = action;
        }
    }

    private void resetToIdle() {
        this.currentState = Action.IDLE;
        this.stateTimer = 0;
        this.stats.hasHit = false;
    }

    public void draw(SpriteBatch batch) {
        boolean flipX = (side == Constants.Side.RIGHT);
        Texture currentTex = textures.get(currentState);
        if (currentTex == null) currentTex = textures.get(Action.IDLE);

        batch.draw(currentTex, x, y, Constants.CHAR_SIZE, Constants.CHAR_SIZE,
            0, 0, currentTex.getWidth(), currentTex.getHeight(), flipX, false);
    }

    // GETTERS & SETTERS
    public boolean isAttacking() {
        return currentState == Action.PUNCH || currentState == Action.KICK || currentState == Action.SKILL;
    }
    public Action getCurrentState() { return currentState; }
    public FighterStats getStats() { return stats; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return stats.hp; }
    public float getMana() {return stats.mana; }

    public void dispose() {
        for (Texture t : textures.values()) if (t != null) t.dispose();
    }
}
