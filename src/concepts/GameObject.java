package concepts;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;

public abstract class GameObject extends Group {

    private Vector position;

    public GameObject(Vector position) {
        this.position = position;

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public static List<Node> getAllSubnodes(Parent root) {
        List<Node> nodes = new ArrayList<>();
        addAllDescendants(root, nodes);
        return nodes;
    }

    private static void addAllDescendants(Parent parent, List<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent) {
                addAllDescendants((Parent) node, nodes);
            }
        }
    }

}
