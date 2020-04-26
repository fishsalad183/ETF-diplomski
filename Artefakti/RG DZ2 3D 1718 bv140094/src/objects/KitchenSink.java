package objects;

import geometry.Vector;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

public class KitchenSink extends GameObject {

    public static final double HEIGHT = 110;
    public static final double WIDTH = 300;
    public static final double DEPTH = 120;

    private static final double SINK_WIDTH = WIDTH / 3;
    private static final double SINK_HEIGHT = HEIGHT / 3;
    private static final double SINK_DEPTH = DEPTH / 1.8;
    private static final double SINK_FRAME = WIDTH / 80;
    private static final double PANEL_WIDTH = HEIGHT / 12;
    private static final double TAP_HEIGHT = SINK_HEIGHT / 1.4;
    private static final double TAP_RADIUS = SINK_WIDTH / 18;

    static final PhongMaterial TOP_MATERIAL = new PhongMaterial();
    static final Image TOP_MATERIAL_IMAGE = new Image("resources/marble.jpg");

    static {
        TOP_MATERIAL.setDiffuseMap(TOP_MATERIAL_IMAGE);
    }

    public KitchenSink(Vector position) {
        super(position);

        final double topBackDepth = (DEPTH - SINK_DEPTH - 2 * SINK_FRAME) * 2 / 3;
        Box topBack = new Box(WIDTH, PANEL_WIDTH, topBackDepth);
        topBack.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        topBack.setTranslateZ(DEPTH / 2 - topBackDepth / 2);
        final double topFrontDepth = topBackDepth / 2;
        Box topFront = new Box(WIDTH, PANEL_WIDTH, topFrontDepth);
        topFront.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        topFront.setTranslateZ(-DEPTH / 2 + topFrontDepth / 2);
        final double sideWidth = (WIDTH - SINK_WIDTH - 2 * SINK_FRAME) / 2;
        Box topLeft = new Box(sideWidth, PANEL_WIDTH, DEPTH - topBackDepth - topFrontDepth);
        topLeft.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        topLeft.setTranslateX(WIDTH / 2 - sideWidth / 2);
        topLeft.setTranslateZ(-DEPTH / 2 + topFrontDepth + topLeft.getDepth() / 2);
        Box topRight = new Box(sideWidth, PANEL_WIDTH, DEPTH - topBackDepth - topFrontDepth);
        topRight.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        topRight.setTranslateX(-(WIDTH / 2 - sideWidth / 2));
        topRight.setTranslateZ(-DEPTH / 2 + topFrontDepth + topRight.getDepth() / 2);

        topFront.setMaterial(TOP_MATERIAL);
        topBack.setMaterial(TOP_MATERIAL);
        topLeft.setMaterial(TOP_MATERIAL);
        topRight.setMaterial(TOP_MATERIAL);

        final double SINK_Z_ZERO = -DEPTH / 2 + topFrontDepth + SINK_FRAME + SINK_DEPTH / 2;
        Box sinkBottom = new Box(SINK_WIDTH + 2 * SINK_FRAME, SINK_FRAME, SINK_DEPTH + 2 * SINK_FRAME);
        sinkBottom.setTranslateY(-HEIGHT / 2 + SINK_HEIGHT + SINK_FRAME / 2);
        sinkBottom.setTranslateZ(SINK_Z_ZERO);
        Box sinkBack = new Box(SINK_WIDTH + 2 * SINK_FRAME, SINK_HEIGHT, SINK_FRAME);
        sinkBack.setTranslateY(-HEIGHT / 2 + SINK_HEIGHT / 2);
        sinkBack.setTranslateZ(SINK_Z_ZERO + SINK_DEPTH / 2 + SINK_FRAME / 2);
        Box sinkFront = new Box(SINK_WIDTH + 2 * SINK_FRAME, SINK_HEIGHT, SINK_FRAME);
        sinkFront.setTranslateY(-HEIGHT / 2 + SINK_HEIGHT / 2);
        sinkFront.setTranslateZ(SINK_Z_ZERO - SINK_DEPTH / 2 - SINK_FRAME / 2);
        Box sinkLeft = new Box(SINK_FRAME, SINK_HEIGHT, SINK_DEPTH);
        sinkLeft.setTranslateY(-HEIGHT / 2 + SINK_HEIGHT / 2);
        sinkLeft.setTranslateX(SINK_WIDTH / 2 + SINK_FRAME / 2);
        sinkLeft.setTranslateZ(SINK_Z_ZERO);
        Box sinkRight = new Box(SINK_FRAME, SINK_HEIGHT, SINK_DEPTH);
        sinkRight.setTranslateY(-HEIGHT / 2 + SINK_HEIGHT / 2);
        sinkRight.setTranslateX(-SINK_WIDTH / 2 - SINK_FRAME / 2);
        sinkRight.setTranslateZ(SINK_Z_ZERO);

        PhongMaterial sinkMat = new PhongMaterial(Color.SILVER);
        sinkBottom.setMaterial(sinkMat);
        sinkBack.setMaterial(sinkMat);
        sinkFront.setMaterial(sinkMat);
        sinkLeft.setMaterial(sinkMat);
        sinkRight.setMaterial(sinkMat);

        Box front = new Box(WIDTH, HEIGHT - PANEL_WIDTH, PANEL_WIDTH);
        front.setTranslateY(PANEL_WIDTH / 2);
        front.setTranslateZ(-DEPTH / 2 + PANEL_WIDTH / 2);
        Box back = new Box(WIDTH, HEIGHT - PANEL_WIDTH, PANEL_WIDTH);
        back.setTranslateY(PANEL_WIDTH / 2);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);
        Box left = new Box(PANEL_WIDTH, HEIGHT - PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateY(PANEL_WIDTH / 2);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        Box right = new Box(PANEL_WIDTH, HEIGHT - PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateY(PANEL_WIDTH / 2);
        right.setTranslateX(-WIDTH / 2 + PANEL_WIDTH / 2);

        PhongMaterial bodyMat = new PhongMaterial(new Color(1.0, 0.9, 0.9, 1.0));
        front.setMaterial(bodyMat);
        back.setMaterial(bodyMat);
        left.setMaterial(bodyMat);
        right.setMaterial(bodyMat);

        Group tap = createTap();
        tap.setTranslateY(-HEIGHT / 2 - TAP_HEIGHT / 2);
        tap.setTranslateZ(SINK_Z_ZERO + SINK_DEPTH / 2 + SINK_FRAME + TAP_RADIUS);

        this.getChildren().addAll(topBack, topFront, topLeft, topRight, front, back, left, right, sinkBottom, sinkBack, sinkFront, sinkLeft, sinkRight, tap);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    private Group createTap() {
        Box squareBase = new Box(TAP_RADIUS * 2.2, TAP_HEIGHT / 50, TAP_RADIUS * 2.2);
        squareBase.setTranslateY(TAP_HEIGHT / 2 - squareBase.getHeight() / 2);
        Cylinder base = new Cylinder(TAP_RADIUS, TAP_HEIGHT);
        Cylinder pipe = new Cylinder(TAP_RADIUS / 2, TAP_HEIGHT * 1.5);
        pipe.setRotationAxis(Rotate.X_AXIS);
        pipe.setRotate(90);
        pipe.setTranslateY(-base.getHeight() / 5);
        pipe.setTranslateZ(-pipe.getHeight() / 2 + TAP_RADIUS / 2);
        Cylinder pipeEnd = new Cylinder(pipe.getRadius(), pipe.getRadius() * 3.5);
        pipeEnd.setTranslateZ(-pipe.getHeight() + base.getRadius() / 2);
        pipeEnd.setTranslateY(pipe.getTranslateY() + pipeEnd.getHeight() / 5);

        PhongMaterial mat = new PhongMaterial(Color.GRAY);
        squareBase.setMaterial(mat);
        base.setMaterial(mat);
        pipe.setMaterial(mat);
        pipeEnd.setMaterial(mat);

        return new Group(squareBase, base, pipe, pipeEnd);
    }
}
