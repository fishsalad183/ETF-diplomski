package objects;

import geometry.Vector;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Countertop extends GameObject {

    public static final double WIDTH = 120;
    public static final double HEIGHT = 110;
    public static final double DEPTH = 120;

    private static final double PANEL_WIDTH = WIDTH / 20;

    static final PhongMaterial TOP_MATERIAL = new PhongMaterial(Color.GRAY);
    static final PhongMaterial REST_MATERIAL = new PhongMaterial(Color.LIGHTGRAY);
    static final Image TOP_MATERIAL_IMAGE = new Image("resources/marble2.jpg");
    static final Image REST_MATERIAL_IMAGE = new Image("resources/marble3.jpg");

    static {
        TOP_MATERIAL.setDiffuseMap(TOP_MATERIAL_IMAGE);
        REST_MATERIAL.setDiffuseMap(REST_MATERIAL_IMAGE);
    }

    public Countertop(Vector position) {
        super(position);

        Box top = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        top.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        Box bottom = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        bottom.setTranslateY(HEIGHT / 2 - PANEL_WIDTH / 2);
        Box left = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        left.setTranslateX(WIDTH / 2 - PANEL_WIDTH / 2);
        Box right = new Box(PANEL_WIDTH, HEIGHT - 2 * PANEL_WIDTH, DEPTH - 2 * PANEL_WIDTH);
        right.setTranslateX(-WIDTH / 2 + PANEL_WIDTH / 2);
        Box front = new Box(WIDTH, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        front.setTranslateZ(-DEPTH / 2 + PANEL_WIDTH / 2);
        Box back = new Box(WIDTH, HEIGHT - 2 * PANEL_WIDTH, PANEL_WIDTH);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);

        top.setMaterial(TOP_MATERIAL);
        bottom.setMaterial(REST_MATERIAL);
        left.setMaterial(REST_MATERIAL);
        right.setMaterial(REST_MATERIAL);
        front.setMaterial(REST_MATERIAL);
        back.setMaterial(REST_MATERIAL);

        this.getChildren().addAll(top, bottom, left, right, front, back);
        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    public static PhongMaterial getTopMaterial() {
        return TOP_MATERIAL;
    }
}
