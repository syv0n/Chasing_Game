package myGame;

import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class leftAction extends AbstractInputAction{
    private MyGame game;
    private GameObject av;
    private Camera cam;

    public leftAction(MyGame g)
    {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -.2 && keyValue < .2) return;
        av = game.getDol();
        av.yaw(-1f);
    }
}
