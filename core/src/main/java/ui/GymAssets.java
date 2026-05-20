package ui;

import com.badlogic.gdx.graphics.Texture;

// Loads and exposes textures used in gym mode.
public class GymAssets {
    private Texture backgroundGym;
    private Texture messiEating;
    private Texture messiDrinking;
    private Texture messiSad;
    private Texture concentric;
    private Texture eccentric;
    private Texture ronalSiu;

    public void load() {
        backgroundGym = new Texture("images/background/bakgroundGym.jpg");
        messiEating = new Texture("images/gym/messiEating.png");
        messiDrinking = new Texture("images/gym/messiDrinking.png");
        messiSad = new Texture("images/gym/messiSad.png");
        concentric = new Texture("images/gym/Concentric.png");
        eccentric = new Texture("images/gym/Eccentric.png");
        ronalSiu = new Texture("images/gym/ronalSiu.png");
    }

    public Texture getBackgroundGym() {
        return backgroundGym;
    }

    public Texture getMessiEating() {
        return messiEating;
    }

    public Texture getMessiDrinking() {
        return messiDrinking;
    }

    public Texture getMessiSad() {
        return messiSad;
    }

    public Texture getConcentric() {
        return concentric;
    }

    public Texture getEccentric() {
        return eccentric;
    }

    public Texture getRonalSiu() {
        return ronalSiu;
    }

    public void dispose() {
        if (backgroundGym != null) backgroundGym.dispose();
        if (messiEating != null) messiEating.dispose();
        if (messiDrinking != null) messiDrinking.dispose();
        if (messiSad != null) messiSad.dispose();
        if (concentric != null) concentric.dispose();
        if (eccentric != null) eccentric.dispose();
        if (ronalSiu != null) ronalSiu.dispose();
    }
}
