package objects;

import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;

public class CeilingLight extends Group {

    private static final double PAN_RADIUS = 50;
    private static final double PAN_HEIGHT = 10;
    private static final double GLASS_RADIUS = PAN_RADIUS * 4 / 5;

    private final PointLight bulb;

    public CeilingLight() {
        // y axis 0 is not in the center of the whole light, it is at the top of pan's center
        Cylinder pan = new Cylinder(PAN_RADIUS, PAN_HEIGHT);
        pan.setTranslateY(PAN_HEIGHT / 2);
        pan.setMaterial(new PhongMaterial(Color.DARKSLATEBLUE));
        Sphere glass = new Sphere(GLASS_RADIUS);
        glass.setTranslateY(PAN_HEIGHT);
        glass.setScaleY(PAN_HEIGHT / GLASS_RADIUS);
        glass.setMaterial(new PhongMaterial(new Color(1, 1, 1, 0.5)));

        bulb = new PointLight(new Color(0.8, 0.8, 0.0, 1.0));
        bulb.setLightOn(false);
        bulb.setTranslateY(PAN_HEIGHT * 1.5);

        this.getChildren().addAll(pan, glass, bulb);
    }

    void toggle() {
        bulb.setLightOn(!bulb.isLightOn());
    }
}
