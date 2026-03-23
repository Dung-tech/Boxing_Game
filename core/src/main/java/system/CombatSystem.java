package system;

import effect.EffectManager;
import entity.Fighter;
import sound.SoundManager;
import util.Constants;
import util.Constants.Action;

public class CombatSystem {
    private EffectManager effectManager;
    private SoundManager soundManager;

    public CombatSystem(EffectManager effectManager, SoundManager soundManager) {
        this.effectManager = effectManager;
        this.soundManager = soundManager;
    }

    public void update(Fighter p1, Fighter p2) {
        checkHit(p1, p2);
        checkHit(p2, p1);
    }

    private void checkHit(Fighter attacker, Fighter target) {
        // Nếu không tấn công hoặc đã đánh trúng rồi thì bỏ qua
        if (!attacker.isAttacking() || attacker.getStats().hasHit) return;

        Action atk = attacker.getCurrentState();
        Action def = target.getCurrentState();
        boolean isBlocked = false;

        // Logic khắc chế
        if (atk == Action.PUNCH && def == Action.BLOCK) isBlocked = true;
        if (atk == Action.KICK && def == Action.DUCK) isBlocked = true;
        if (atk == Action.SKILL) isBlocked = false; // Skill phá mọi phòng thủ

        if (!isBlocked) {
            float dmg = (atk == Action.SKILL) ? Constants.DAMAGE_SKILL :
                (atk == Action.PUNCH) ? Constants.DAMAGE_PUNCH : Constants.DAMAGE_KICK;

            applyDamage(attacker, target, dmg, atk);
        }
    }

    private void applyDamage(Fighter attacker, Fighter target, float damage, Action type) {
        target.getStats().takeDamage(damage);
        attacker.getStats().hasHit = true; // Chốt chặn: Một lần ra đòn chỉ trúng 1 lần

        if (type != Action.SKILL) attacker.getStats().addMana(1f);

        // Kích hoạt hiệu ứng hình ảnh/âm thanh
        effectManager.spawnHitEffect(target.getX() + 100, target.getY() + 100);
        soundManager.playHit();
    }
}
