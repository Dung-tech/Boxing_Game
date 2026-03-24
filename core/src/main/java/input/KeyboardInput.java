package input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class KeyboardInput implements InputController {
    private int kP, kK, kD, kB, kS;
    private boolean isP, isK, isD, isB, isS;

    public KeyboardInput(int p, int k, int d, int b, int s) {
        this.kP = p; this.kK = k; this.kD = d; this.kB = b; this.kS = s;
    }

    @Override public void update(float delta) {
        isP = Gdx.input.isKeyJustPressed(kP);
        isK = Gdx.input.isKeyJustPressed(kK);
        isD = Gdx.input.isKeyPressed(kD);
        isB = Gdx.input.isKeyPressed(kB);
        isS = Gdx.input.isKeyJustPressed(kS);
    }

    @Override public boolean punch() { return isP; }
    @Override public boolean kick() { return isK; }
    @Override public boolean duck() { return isD; }
    @Override public boolean block() { return isB; }
    @Override public boolean skill() { return isS; }

    @Override
    public String getNextAction() {
        return "";
    }

    @Override public void reset() { isP = isK = isS = false; }
}
