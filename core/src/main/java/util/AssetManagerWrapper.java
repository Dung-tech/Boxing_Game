package util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AssetManagerWrapper {
    private AssetManager manager = new AssetManager();

    public void loadAssets() {
        // Nạp các file hiệu ứng
        manager.load("sounds/punch.mp3", Sound.class);
        manager.load("sounds/hit.mp3", Sound.class);
        manager.load("sounds/kick.mp3", Sound.class);
        manager.load("sounds/glass_break.mp3", Sound.class);

        // GIỮ NGUYÊN TÊN FILE NHẠC NỀN CỦA BẠN
        manager.load("sounds/bg_music.mp3", Music.class);

        manager.finishLoading();
    }

    public <T> T getAsset(String fileName, Class<T> type) {
        if (manager.isLoaded(fileName)) {
            return manager.get(fileName, type);
        }
        return null;
    }

    public void dispose() {
        manager.dispose();
    }
}
