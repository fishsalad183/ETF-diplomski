package objects;

import geometry.Vector;
import javafx.scene.Group;

public class GameObject extends Group {

    protected Vector position;

    public GameObject(Vector position) {
        this.position = position;
    }

    public GameObject() {
        position = new Vector(0, 0, 0);
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }
}
