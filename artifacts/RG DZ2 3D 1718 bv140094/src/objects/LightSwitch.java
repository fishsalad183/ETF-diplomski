package objects;

import interfaces.Interactive;
import geometry.Vector;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class LightSwitch extends GameObject implements Interactive {

    private static final double HEIGHT = 20;
    private static final double WIDTH = 10;
    private static final double DEPTH = 5;

    private final CeilingLight light;

    // 0 on Z axis is not in the center, but on the front part of the casing
    public LightSwitch(Vector position, CeilingLight l) {
        super(position);

        light = l;

        Box casing = new Box(WIDTH, HEIGHT, DEPTH);
        casing.setTranslateZ(-DEPTH / 2);
        casing.setMaterial(new PhongMaterial(Color.LIGHTYELLOW));
        Box toggle = new Box(WIDTH / 3, HEIGHT / 2, DEPTH / 2);
        toggle.setTranslateZ(-DEPTH - toggle.getDepth() / 2);
        toggle.setMaterial(new PhongMaterial(Color.NAVAJOWHITE));

        this.getChildren().addAll(casing, toggle);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    @Override
    public void interact() {
        light.toggle();
    }
}
