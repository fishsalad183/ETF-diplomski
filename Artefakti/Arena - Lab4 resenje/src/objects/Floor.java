package objects;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Floor extends Box {

    public static final double FLOOR_HEIGHT = 1;

    private static final Color DEFAULT_FLOOR_COLOR = Color.GREEN;
    private static final PhongMaterial FLOOR_MATERIAL = new PhongMaterial(DEFAULT_FLOOR_COLOR);
    private static final Image FLOOR_IMAGE = new Image("resources/grass.jpg");
    static {
        FLOOR_MATERIAL.setDiffuseMap(FLOOR_IMAGE);
    }

    public Floor(double width, double length) {
        super(width, FLOOR_HEIGHT, length);
        this.setMaterial(FLOOR_MATERIAL);
    }
    
}
