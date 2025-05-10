package myGame;
import tage.*;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import org.joml.*;
import tage.shapes.*;

public class InputController extends AbstractInputAction {

    private MyGame game;
    private String actionType;
    private GameObject person;
    private Camera cam;
    private Vector3f oldPosition, newPosition, camDirection;
    private Vector4f fwdDirection;
    private float turnSpeed = 3f;
    private float moveSpeed = 0.15f;
    private AnimatedShape personS;
    private ProtocolClient protClient;

    public InputController(MyGame game, String actionType, ProtocolClient p) {
        this.game = game;
        this.actionType = actionType;
        this.protClient = p;
    }

    @Override
    public void performAction(float time, net.java.games.input.Event evt) {
        float value = evt.getValue();
        person = game.getPerson();
        // if (value < 0.2f) return;

        switch (actionType) {
            case "move":
                if (value > -.2 && value < .2) {
                    return;
                }
                oldPosition = person.getWorldLocation();
                fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
                fwdDirection.mul(person.getWorldRotation());
                fwdDirection.mul(-value * moveSpeed);

                newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
                person.setLocalLocation(newPosition);
                protClient.sendMoveMessage(person.getWorldLocation());

                break;
            case "turn":
                float turnAmount = value;
                person.yaw(turnAmount * 1.2f);
                break;
            case "wave":
                personS = game.getPersonAnimatedShape();
                personS.playAnimation("WAVE", 0.5f, AnimatedShape.EndType.STOP, 0);
                break;
        }
    }
    
}
