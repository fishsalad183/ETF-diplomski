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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Door extends GameObject implements Interactive, SpecificallyBounded {

    public static final double WIDTH = 180;
    public static final double HEIGHT = 240;
    public static final double DEPTH = 70;

    private static final double FRAME_WIDTH = WIDTH / 40;

    static final PhongMaterial FRAME_MATERIAL = new PhongMaterial(Color.SIENNA);
    static final Image FRAME_MATERIAL_IMAGE = new Image("resources/wood2.jpg");

    static {
        FRAME_MATERIAL.setDiffuseMap(FRAME_MATERIAL_IMAGE);
    }

    private final Group door;
    private final Rotate doorRotate;
    private Timeline timeline;

    public Door(Vector position) {
        super(position);

        Box right = new Box(FRAME_WIDTH, HEIGHT, DEPTH);
        right.setTranslateX(WIDTH / 2 - FRAME_WIDTH / 2);
        Box left = new Box(FRAME_WIDTH, HEIGHT, DEPTH);
        left.setTranslateX(-(WIDTH / 2 - FRAME_WIDTH / 2));
        Box top = new Box(WIDTH - 2 * FRAME_WIDTH, FRAME_WIDTH, DEPTH);
        top.setTranslateY(-(HEIGHT / 2 - FRAME_WIDTH / 2));

        right.setMaterial(FRAME_MATERIAL);
        left.setMaterial(FRAME_MATERIAL);
        top.setMaterial(FRAME_MATERIAL);

        Box d = new Box(WIDTH - FRAME_WIDTH * 2, HEIGHT - FRAME_WIDTH, FRAME_WIDTH);
        PhongMaterial doorMat = new PhongMaterial(Color.BURLYWOOD);
        d.setMaterial(doorMat);
        Group handles = createHandleset(d.getDepth());
        handles.setTranslateX(-d.getWidth() / 2 + handles.getBoundsInLocal().getWidth() / 2);

        door = new Group(d, handles);
        door.setTranslateZ(-(DEPTH / 2 - FRAME_WIDTH / 2));
        door.setTranslateY(FRAME_WIDTH / 2);
        doorRotate = new Rotate(0, WIDTH / 2 - FRAME_WIDTH / 2, 0, 0, Rotate.Y_AXIS);
        door.getTransforms().add(doorRotate);

        this.getChildren().addAll(right, left, top, door);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), doorRotate.getAngle(), Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), -90.0 - doorRotate.getAngle(), Interpolator.EASE_BOTH))
        );
        timeline.setOnFinished(e -> timeline.setRate(-timeline.getRate()));
    }

    private Group createHandleset(final double doorPanelDepth) {
        Box handleZPart = new Box(WIDTH / 60, HEIGHT / 100, doorPanelDepth * 3);
        Box handleset = new Box(WIDTH / 30, HEIGHT / 10, doorPanelDepth * 1.2);
        Box frontHandle = new Box(WIDTH / 10, HEIGHT / 100, DEPTH / 16);
        Box backHandle = new Box(WIDTH / 10, HEIGHT / 100, DEPTH / 16);
        final double frontHandleZ = -handleZPart.getDepth() / 2 - frontHandle.getDepth() / 2;
        final double handleX = frontHandle.getWidth() / 2 - handleZPart.getWidth() / 2;
        frontHandle.setTranslateZ(frontHandleZ);
        frontHandle.setTranslateX(handleX);
        backHandle.setTranslateZ(-frontHandleZ);
        backHandle.setTranslateX(handleX);

        PhongMaterial handleMat = new PhongMaterial(Color.DARKGRAY);
        handleset.setMaterial(handleMat);
        handleZPart.setMaterial(handleMat);
        frontHandle.setMaterial(handleMat);
        backHandle.setMaterial(handleMat);

        return new Group(handleset, handleZPart, frontHandle, backHandle);
    }

    @Override
    public void interact() {
        /*if (timeline != null && timeline.getStatus() != Animation.Status.STOPPED)
            return;
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(doorRotate.angleProperty(), doorRotate.getAngle(), Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(doorRotate.angleProperty(), -90.0 - doorRotate.getAngle(), Interpolator.EASE_BOTH))
        );
        timeline.play();*/

        if (timeline.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        timeline.play();
    }

    @Override
    public Bounds getSpecificBounds() {
//        if (doorRotate.getAngle() == 0) {
//            return this.getBoundsInParent();
//        }
//        else {
//            return this.localToParent(door.getBoundsInParent());
//        }

        return this.localToParent(door.getBoundsInParent());
    }
}
