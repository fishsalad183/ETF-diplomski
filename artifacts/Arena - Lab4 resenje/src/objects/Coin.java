package objects;

import geometry.Vector;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Coin extends GameObject {

    public static final double RADIUS = 50;
    public static final double THICKNESS = 15;

    private static final double ROTATION_TIME = 2;

    private static final PhongMaterial GOLD = new PhongMaterial(Color.GOLD);
    private static final PhongMaterial SILVER = new PhongMaterial(Color.SILVER);

    public Coin(Vector position) {
        super(position);
        
        Cylinder outer = new Cylinder(RADIUS, THICKNESS);
        outer.setMaterial(GOLD);
        Cylinder inner = new Cylinder(RADIUS * 9 / 10, THICKNESS * 1.125);
        inner.setMaterial(SILVER);
        Box deco1 = new Box(RADIUS / 2, THICKNESS * 1.25, RADIUS / 2);
        deco1.setMaterial(GOLD);
        Box deco2 = new Box(RADIUS / 2, THICKNESS * 1.25, RADIUS / 2);
        deco2.setMaterial(GOLD);
        deco2.getTransforms().add(new Rotate(45, new Point3D(0, 1, 0)));

        this.getChildren().addAll(outer, inner, deco1, deco2);
        
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());        

        this.getTransforms().add(new Rotate(90, 1, 0, 0));

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(ROTATION_TIME), this);
        rotateTransition.setAxis(new Point3D(0, 1, 0));
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.play();
    }

}
