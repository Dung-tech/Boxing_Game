package sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import util.AssetManagerWrapper;

public class SoundManager {
    private static final float MENU_MUSIC_BASE_VOLUME = 0.5f;
    private static final float FIGHT_MUSIC_BASE_VOLUME = 1.0f;
    private static final float END_MUSIC_BASE_VOLUME = 0.7f;

    public enum MusicState {
        NONE,
        MENU,
        FIGHT,
        END
    }

    private Sound punch, kick, hit, glassBreak, countDown, siuu, ankaraMessi;
    private Music bgMusic, menuMusic, endMusic;
    private AssetManagerWrapper wrapper;
    private MusicState currentMusicState = MusicState.NONE;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;

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
        this.countDown = wrapper.getAsset("sounds/CountDownSound.mp3", Sound.class);
        this.siuu = wrapper.getAsset("sounds/siuu.mp3", Sound.class);
        this.ankaraMessi = wrapper.getAsset("sounds/ankara_messi.mp3", Sound.class);
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

    public void playPunch() { if (punch != null) punch.play(sfxVolume); }
    public void playKick() { if (kick != null) kick.play(sfxVolume); }
    public void playHit() { if (hit != null) hit.play(sfxVolume); }
    public void playGlassBreak() { if (glassBreak != null) glassBreak.play(sfxVolume); }
    public void playCountDown() { if (countDown != null) countDown.play(sfxVolume); }
    public void playSiuu() { if (siuu != null) siuu.play(sfxVolume); }
    public void playAnkaraMessi() { if (ankaraMessi != null) ankaraMessi.play(sfxVolume); }

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
                startMusic(menuMusic, true, MENU_MUSIC_BASE_VOLUME);
                break;
            case FIGHT:
                startMusic(bgMusic, true, FIGHT_MUSIC_BASE_VOLUME);
                break;
            case END:
                startMusic(endMusic, true, END_MUSIC_BASE_VOLUME);
                break;
            case NONE:
            default:
                break;
        }

        currentMusicState = nextState;
    }

    private void startMusic(Music music, boolean loop, float baseVolume) {
        if (music == null) return;
        music.setLooping(loop);
        applyMusicVolume(music, baseVolume);
        music.play();
    }

    private void applyMusicVolume(Music music, float baseVolume) {
        if (music == null) return;
        music.setVolume(baseVolume * musicVolume);
    }

    private void updateCurrentMusicVolume() {
        switch (currentMusicState) {
            case MENU:
                applyMusicVolume(menuMusic, MENU_MUSIC_BASE_VOLUME);
                break;
            case FIGHT:
                applyMusicVolume(bgMusic, FIGHT_MUSIC_BASE_VOLUME);
                break;
            case END:
                applyMusicVolume(endMusic, END_MUSIC_BASE_VOLUME);
                break;
            case NONE:
            default:
                break;
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        musicVolume = clampVolume(volume);
        updateCurrentMusicVolume();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clampVolume(volume);
    }

    private float clampVolume(float volume) {
        return Math.max(0f, Math.min(1f, volume));
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
