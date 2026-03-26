package sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import util.AssetManagerWrapper;

public class SoundManager {
    private Sound punch, kick, hit, glassBreak;
    private Music bgMusic, menuMusic;
    private AssetManagerWrapper wrapper;

    // GIỮ TÊN HÀM load() để file Main.java không bị lỗi
    public void load() {
        if (wrapper == null) {
            wrapper = new AssetManagerWrapper();
            wrapper.loadAssets();
        }

        this.punch = wrapper.getAsset("sounds/punch.mp3", Sound.class);
        this.kick = wrapper.getAsset("sounds/kick.mp3", Sound.class);
        this.hit = wrapper.getAsset("sounds/hit.mp3", Sound.class);
        this.glassBreak = wrapper.getAsset("sounds/glass_break.mp3", Sound.class);
        this.bgMusic = wrapper.getAsset("sounds/bg_music.mp3", Music.class);
        this.menuMusic = wrapper.getAsset("sounds/menu_music.mp3", Music.class);
    }

    // GIỮ TÊN HÀM playMusic() để file GameScreen.java của bạn HẾT LỖI ĐỎ
    public void playMusic() {
        if (bgMusic != null) {
            bgMusic.setLooping(true);
            bgMusic.play();
        }
    }

    public void playMenuMusic() {
        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f);
            menuMusic.play();
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    // Hàm này dự phòng nếu có file khác gọi tên dài hơn
    public void playBackgroundMusic() {
        playMusic();
    }

    public void playPunch() { if (punch != null) punch.play(); }
    public void playKick() { if (kick != null) kick.play(); }
    public void playHit() { if (hit != null) hit.play(); }
    public void playGlassBreak() { if (glassBreak != null) glassBreak.play(); }

    public void stopBackgroundMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.stop();
        }
    }

    public void dispose() {
        if (wrapper != null) wrapper.dispose();
    }
}
