package system;

import com.badlogic.gdx.Game;
import screen.GameOverScreen;
import util.Constants;
import com.badlogic.gdx.Game;
import entity.Fighter;
import screen.GameOverScreen;
import util.Constants;
import main.Main;

public class GameStateManager {
    private final Game game;
    private String controlMode;
    private Constants.GameState currentState = Constants.GameState.PLAYING;

    public GameStateManager(Main game, String mode) {
        this.game = game;
        this.controlMode = mode;
    }

    public void update(Fighter p1, Fighter p2, RoundSystem roundSystem) {
        if (currentState != Constants.GameState.PLAYING) return;

        if (p1.isDead() || p2.isDead() || roundSystem.isRoundEnded()) {
            currentState = Constants.GameState.GAME_OVER;
            game.setScreen(new GameOverScreen(
                game,
                p1,
                p2,
                roundSystem,
                ((Main) game).batch,
                controlMode // Tham số thứ 6 còn thiếu đây rồi!
            ));
        }
    }

    public void reset() {
        currentState = Constants.GameState.PLAYING;
    }
}
