package system;

import controller.GymerController;
import entity.Gymer;
import input.GymInputHandler;

public class GymSession {
    private final Gymer gymer;
    private final GymerController gymerController;
    private final GymState state;
    private final GymInputHandler inputHandler;

    public GymSession() {
        this.gymer = new Gymer();
        this.gymerController = new GymerController(gymer);
        this.state = new GymState();
        this.inputHandler = new GymInputHandler();
    }

    public void update(float delta, Runnable exitToMenu) {
        if (state.isGameOver()) {
            GymInputHandler.Action action = inputHandler.handleGameOverInput(state);
            if (action == GymInputHandler.Action.EXIT_MENU) {
                exitToMenu.run();
            } else if (action == GymInputHandler.Action.RESTART) {
                gymer.reset();
                state.resetAfterRestart();
            }
            return;
        }

        GymInputHandler.Action action = inputHandler.handleGlobalInput();
        if (action == GymInputHandler.Action.EXIT_MENU) {
            exitToMenu.run();
            return;
        }

        gymerController.update();
        state.update(delta, gymer);
    }

    public Gymer getGymer() {
        return gymer;
    }

    public GymState getState() {
        return state;
    }
}
