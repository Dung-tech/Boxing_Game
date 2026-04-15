package entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entity.component.FighterStats;
import input.GestureReceiver;
import util.Constants;
import util.Constants.Action;
import java.util.HashMap;
import java.util.Map;
import static util.Constants.Action.*;

public class Fighter {
    private String controlMode;
    private float x, y;
    private Constants.Side side;
    private FighterStats stats;
    private Map<Action, Texture> textures = new HashMap<>();
    private boolean isDead = false;
    private boolean skillCutscenePending = false;

    // Trạng thái logic
    private Action currentState = Action.IDLE;
    private float stateTimer = 0;
    private final float ACTION_DURATION = 0.5f;
    private final float HIT_DURATION = 0.5f;   // ← HIT sẽ hiển thị lâu hơn

    public Fighter(Constants.Side side, String folderPath, String mode) {
        this.side = side;
        this.controlMode = mode;
        this.stats = new FighterStats();
        this.y = Constants.GROUND_Y;

        // Vị trí đứng sát nhau hơn + cân đối
        this.x = (side == Constants.Side.LEFT) ?
            Constants.APP_WIDTH * 0.30f :     // Ronaldo (bên trái)
            Constants.APP_WIDTH * 0.45f;      // Messi (bên phải)
        loadTextures(folderPath);
    }

    // Thêm biến này để nhớ trạng thái trước khi bị hit (dùng để chọn hit_stand hay hit_duck)
    private Action previousState = IDLE;

    // ==================== LOAD TEXTURES ====================
    private void loadTextures(String path) {
        // Load các action bình thường
        Action[] supportedActions = {
            IDLE, PUNCH, KICK, BLOCK, DUCK, SKILL
        };

        for (Action action : supportedActions) {
            String fileName = path + "/" + action.name().toLowerCase() + ".png";
            textures.put(action, new Texture(fileName));
        }

        // Load riêng 2 ảnh hit
        textures.put(HIT, new Texture(path + "/hit_stand.png"));   // hit khi đứng
        // Nếu có hit_duck.png thì load thêm (nếu chưa có thì tạm comment)
        // textures.put("HIT_DUCK", new Texture(path + "/hit_duck.png"));
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
        // Input handling
        String actionStr = "NONE";

        if ("CAMERA_AI".equals(this.controlMode) || "CAMERA_POSE".equals(this.controlMode)) {
            int playerId = (side == Constants.Side.LEFT) ? 1 : 2;
            actionStr = GestureReceiver.getInstance().getActionForPlayer(playerId);
        } else {
            actionStr = getManualInput();
        }

        if (!actionStr.equals("NONE")) {
            try {
                Action inputAction = Action.valueOf(actionStr);
                performAction(inputAction);
            } catch (IllegalArgumentException e) {}
        }

        // ==================== STATE TIMER LOGIC ====================
        if (currentState == HIT) {
            stateTimer += delta;
            if (stateTimer >= HIT_DURATION) {
                resetToIdle();
            }
        }
        else if (isAttacking()) {
            stateTimer += delta;
            if (stateTimer >= ACTION_DURATION) {
                resetToIdle();
            }
        }

    }
    public void performAction(Action action) {
        // Không cho phép thay đổi trạng thái khi đang HIT
        if (currentState == HIT) {
            return;
        }

        if (action == SKILL) {
            if (stats.mana < Constants.MANA_COST_SKILL) {
                return;
            }

            if (this.currentState == IDLE) {
                this.currentState = action;
                this.stateTimer = 0;
                this.stats.hasHit = false;
                this.stats.mana = 0;
                this.skillCutscenePending = true;
            }
        } else if (action == PUNCH || action == KICK) {
            if (this.currentState == IDLE) {
                this.currentState = action;
                this.stateTimer = 0;
                this.stats.hasHit = false;
            }
        } else {
            // Block, Duck
            if (!isAttacking() && currentState != HIT) {
                this.currentState = action;
            }
        }
    }

    private void resetToIdle() {
        this.currentState = IDLE;
        this.stateTimer = 0;
        this.stats.hasHit = false;
    }

    public void draw(SpriteBatch batch) {
        boolean flipX = (side == Constants.Side.RIGHT);
        Texture currentTex = textures.get(currentState);

        // Nếu đang HIT thì dùng hit_stand
        if (currentState == HIT) {
            currentTex = textures.get(HIT);
        }

        // Fallback về IDLE nếu không có
        if (currentTex == null) {
            currentTex = textures.get(IDLE);
        }

        if (currentTex == null) return;

        // ====================== SỬA CHÍNH Ở ĐÂY ======================
        // Giữ chiều cao cố định = CHAR_SIZE (380f hiện tại)
        // Chiều rộng tính theo tỷ lệ gốc của texture (682x1024 → sẽ ra khoảng 253)
        float targetHeight = Constants.CHAR_SIZE;
        float targetWidth  = targetHeight * (currentTex.getWidth() / (float) currentTex.getHeight());

        // Vẽ với kích thước mới (không ép vuông nữa)
        batch.draw(currentTex,
            x, y,                          // vị trí
            targetWidth, targetHeight,     // kích thước thực tế
            0, 0,
            currentTex.getWidth(),         // toàn bộ texture
            currentTex.getHeight(),
            flipX, false);
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

    public boolean consumeSkillCutsceneTrigger() {
        if (skillCutscenePending) {
            skillCutscenePending = false;
            return true;
        }
        return false;
    }

    public void takeDamage(float damage, Action attackType) {
        previousState = currentState;   // lưu trạng thái trước khi bị hit

        boolean isHit = true;

        // === LOGIC NÉ ĐÒN ===
        if (attackType == PUNCH) {
            if (currentState == BLOCK) {
                isHit = false;                    // Block né được đấm
            }
        }
        else if (attackType == KICK) {
            if (currentState == DUCK) {
                isHit = false;                    // Duck né được đá
            }
        }
        else if (attackType == SKILL) {
            isHit = true;                         // Skill không né được
        }

        if (!isHit) {
            return;   // né thành công → không mất máu, không đổi trạng thái
        }

        // === BỊ TRÚNG ĐÒN ===
        stats.hp -= (int) damage;

        if (stats.hp <= 0) {
            stats.hp = 0;
            isDead = true;
            currentState = HIT;
            stateTimer = 0;
            return;
        }

        // Đổi sang trạng thái HIT
        currentState = HIT;
        stateTimer = 0;           // Reset timer để hit hiển thị đủ lâu
    }

    public boolean isDead() {
        return isDead || stats.hp <= 0;
    }

    public void reset() {
        stats.hp = Constants.MAX_HP;
        stats.mana = Constants.MAX_MANA;
        isDead = false;
        skillCutscenePending = false;
        resetToIdle();
    }

    public void dispose() {
        for (Texture t : textures.values()) if (t != null) t.dispose();
    }
}
