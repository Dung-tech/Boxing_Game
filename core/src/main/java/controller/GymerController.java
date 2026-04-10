package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import entity.Gymer;

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
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			gymer.setConcentric();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			gymer.setEccentric();
		}
	}
}
