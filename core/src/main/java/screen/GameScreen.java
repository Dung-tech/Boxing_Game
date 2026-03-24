package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import controller.P1Controller;
import controller.P2Controller;
import effect.EffectManager;
import input.GestureReceiver;
import input.KeyboardInput;
import main.Main;
import entity.Fighter;
import system.CombatSystem;
import ui.GameHUD;
import util.Constants;

public class GameScreen extends ScreenAdapter {
    private CombatSystem combatSystem;
    private GameHUD hud;
    private final Main game;
    private Texture background;
    private Fighter p1, p2;
    private EffectManager effectManager;
    private P1Controller p1Controller;
    private P2Controller p2Controller;
    private String controlMode;

    public GameScreen(Main game, String mode) {
        this.game = game;
        this.controlMode = mode;
        background = new Texture("images/background/background.jpg");
        effectManager = new EffectManager();
        effectManager.load();
        hud = new GameHUD();
        combatSystem = new CombatSystem(effectManager, game.soundManager);
        p1 = new Fighter(Constants.Side.LEFT, "images/p1",mode);
        p2 = new Fighter(Constants.Side.RIGHT, "images/p2", mode);
    }

    @Override
    public void render(float delta) {
        if (p1Controller != null) p1Controller.update(delta);
        if (p2Controller != null) p2Controller.update(delta);
        p1.update(delta);
        p2.update(delta);
        combatSystem.update(p1, p2);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(background, 0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);
        p1.draw(game.batch);
        p2.draw(game.batch);
        effectManager.draw(game.batch, delta);
        hud.render(game.batch, p1, p2);
        game.batch.end();
    }
    @Override
    public void show() {
        GestureReceiver.getInstance().start();
        game.soundManager.playMusic();
        KeyboardInput p1Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.D,
            com.badlogic.gdx.Input.Keys.A,
            com.badlogic.gdx.Input.Keys.S,
            com.badlogic.gdx.Input.Keys.W,
            com.badlogic.gdx.Input.Keys.SPACE
        );
        p1Controller = new P1Controller(p1, p1Input);
        KeyboardInput p2Input = new KeyboardInput(
            com.badlogic.gdx.Input.Keys.RIGHT,
            com.badlogic.gdx.Input.Keys.LEFT,
            com.badlogic.gdx.Input.Keys.DOWN,
            com.badlogic.gdx.Input.Keys.UP,
            com.badlogic.gdx.Input.Keys.ENTER
        );
        p2Controller = new P2Controller(p2, p2Input);
    }

    @Override
    public void dispose() {
        background.dispose();
        p1.dispose();
        p2.dispose();
        effectManager.dispose();
        hud.dispose();
    }
}
