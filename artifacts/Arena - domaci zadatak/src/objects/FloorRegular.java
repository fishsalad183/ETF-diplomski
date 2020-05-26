package objects;

import concepts.Vector;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;

public class FloorRegular extends Floor {

    public FloorRegular(Vector position, double width, double length, Material material) {
        super(position);
        
        Box floor = new Box(width, FLOOR_HEIGHT, length);
        floor.setMaterial(material);
        this.getChildren().add(floor);
    }

}
