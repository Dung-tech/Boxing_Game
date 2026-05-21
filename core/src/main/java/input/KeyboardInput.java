package input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

// Keyboard-based input mapping for a single fighter.
public class KeyboardInput implements InputController {
    private int kP, kK, kD, kB, kS, kLeft, kRight;
    private int altP = -1, altK = -1, altD = -1, altB = -1, altS = -1;
    private boolean isP, isK, isD, isB, isS, isLeft, isRight;

    public KeyboardInput(int p, int k, int d, int b, int s, int left, int right) {
        this.kP = p; this.kK = k; this.kD = d; this.kB = b; this.kS = s;
        this.kLeft = left; this.kRight = right;
    }

    public KeyboardInput(int p, int altP, int k, int altK, int d, int altD, int b, int altB, int s, int altS, int left, int right) {
        this.kP = p; this.altP = altP;
        this.kK = k; this.altK = altK;
        this.kD = d; this.altD = altD;
        this.kB = b; this.altB = altB;
        this.kS = s; this.altS = altS;
        this.kLeft = left; this.kRight = right;
    }

    @Override public void update(float delta) {
        isP = Gdx.input.isKeyJustPressed(kP) || (altP != -1 && Gdx.input.isKeyJustPressed(altP));
        isK = Gdx.input.isKeyJustPressed(kK) || (altK != -1 && Gdx.input.isKeyJustPressed(altK));
        isD = Gdx.input.isKeyPressed(kD) || (altD != -1 && Gdx.input.isKeyPressed(altD));
        isB = Gdx.input.isKeyPressed(kB) || (altB != -1 && Gdx.input.isKeyPressed(altB));
        isS = Gdx.input.isKeyJustPressed(kS) || (altS != -1 && Gdx.input.isKeyJustPressed(altS));
        isLeft = Gdx.input.isKeyPressed(kLeft);
        isRight = Gdx.input.isKeyPressed(kRight);
    }

    @Override public boolean punch() { return isP; }
    @Override public boolean kick() { return isK; }
    @Override public boolean duck() { return isD; }
    @Override public boolean block() { return isB; }
    @Override public boolean skill() { return isS; }
    @Override public boolean moveLeft() { return isLeft; }
    @Override public boolean moveRight() { return isRight; }

    @Override
    public String getNextAction() {
        return "";
    }

    @Override public void reset() { isP = isK = isS = false; }
}
