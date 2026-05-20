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

    private void applyDamage(Fighter attacker, Fighter target, float damage, Action attackType) {
        // Gọi takeDamage trên Fighter (đã có logic né đòn bên trong)
        target.takeDamage(damage, attackType);

        attacker.getStats().hasHit = true;

        // Hồi mana cho người tấn công (trừ skill)
        if (attackType != Action.SKILL) {
            attacker.getStats().addMana(Constants.MANA_GAIN_PER_HIT);
        }

        // Hiệu ứng + âm thanh
        effectManager.spawnHitEffect(target.getX() + 100, target.getY() + 100);
        soundManager.playHit();
    }
}
