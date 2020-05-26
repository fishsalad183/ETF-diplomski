package objects;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class FloorCeiling extends Group {

    private static final PhongMaterial FLOOR_MATERIAL = new PhongMaterial(Color.BURLYWOOD);
    private static final PhongMaterial CEILING_MATERIAL = new PhongMaterial(Color.IVORY);
    private static final PhongMaterial GROUND_MATERIAL = new PhongMaterial(Color.GREEN);
    private static final Image FLOOR_IMAGE = new Image("resources/floor.jpg");
    private static final Image CEILING_IMAGE = new Image("resources/wall.jpg");
    private static final Image GROUND_IMAGE = new Image("resources/grass.jpg");

    static {
        FLOOR_MATERIAL.setDiffuseMap(FLOOR_IMAGE);
        CEILING_MATERIAL.setDiffuseMap(CEILING_IMAGE);
        GROUND_MATERIAL.setDiffuseMap(GROUND_IMAGE);
    }

    public enum Type {
        FLOOR, CEILING, GROUND;
    };

    private final Box box;

    public static final double FLOOR_HEIGHT = 1;

    public FloorCeiling(double width, double length, Type type) {
        box = new Box(width, FLOOR_HEIGHT, length);
        switch (type) {
            case FLOOR:
                box.setMaterial(FLOOR_MATERIAL);
                break;
            case CEILING:
                box.setMaterial(CEILING_MATERIAL);
                break;
            case GROUND:
                box.setMaterial(GROUND_MATERIAL);
                break;
        }
        this.getChildren().add(box);
    }
}
