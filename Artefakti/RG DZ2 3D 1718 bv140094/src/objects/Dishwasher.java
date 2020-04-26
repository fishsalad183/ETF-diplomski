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
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Dishwasher extends GameObject implements Interactive, SpecificallyBounded {

    public static final double HEIGHT = 110;
    public static final double WIDTH = 120;
    public static final double DEPTH = 120;

    private static final double PANEL_WIDTH = WIDTH / 14;

    private final Group body;
    private final Box door;
    private final Rotate doorRotate;
    private Timeline timeline;

    public Dishwasher(Vector position) {
        super(position);

        Box bottom = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        bottom.setTranslateY(HEIGHT / 2 - PANEL_WIDTH / 2);
        Box top = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        top.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        Box left = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        Box right = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-WIDTH / 2 + PANEL_WIDTH / 2);
        Box back = new Box(WIDTH, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);

        PhongMaterial mat1 = new PhongMaterial(new Color(.88, .88, .88, 1.0));
        bottom.setMaterial(mat1);
        top.setMaterial(mat1);
        left.setMaterial(mat1);
        right.setMaterial(mat1);
        back.setMaterial(mat1);

        body = new Group(bottom, top, left, right, back);

        door = new Box(WIDTH, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        door.setTranslateZ(-DEPTH / 2 + PANEL_WIDTH / 2);
        doorRotate = new Rotate(0, 0, HEIGHT / 2 - PANEL_WIDTH * 3 / 2, 0, Rotate.X_AXIS);
        door.getTransforms().add(doorRotate);
        PhongMaterial doorMat = new PhongMaterial(new Color(.6, .6, .6, 1.0));
        door.setMaterial(doorMat);

        Group rack1 = createRack();
        rack1.setTranslateY(-HEIGHT / 6);
        Group rack2 = createRack();
        rack2.setTranslateY(HEIGHT / 6);

        this.getChildren().addAll(body, door, rack1, rack2);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), 90, Interpolator.EASE_BOTH))
        );
        timeline.setOnFinished(e -> timeline.setRate(-timeline.getRate()));
    }

    private Group createRack() {
        Group rack = new Group();
        final int numOfBars = 20;
        for (int i = 0; i < numOfBars; i++) {
            Cylinder bar = new Cylinder(HEIGHT / 110, WIDTH - 2 * PANEL_WIDTH);
            bar.setRotationAxis(Rotate.Z_AXIS);
            bar.setRotate(90);
            bar.setTranslateZ((DEPTH - 2 * PANEL_WIDTH) / (numOfBars + 1) * (numOfBars / 2 - i));
            rack.getChildren().add(bar);
        }
        Box left = new Box(HEIGHT / 80, PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH - left.getWidth() / 2);
        Box right = new Box(HEIGHT / 80, PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-(WIDTH / 2 - PANEL_WIDTH) + right.getWidth() / 2);
        rack.getChildren().addAll(left, right);
        return rack;
    }

    @Override
    public void interact() {
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
