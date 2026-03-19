package sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {

    private Music backgroundMusic;

    private Sound punchSound;
    private Sound hitSound;

    public void load() {
        backgroundMusic = Gdx.audio.newMusic(
            Gdx.files.internal("sounds/bg_music.mp3")
        );

        punchSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/punch.mp3")
        );

        hitSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/hit.mp3")
        );
    }

    public void playMusic() {
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();
    }

    public void playPunch() {
        punchSound.play(1.0f);
    }

    public void playHit() {
        hitSound.play(1.0f);
    }

    public void dispose() {
        backgroundMusic.dispose();
        punchSound.dispose();
        hitSound.dispose();
    }
}
