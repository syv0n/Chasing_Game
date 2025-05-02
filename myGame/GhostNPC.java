package myGame;
import java.util.UUID;

import tage.*;
import org.joml.*;

public class GhostNPC extends GameObject {
    private int id;

    public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p)
    {
        super(GameObject.root(), s, t);
        this.id = id;
        this.setLocalLocation(p);
    }

    public void setPosition(Vector3f position) {
        Vector3f newPosition = position.add(0, 0.5f, 0);
        this.setLocalLocation(newPosition);
    }

    public void setSize(boolean big)
    {
        if (!big) {
            this.setLocalScale(new Matrix4f().scaling(2.0f));
        } else {
            this.setLocalScale(new Matrix4f().scaling(3.0f));
        }
    }
}
