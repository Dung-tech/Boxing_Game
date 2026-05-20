package entity.component;

import com.badlogic.gdx.graphics.Texture;
import util.Constants.Action;
import java.util.HashMap;
import java.util.Map;

public class FighterRenderer {
    private Map<Action, Texture> textures = new HashMap<>();
    private Texture hitTexture;
    private float flinchTimer = 0;
    private final float FLINCH_DURATION = 0.15f;

    public FighterRenderer(String folderPath) {
        textures.put(Action.IDLE,  new Texture(folderPath + "/idle.png"));
        textures.put(Action.PUNCH, new Texture(folderPath + "/punch.png"));
        textures.put(Action.KICK,  new Texture(folderPath + "/kick.png"));
        textures.put(Action.BLOCK, new Texture(folderPath + "/block.png"));
        textures.put(Action.DUCK,  new Texture(folderPath + "/duck.png"));
        textures.put(Action.SKILL, new Texture(folderPath + "/skill.png"));
        hitTexture = new Texture(folderPath + "/hit.png");
    }

    private void loadTextures(String path) {
        // Chỉ liệt kê những Action đã có ảnh chuẩn
        Action[] supportedActions = {
            Action.IDLE,
            Action.PUNCH,
            Action.KICK,
            Action.BLOCK,
            Action.DUCK,
            Action.SKILL
        };

        for (Action action : supportedActions) {
            String fileName = path + "/" + action.name().toLowerCase() + ".png";
            textures.put(action, new Texture(fileName));
        }

    }



    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
    }
}
