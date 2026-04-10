package screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import entity.Fighter;
import main.Main;
import system.RoundSystem;
import util.Constants;

public class GameOverScreen extends ScreenAdapter {

    private final Game game;
    private final Fighter p1;
    private final Fighter p2;
    private final RoundSystem roundSystem;
    private final SpriteBatch batch;
    private final BitmapFont bigFont;
    private final BitmapFont smallFont;
    private String currentMode;

    public GameOverScreen(Game game, Fighter p1, Fighter p2, RoundSystem roundSystem, SpriteBatch batch,String mode) {
        this.game = game;
        this.p1 = p1;
        this.p2 = p2;
        this.currentMode = mode;
        this.roundSystem = roundSystem;
        this.batch = batch;
        this.bigFont = new BitmapFont();
        this.smallFont = new BitmapFont();

        bigFont.getData().setScale(2.5f);   // chữ to hơn
        smallFont.getData().setScale(1.2f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // === TIÊU ĐỀ ===
        bigFont.draw(batch, "GAME OVER", Constants.APP_WIDTH / 2f - 140, 520);

        // === KẾT QUẢ ===
        String resultText;
        if (roundSystem.getP1RoundWins() > roundSystem.getP2RoundWins()) {
            resultText = "P1 WINS MATCH!";
        } else if (roundSystem.getP2RoundWins() > roundSystem.getP1RoundWins()) {
            resultText = "P2 WINS MATCH!";
        } else {
            resultText = "DRAW MATCH";
        }

        bigFont.draw(batch, resultText, Constants.APP_WIDTH / 2f - 110, 410);

        // === HP ===
        smallFont.draw(batch, "P1 Final HP : " + p1.getHp(), 380, 300);
        smallFont.draw(batch, "P2 Final HP : " + p2.getHp(), 680, 300);
        smallFont.draw(batch,
            "Round Score: P1 " + roundSystem.getP1RoundWins() + " - " + roundSystem.getP2RoundWins() + " P2",
            Constants.APP_WIDTH / 2f - 160, 260);

        // === Hướng dẫn ===
        smallFont.draw(batch, "Press [ R ] to Play Again", Constants.APP_WIDTH / 2f - 130, 180);

        batch.end();

        // Restart khi bấm R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            p1.reset();
            p2.reset();
            roundSystem.reset();
            game.setScreen(new GameScreen((Main) game, currentMode));
        }
    }

    @Override
    public void dispose() {
        bigFont.dispose();
        smallFont.dispose();
    }
}
