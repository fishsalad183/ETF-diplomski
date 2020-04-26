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
//import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class Cabinet extends GameObject implements Interactive, SpecificallyBounded {

    public static final double WIDTH = 100;
    public static final double HEIGHT = 80;
    public static final double DEPTH = 80;

    private static final double PANEL_WIDTH = WIDTH / 20;

//    static final PhongMaterial MATERIAL = new PhongMaterial(Color.rgb(0xFB, 0xFB, 0xFB));
//    static final Image IMAGE = new Image("resources/wood3.jpg");
//    static {
//        MATERIAL.setDiffuseMap(IMAGE);
//    }
    private final Group body;
    private final Box[] shelves;
    private final Box[] doors = new Box[2];
    private final Timeline timeline;

    public Cabinet(Vector position, int numOfShelves) {
        super(position);

        Box top = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        top.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        Box bottom = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        bottom.setTranslateY(HEIGHT / 2 - PANEL_WIDTH / 2);
        Box left = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        Box right = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-WIDTH / 2 + PANEL_WIDTH / 2);
        Box back = new Box(WIDTH, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);

        PhongMaterial bodyMat = new PhongMaterial(Color.PERU);
        top.setMaterial(bodyMat);
        bottom.setMaterial(bodyMat);
        left.setMaterial(bodyMat);
        right.setMaterial(bodyMat);
        back.setMaterial(bodyMat);

        // do not change order of nodes in body
        body = new Group(top, bottom, left, right, back);

        PhongMaterial shelfMaterial = new PhongMaterial(Color.BROWN);
        shelves = new Box[numOfShelves];
        final double stepY = (HEIGHT - 2 * PANEL_WIDTH) / (numOfShelves + 1);
        double y = HEIGHT / 2 - PANEL_WIDTH / 2 - stepY;
        for (int i = 0; i < numOfShelves; i++) {
            shelves[i] = new Box(WIDTH - 2 * PANEL_WIDTH, PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
            shelves[i].setTranslateY(y);
            y -= stepY;
            shelves[i].setMaterial(shelfMaterial);
        }
        body.getChildren().addAll(shelves);

        Box leftDoor = new Box(WIDTH / 2, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        leftDoor.setTranslateZ(-DEPTH / 2 + PANEL_WIDTH / 2);
        leftDoor.setTranslateX(WIDTH / 4);
        doors[0] = leftDoor;
        Box rightDoor = new Box(WIDTH / 2, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        rightDoor.setTranslateZ(-DEPTH / 2 + PANEL_WIDTH / 2);
        rightDoor.setTranslateX(-WIDTH / 4);
        doors[1] = rightDoor;

        Scale scale = new Scale(0.99, 0.99, 0.99);
        Rotate leftDoorRotate = new Rotate(0, WIDTH / 4 - PANEL_WIDTH / 2, 0, 0, Rotate.Y_AXIS);
        leftDoor.getTransforms().addAll(leftDoorRotate, scale);
        Rotate rightDoorRotate = new Rotate(0, -WIDTH / 4 + PANEL_WIDTH / 2, 0, 0, Rotate.Y_AXIS);
        rightDoor.getTransforms().addAll(rightDoorRotate, scale);

        PhongMaterial doorMaterial = new PhongMaterial(Color.SIENNA);
        leftDoor.setMaterial(doorMaterial);
        rightDoor.setMaterial(doorMaterial);
//        leftDoor.setMaterial(MATERIAL);
//        leftDoor.setMaterial(MATERIAL);

        this.getChildren().addAll(body, leftDoor, rightDoor);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(leftDoorRotate.angleProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(rightDoorRotate.angleProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1), new KeyValue(leftDoorRotate.angleProperty(), -90, Interpolator.EASE_BOTH),
                        new KeyValue(rightDoorRotate.angleProperty(), 90, Interpolator.EASE_BOTH))
        );
        timeline.setOnFinished(e -> timeline.setRate(-timeline.getRate()));
    }

    public void setDoorsMaterial(PhongMaterial mat) {
        doors[0].setMaterial(mat);
        doors[1].setMaterial(mat);
    }

    public void setDrawersTopAndBottomMaterial(PhongMaterial mat) {
        for (int i = 0; i <= 1; i++) {
            // elements with indexes 0 and 1 are top and bottom
            ((Box) body.getChildren().get(i)).setMaterial(mat);
        }
    }

    public void setDrawersLeftRightAndBackMaterial(PhongMaterial mat) {
        for (int i = 2; i <= 4; i++) {
            // elements with indexes 2, 3 and 4 are left, right and back
            ((Box) body.getChildren().get(i)).setMaterial(mat);
        }
    }

    public void setShelfMaterial(PhongMaterial mat) {
        for (Box shelf : shelves) {
            shelf.setMaterial(mat);
        }
    }

    public double getShelfTopY(int shelfIndex, double yScale) {
        if (shelfIndex == -1) {
            return getTopOfBottomPart(yScale);
        }
        return this.localToParent(shelves[shelfIndex].getBoundsInParent()).getMinY() - (yScale - 1) * (PANEL_WIDTH / 2);
    }

    public double getTopOfBottomPart(double yScale) {
        return this.getBoundsInParent().getMaxY() - PANEL_WIDTH * yScale;
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
        return this.localToParent(body.getBoundsInParent());
    }
}
