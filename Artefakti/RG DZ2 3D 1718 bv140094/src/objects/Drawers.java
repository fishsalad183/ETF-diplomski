package objects;

import interfaces.SpecificallyBounded;
import interfaces.Interactive;
import geometry.Vector;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Drawers extends GameObject implements Interactive, SpecificallyBounded {

    public static final double WIDTH = 100;
    public static final double HEIGHT = 80;
    public static final double DEPTH = 60;

    private static final double PANEL_WIDTH = WIDTH / 20;

    private final Group body;
    private final Group[] drawers;
    private Timeline[] timelines;
    private int drawnIndex = 0;

    public Drawers(Vector position, int numOfDrawers) {
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

        // DO NOT CHANGE THE ORDER OF NODES IN body!
        body = new Group(top, bottom, left, right, back);

        drawers = createDrawersAndAnimations(numOfDrawers);

        this.getChildren().add(body);
        this.getChildren().addAll(drawers);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    private Group[] createDrawersAndAnimations(int numOfDrawers) {
        timelines = new Timeline[numOfDrawers];
        Group[] d = new Group[numOfDrawers];

        final double drawerHeight = (HEIGHT - 2 * PANEL_WIDTH) / numOfDrawers;
        final double startY = -HEIGHT / 2 + PANEL_WIDTH + drawerHeight / 2;
        for (int i = 0; i < numOfDrawers; i++) {
            d[i] = createDrawer(drawerHeight);
            d[i].setTranslateZ(-PANEL_WIDTH / 2);
            d[i].setTranslateY(startY + i * drawerHeight);

            Translate translate = new Translate();
            d[i].getTransforms().add(translate);
            timelines[i] = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(translate.zProperty(), 0, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(translate.zProperty(), -3. / 4. * DEPTH, Interpolator.EASE_BOTH))
            );
            final int index = i;
            timelines[index].setOnFinished(e -> timelines[index].setRate(-timelines[index].getRate()));
        }

        return d;
    }

    private Group createDrawer(double drawerHeight) {
        final double DRAWER_WIDTH = WIDTH - 2 * PANEL_WIDTH;
        final double DRAWER_FRONT_WIDTH = WIDTH;
        final double DRAWER_DEPTH = DEPTH - PANEL_WIDTH;
        Box back = new Box(DRAWER_WIDTH, drawerHeight, PANEL_WIDTH);
        back.setTranslateZ(DRAWER_DEPTH / 2 - PANEL_WIDTH / 2);
        Box left = new Box(PANEL_WIDTH, drawerHeight * 3 / 4, DRAWER_DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(DRAWER_WIDTH / 2 - PANEL_WIDTH / 2);
        left.setTranslateY(drawerHeight / 8);
        Box right = new Box(PANEL_WIDTH, drawerHeight * 3 / 4, DRAWER_DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-DRAWER_WIDTH / 2 + PANEL_WIDTH / 2);
        right.setTranslateY(drawerHeight / 8);
        Box bottom = new Box(DRAWER_WIDTH - 2 * PANEL_WIDTH, PANEL_WIDTH, DRAWER_DEPTH - 2 * PANEL_WIDTH);
        bottom.setTranslateY(drawerHeight / 2 - PANEL_WIDTH / 2);
        Box front = new Box(DRAWER_FRONT_WIDTH, drawerHeight, PANEL_WIDTH);
        front.setTranslateZ(-DRAWER_DEPTH / 2 + PANEL_WIDTH / 2);

        PhongMaterial mat = new PhongMaterial(Color.SIENNA);
        back.setMaterial(mat);
        left.setMaterial(mat);
        right.setMaterial(mat);
        bottom.setMaterial(mat);
        front.setMaterial(mat);

        // do not change order of nodes in group
        final Group drawer = new Group(front, back, left, right, bottom);
        drawer.setScaleY(0.98);
        return drawer;
    }

    public void setDrawerFrontMaterial(PhongMaterial mat) {
        for (Group drawer : drawers) {
            // element with index 0 is drawer front
            ((Box) drawer.getChildren().get(0)).setMaterial(mat);
        }
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

    @Override
    public void interact() {
        Timeline timeline = timelines[drawnIndex];
        if (timeline.getStatus() != Status.STOPPED) {
            return;
        }
        timeline.play();
        if (timeline.getRate() < 0) {   // change the drawer that is being drawn after drawing in previous one
            drawnIndex = (drawnIndex + 1) % drawers.length;
        }
    }

    @Override
    public Bounds getSpecificBounds() {
        return this.localToParent(body.getBoundsInParent());
    }
}
