package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import controller.P1Controller;
import controller.P2Controller;
import effect.EffectManager;
import input.KeyboardInput;
import main.Main;
import entity.Fighter;
import ui.HealthBar;
import ui.ManaBar;
import util.Constants;

public class GameScreen extends ScreenAdapter {
    private float x, y;
    private final Main game;
    private Texture background;
    private Fighter p1, p2;
    private HealthBar hpBar;
    private ManaBar manaBar;
    private EffectManager effectManager;
    private P1Controller p1Controller;
    private P2Controller p2Controller;

    public GameScreen(Main game) {
        this.game = game;
        // Load lại cái nền xịn của ông giáo
        background = new Texture("images/background/background.jpg");
        effectManager = new EffectManager();
        effectManager.load();
        // Khởi tạo 2 Ronaldo đối đầu
        p1 = new Fighter(Constants.Side.LEFT, "images/p1");
        p2 = new Fighter(Constants.Side.RIGHT, "images/p2");
        hpBar = new HealthBar(Color.RED);
        manaBar = new ManaBar();
    }
    private void applyDamage(Fighter attacker, Fighter target, float damage, Constants.Action attackType) {
        target.takeDamage(damage, attackType);

        if (effectManager != null) {
            effectManager.spawnHitEffect(target.getX() + 100, target.getY() + 100);
        }

        if (attackType != Constants.Action.SKILL) {
            attacker.recordHit();
        }
        attacker.setHit(true);
        game.soundManager.playHit();
    }
    private void processAttack(Fighter attacker, Fighter target) {
        if (attacker.hasHit()) return; // Nếu đòn này đã trúng rồi thì bỏ qua

        Constants.Action atk = attacker.getCurrentState();
        Constants.Action def = target.getCurrentState();

        if (atk == Constants.Action.SKILL) {
            applyDamage(attacker, target, Constants.DAMAGE_SKILL, atk);
        }
        else if (atk == Constants.Action.PUNCH && def != Constants.Action.BLOCK) {
            applyDamage(attacker, target, Constants.DAMAGE_PUNCH, atk);
        }
        else if (atk == Constants.Action.KICK && def != Constants.Action.DUCK) {
            applyDamage(attacker, target, Constants.DAMAGE_KICK, atk);
        }
    }
    private void handleCombat() {
        // P1 tấn công P2
        processAttack(p1, p2);
        // P2 tấn công P1
        processAttack(p2, p1);
    }

    @Override
    public void render(float delta) {
        // 1. CẬP NHẬT LOGIC (PHẢI CÓ)
        if (p1Controller != null) p1Controller.update(delta);
        if (p2Controller != null) p2Controller.update(delta);
        p1.update(delta);
        p2.update(delta);


        // 2. XỬ LÝ CHIẾN ĐẤU
        handleCombat();
        // VẼ GIAO
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        // 1. Vẽ nền trước (Phủ kín màn hình 1280x720)
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        // 2. Vẽ 2 võ sĩ (Tự động đứng đúng chỗ và đối mặt nhau)
        p1.draw(game.batch);
        p2.draw(game.batch);
        // 3. Vẽ HUD cho P1 (Bên trái)
        hpBar.draw(game.batch, p1.getHp(), Constants.MAX_HP, 50, 650);
        manaBar.draw(game.batch, p1.getMana(), 50, 610);
        effectManager.draw(game.batch, delta);

        // 4. Vẽ HUD cho P2 (Bên phải - lùi lại 350px để chừa chỗ cho độ dài 300px)
        hpBar.draw(game.batch, p2.getHp(), Constants.MAX_HP, Constants.APP_WIDTH - 350, 650);
        manaBar.draw(game.batch, p2.getMana(), Constants.APP_WIDTH - 350, 610);
        game.batch.end();
    }
    @Override
    public void show() {
        // Phát nhạc nền khi màn hình game hiện ra
        game.soundManager.playMusic();
        // Thiết lập phím cho P1 (J: Đấm, K: Đá, S: Né, B: Đỡ, L: Skill)
        KeyboardInput p1Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.J,
            com.badlogic.gdx.Input.Keys.K,
            com.badlogic.gdx.Input.Keys.S,
            com.badlogic.gdx.Input.Keys.W,
            com.badlogic.gdx.Input.Keys.SPACE
        );

        // Truyền soundManager từ Main vào Controller
        p1Controller = new P1Controller(p1, p1Input);

        // Tương tự cho P2 (Ví dụ dùng phím Numpad)
        KeyboardInput p2Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.NUMPAD_1,
            com.badlogic.gdx.Input.Keys.NUMPAD_2,
            com.badlogic.gdx.Input.Keys.DOWN,
            com.badlogic.gdx.Input.Keys.UP,
            com.badlogic.gdx.Input.Keys.ENTER
        );
        p2Controller = new P2Controller(p2, p2Input);
    }

    @Override
    public void dispose() {
        background.dispose();
        p1.dispose();
        p2.dispose();
        hpBar.dispose();
        manaBar.dispose();
        effectManager.dispose();
    }
}
