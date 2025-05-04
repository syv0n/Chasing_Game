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

        Vector3f locTor = new Vector3f(0f, 5f, 0f);

        Matrix4f rotMatrix = av.getWorldRotation();

        Quaternionf orientation = new Quaternionf().setFromNormalized(rotMatrix);

        Vector3f worldTorque = orientation.transform(locTor);

        av.getPhysicsObject().applyTorque(worldTorque.x(), worldTorque.y(), worldTorque.z());
    }
}
