package util;

import com.badlogic.gdx.assets.AssetManager;

public class AssetManagerWrapper {
    private static AssetManager manager = new AssetManager();

    public static void loadAll() {
        // Dev 5 và Dev 4 sẽ vào đây thêm đường dẫn file khi có hàng xịn
        // manager.load(Constants.SFX_PUNCH, Sound.class);
        // manager.load(Constants.TEXTURE_ATLAS, TextureAtlas.class);
    }

    public static <T> T get(String fileName, Class<T> type) {
        return manager.get(fileName, type);
    }

    public static void dispose() {
        manager.dispose();
    }

    public static boolean update() { return manager.update(); }
}
