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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Oven extends GameObject implements Interactive, SpecificallyBounded {

    public static final double HEIGHT = 110;
    public static final double WIDTH = 120;
    public static final double DEPTH = 120;

    private static final double PANEL_WIDTH = WIDTH / 14;
    private static final double BASE_HEIGHT = PANEL_WIDTH * 3;
    private static final double STOVE_RADIUS = WIDTH / 9;
    private static final double STOVE_HEIGHT = HEIGHT / 24;

    private static final PhongMaterial MATERIAL = new PhongMaterial(/*new Color(.6, .6, .6, 1.0)*/);
    private static final Image MATERIAL_IMAGE = new Image("resources/whiteMetal.jpg");

    static {
        MATERIAL.setDiffuseMap(MATERIAL_IMAGE);
    }

    private final Group body;
    private final Box door;
    private final Rotate doorRotate;
    private Timeline timeline;

    public Oven(Vector position) {
        super(position);

        Box bottom = new Box(WIDTH, BASE_HEIGHT, DEPTH);
        bottom.setTranslateY(HEIGHT / 2 - BASE_HEIGHT / 2);
        Box top = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        top.setTranslateY(-(HEIGHT / 2 - PANEL_WIDTH / 2));
        Box left = new Box(PANEL_WIDTH, HEIGHT - BASE_HEIGHT - PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        left.setTranslateY(-HEIGHT / 2 + left.getHeight() / 2 + PANEL_WIDTH);
        Box right = new Box(PANEL_WIDTH, HEIGHT - BASE_HEIGHT - PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-(WIDTH / 2 - PANEL_WIDTH / 2));
        right.setTranslateY(-HEIGHT / 2 + right.getHeight() / 2 + PANEL_WIDTH);
        Box back = new Box(WIDTH, HEIGHT - BASE_HEIGHT - PANEL_WIDTH, PANEL_WIDTH);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);
        back.setTranslateY(-HEIGHT / 2 + back.getHeight() / 2 + PANEL_WIDTH);

        bottom.setMaterial(MATERIAL);
        top.setMaterial(MATERIAL);
        PhongMaterial mat2 = new PhongMaterial(Color.SILVER);
        left.setMaterial(mat2);
        right.setMaterial(mat2);
        back.setMaterial(mat2);

        body = new Group(bottom, top, left, right, back);

        PhongMaterial stoveMat = new PhongMaterial(new Color(.3, .3, .3, 1.0));
        for (int i = 0; i < 4; i++) {
            Cylinder stove = new Cylinder(STOVE_RADIUS, STOVE_HEIGHT);
            stove.setTranslateY(-HEIGHT / 2 - STOVE_HEIGHT / 2);
            stove.setTranslateX(WIDTH / 4 * (i % 2 == 0 ? 1 : -1));
            stove.setTranslateZ(DEPTH / 4 * (i < 2 ? 1 : -1));
            stove.setMaterial(stoveMat);
            body.getChildren().add(stove);
        }

        door = new Box(WIDTH, HEIGHT - BASE_HEIGHT - PANEL_WIDTH, PANEL_WIDTH);
        door.setTranslateZ(-(DEPTH / 2 - PANEL_WIDTH / 2));
        door.setTranslateY(-HEIGHT / 2 + door.getHeight() / 2 + PANEL_WIDTH);
        doorRotate = new Rotate(0, 0, HEIGHT / 2 - BASE_HEIGHT + PANEL_WIDTH / 2, 0, Rotate.X_AXIS);
        door.getTransforms().add(doorRotate);
        PhongMaterial doorMat = new PhongMaterial(new Color(.1, .1, .1, .6));
        door.setMaterial(doorMat);

        this.getChildren().addAll(body, door);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), 90, Interpolator.EASE_BOTH))
        );
        timeline.setOnFinished(e -> timeline.setRate(-timeline.getRate()));
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
