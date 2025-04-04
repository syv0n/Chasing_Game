package myGame;

import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class downAction extends AbstractInputAction{
    private MyGame game;
    private GameObject av;
    private Camera cam;

    public downAction(MyGame g)
    { game = g;
    }

    @Override
    public void performAction(float time, Event e)
    { float keyValue = e.getValue();
        if (keyValue > -.2 && keyValue < .2) return; // deadzone
        av = game.getDol();
        av.pitch(-1f);
    }
}
