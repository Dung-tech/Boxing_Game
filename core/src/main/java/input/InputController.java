package input;

public interface InputController {

    boolean kick();

    boolean duck();  //cúi xuống

    boolean punch();

    boolean block();

    boolean skill();

    String getNextAction();

    void update(float delta);

    void reset();

}
