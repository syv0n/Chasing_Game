package myGame;

import tage.Camera;
import tage.*;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
// tage.rml.Vector4f;

public class fwdAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Camera cam;
    private Vector3f oldPosition, newPosition, camDirection;
    private Vector4f fwdDirection;
    private float maxDist = 7f;
    private ProtocolClient protClient;

    public fwdAction(MyGame g, ProtocolClient p) {
        this.game = g;
        this.protClient = p;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -.2 && keyValue < .2) return; // deadzone
        av = game.getPerson();
        oldPosition = av.getWorldLocation();
        fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
        fwdDirection.mul(av.getWorldRotation());
        fwdDirection.mul(0.1f);
        newPosition = oldPosition.add(fwdDirection.x(),
                fwdDirection.y(), fwdDirection.z());
        av.setLocalLocation(newPosition);
        protClient.sendMoveMessage(av.getWorldLocation());
    }
}
