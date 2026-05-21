package input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import system.GymState;

// Handles gym menu/global input in gym mode.
public class GymInputHandler {
    public enum Action {
        NONE,
        EXIT_MENU,
        RESTART
    }

    public Action handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return Action.EXIT_MENU;
        }
        return Action.NONE;
    }

    public Action handleGameOverInput(GymState state) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return Action.EXIT_MENU;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            state.selectPreviousOption();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            state.selectNextOption();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return state.isContinueSelected() ? Action.RESTART : Action.EXIT_MENU;
        }

        return Action.NONE;
    }
}
