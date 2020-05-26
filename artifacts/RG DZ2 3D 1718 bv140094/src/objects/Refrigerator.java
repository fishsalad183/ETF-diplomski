package objects;

import interfaces.SpecificallyBounded;
import interfaces.Interactive;
import geometry.Vector;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Group;
//import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Refrigerator extends GameObject implements Interactive, SpecificallyBounded {

    public static final double HEIGHT = 200;
    public static final double WIDTH = 120;
    public static final double DEPTH = 120;

    private static final double PANEL_WIDTH = WIDTH / 14;
    private static final double SHELF_WIDTH = HEIGHT / 100;

    private final Group body;
    private final Box door;
    private final Rotate doorRotate;
    private Timeline timeline;

    /*
    private final PointLight light;
     */
    public Refrigerator(Vector position) {
        super(position);

        Box bottom = new Box(WIDTH, PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        bottom.setTranslateY(HEIGHT / 2 - PANEL_WIDTH / 2);
        Box top = new Box(WIDTH, PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        top.setTranslateY(-(HEIGHT / 2 - PANEL_WIDTH / 2));
        Box left = new Box(PANEL_WIDTH, HEIGHT, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        Box right = new Box(PANEL_WIDTH, HEIGHT, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-(WIDTH / 2 - PANEL_WIDTH / 2));
        Box back = new Box(WIDTH, HEIGHT, PANEL_WIDTH);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);

        door = new Box(WIDTH, HEIGHT, PANEL_WIDTH);
        door.setTranslateZ(-(DEPTH / 2 - PANEL_WIDTH / 2));
        doorRotate = new Rotate(0, WIDTH / 2 - PANEL_WIDTH / 2, 0, 0, Rotate.Y_AXIS);
        door.getTransforms().add(doorRotate);

        PhongMaterial mat = new PhongMaterial(Color.WHITESMOKE);
        bottom.setMaterial(mat);
        top.setMaterial(mat);
        left.setMaterial(mat);
        right.setMaterial(mat);
        back.setMaterial(mat);
        PhongMaterial doorMat = new PhongMaterial(Color.SILVER);
        door.setMaterial(doorMat);

        Box shelf1 = new Box(WIDTH - 2 * PANEL_WIDTH, SHELF_WIDTH, DEPTH - 3 * PANEL_WIDTH);
        shelf1.setTranslateY(HEIGHT / 6);
        shelf1.setTranslateZ(PANEL_WIDTH);
        Box shelf2 = new Box(WIDTH - 2 * PANEL_WIDTH, SHELF_WIDTH, DEPTH - 3 * PANEL_WIDTH);
        shelf2.setTranslateY(-HEIGHT / 6);
        shelf2.setTranslateZ(PANEL_WIDTH);
        PhongMaterial shelfMat = new PhongMaterial();
        shelfMat.setDiffuseColor(new Color(0, 0, 0.3, 0.2));
        shelf1.setMaterial(shelfMat);
        shelf2.setMaterial(shelfMat);
        /*
        light = new PointLight(new Color(1, 1, 0, 0.2));
        light.setLightOn(false);
        light.setTranslateX(WIDTH / 2 - PANEL_WIDTH * 1.5);
        light.setTranslateY(-(HEIGHT / 2 - PANEL_WIDTH * 1.5));
         */
        body = new Group(bottom, top, left, right, back, shelf1, shelf2/*, light*/);
        this.getChildren().addAll(body, door);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), doorRotate.getAngle(), Interpolator.EASE_BOTH)/*,
                        new KeyValue(light.lightOnProperty(), light.isLightOn(), Interpolator.EASE_BOTH)*/),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), -90.0 - doorRotate.getAngle(), Interpolator.EASE_BOTH)/*,
                        new KeyValue(light.lightOnProperty(), !light.isLightOn(), Interpolator.EASE_BOTH)*/)
        );
        timeline.setOnFinished(e -> timeline.setRate(-timeline.getRate()));
    }

    @Override
    public void interact() {
        /*        if (timeline != null && timeline.getStatus() != Status.STOPPED)
            return;
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), doorRotate.getAngle(), Interpolator.EASE_BOTH),
                        new KeyValue(light.lightOnProperty(), light.isLightOn(), Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), -90.0 - doorRotate.getAngle(), Interpolator.EASE_BOTH),
                        new KeyValue(light.lightOnProperty(), !light.isLightOn(), Interpolator.EASE_BOTH))
        );
        timeline.play();*/

        if (timeline.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        timeline.play();
    }

    @Override
    public Bounds getSpecificBounds() {
        if (doorRotate.getAngle() == 0) {
            return this.getBoundsInParent();
        } else {
            return this.localToParent(body.getBoundsInParent());
        }
    }

}
