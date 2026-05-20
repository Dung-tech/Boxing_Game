package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import entity.Gymer;
import input.GymGestureReceiver;

public class GymerController {
	private final Gymer gymer;

	public GymerController(Gymer gymer) {
		this.gymer = gymer;
	}

	public void update() {
		if (gymer.isExhausted()) {
			return;
		}
		handleInput();
	}

	private void handleInput() {
		String cameraAction = GymGestureReceiver.getInstance().getGymAction();
		if ("CONCENTRIC".equals(cameraAction)) {
			gymer.setConcentric();
		} else if ("ECCENTRIC".equals(cameraAction)) {
			gymer.setEccentric();
		}

		// Keep keyboard active in parallel; key press can override camera state this frame.
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			gymer.setConcentric();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			gymer.setEccentric();
		}
	}
}
