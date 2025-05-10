package myGame;

import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
// tage.rml.Vector4f;

public class backAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition, camDirection;
    private Vector4f fwdDirection;
    private Camera cam;
    private float maxDist = 7f;

    public backAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        av = game.getPerson();
        oldPosition = av.getWorldLocation();
        fwdDirection = new Vector4f(0f, 0f, -1f, 1f);
        fwdDirection.mul(av.getWorldRotation());
        fwdDirection.mul(0.1f);
        newPosition = oldPosition.add(fwdDirection.x(),
                fwdDirection.y(), fwdDirection.z());
        if (!game.isBlocked(newPosition))
            av.setLocalLocation(newPosition);    }
}
