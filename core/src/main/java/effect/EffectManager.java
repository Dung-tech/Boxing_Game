package effect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class EffectManager {

    // Danh sách các hiệu ứng đang chạy trên màn hình
    private Array<ParticleEffect> activeEffects;

    // Mẫu hiệu ứng (Template) để nhân bản ra nhiều hạt
    private ParticleEffect hitEffectTemplate;

    public EffectManager() {
        activeEffects = new Array<>();
    }

    public void load() {
        hitEffectTemplate = new ParticleEffect();

        // Đường dẫn file hiệu ứng (bạn sẽ tạo thư mục này ở Bước 2)
        try {
            hitEffectTemplate.load(
                Gdx.files.internal("effects/hit_spark.p"),
                Gdx.files.internal("effects")
            );
        } catch (Exception e) {
            Gdx.app.log("EffectManager", "Khong tim thay file hieu ung hit_spark.p");
        }
    }

    // Hàm tạo tia lửa tại vị trí x, y khi đấm trúng
    public void spawnHitEffect(float x, float y) {
        if (hitEffectTemplate != null) {
            ParticleEffect newEffect = new ParticleEffect(hitEffectTemplate);
            newEffect.scaleEffect(5.0f);
            newEffect.setPosition(x, y);
            newEffect.start();
            activeEffects.add(newEffect);
        }
    }

    // Vẽ hiệu ứng (Lead/Dev 4 sẽ gọi hàm này trong vòng lặp render)
    public void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < activeEffects.size; i++) {
            ParticleEffect effect = activeEffects.get(i);
            effect.draw(batch, delta);

            // Xóa hiệu ứng khi chạy xong để giải phóng RAM
            if (effect.isComplete()) {
                effect.dispose();
                activeEffects.removeIndex(i);
                i--;
            }
        }
    }

    public void dispose() {
        if (hitEffectTemplate != null) hitEffectTemplate.dispose();
        for (ParticleEffect effect : activeEffects) {
            effect.dispose();
        }
        activeEffects.clear();
    }
}
