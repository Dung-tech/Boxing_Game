package sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {

    private Music backgroundMusic;
    private Sound punchSound;
    private Sound hitSound;

    public void load() {
        // Kiểm tra xem file có tồn tại không trước khi load (Tránh crash game)
        if (Gdx.files.internal("sounds/bg_music.mp3").exists()) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/bg_music.mp3"));
        }

        if (Gdx.files.internal("sounds/punch.mp3").exists()) {
            punchSound = Gdx.audio.newSound(Gdx.files.internal("sounds/punch.mp3"));
        }

        if (Gdx.files.internal("sounds/hit.mp3").exists()) {
            hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.mp3"));
        }
    }

    public void playMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
            backgroundMusic.play();
        }
    }

    public void playPunch() {
        if (punchSound != null) {
            punchSound.play(1.0f);
        }
    }

    public void playHit() {
        if (hitSound != null) {
            hitSound.play(1.0f);
        }
    }

    public void dispose() {
        // Giải phóng bộ nhớ an toàn
        if (backgroundMusic != null) backgroundMusic.dispose();
        if (punchSound != null) punchSound.dispose();
        if (hitSound != null) hitSound.dispose();
    }
}
