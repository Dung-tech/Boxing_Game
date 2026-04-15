package sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import util.AssetManagerWrapper;

public class SoundManager {
    public enum MusicState {
        NONE,
        MENU,
        FIGHT,
        END
    }

    private Sound punch, kick, hit, glassBreak;
    private Music bgMusic, menuMusic, endMusic;
    private AssetManagerWrapper wrapper;
    private MusicState currentMusicState = MusicState.NONE;

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
        this.endMusic = wrapper.getAsset("sounds/end_music.mp3", Music.class);
    }

    // GIỮ TÊN HÀM playMusic() để file GameScreen.java của bạn HẾT LỖI ĐỎ
    public void playMusic() {
        transitionToMusicState(MusicState.FIGHT);
    }

    public void playMenuMusic() {
        transitionToMusicState(MusicState.MENU);
    }

    public void playEndMusic() {
        transitionToMusicState(MusicState.END);
    }

    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            if (currentMusicState == MusicState.MENU) {
                currentMusicState = MusicState.NONE;
            }
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
        if (bgMusic != null) {
            bgMusic.stop();
            if (currentMusicState == MusicState.FIGHT) {
                currentMusicState = MusicState.NONE;
            }
        }
    }

    public void transitionToMusicState(MusicState nextState) {
        if (nextState == null) {
            nextState = MusicState.NONE;
        }

        if (nextState == currentMusicState && isStatePlaying(nextState)) {
            return;
        }

        stopAllMusicInternal();

        switch (nextState) {
            case MENU:
                startMusic(menuMusic, true, 0.5f);
                break;
            case FIGHT:
                startMusic(bgMusic, true, 1.0f);
                break;
            case END:
                startMusic(endMusic, true, 0.7f);
                break;
            case NONE:
            default:
                break;
        }

        currentMusicState = nextState;
    }

    private void startMusic(Music music, boolean loop, float volume) {
        if (music == null) return;
        music.setLooping(loop);
        music.setVolume(volume);
        music.play();
    }

    private void stopAllMusicInternal() {
        if (menuMusic != null) menuMusic.stop();
        if (bgMusic != null) bgMusic.stop();
        if (endMusic != null) endMusic.stop();
    }

    private boolean isStatePlaying(MusicState state) {
        switch (state) {
            case MENU:
                return menuMusic != null && menuMusic.isPlaying();
            case FIGHT:
                return bgMusic != null && bgMusic.isPlaying();
            case END:
                return endMusic != null && endMusic.isPlaying();
            case NONE:
            default:
                return false;
        }
    }

    public void dispose() {
        stopAllMusicInternal();
        if (wrapper != null) wrapper.dispose();
    }
}
