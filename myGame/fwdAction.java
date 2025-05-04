package myGame;

import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
// tage.rml.Vector4f;

public class fwdAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Camera cam;
    private Vector3f oldPosition, newPosition, camDirection, fwd;
    private Vector4f fwdDirection;
    private float maxDist = 7f;

    public fwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -.2 && keyValue < .2) return; // deadzone
        av = game.getDol();

        fwd = new Vector3f(0, 0, 1);

        Matrix4f worldTransform = av.getWorldRotation();
        Quaternionf rot = new Quaternionf();
        worldTransform.getUnnormalizedRotation(rot);
        rot.transform(fwd);
        fwd.normalize();
        fwd.mul(3f);

        av.getPhysicsObject().applyForce(fwd.x(), fwd.y(), fwd.z(), 0f, 0f, 0f);

    }
}
