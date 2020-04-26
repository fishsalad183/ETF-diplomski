package objects;

import geometry.Vector;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

public class Table extends GameObject {

    public static final double WIDTH = 340;
    public static final double HEIGHT = 100;
    public static final double DEPTH = 220;

    private static final double PANEL_WIDTH = HEIGHT / 10;
    private static final double LEG_RADIUS = PANEL_WIDTH * 0.9;

    private static final PhongMaterial TABLE_MATERIAL = new PhongMaterial(Color.SIENNA);
    private static final Image TABLE_MATERIAL_IMAGE = new Image("resources/wood.jpg");

    static {
        TABLE_MATERIAL.setDiffuseMap(TABLE_MATERIAL_IMAGE);
    }

    public Table(Vector position) {
        super(position);

        Box table = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        table.setTranslateY(-HEIGHT / 2 + PANEL_WIDTH / 2);
        table.setMaterial(TABLE_MATERIAL);
        this.getChildren().add(table);

        PhongMaterial legMat = new PhongMaterial(Color.rgb(0xBE, 0x98, 0x67));
        for (int i = 0; i < 4; i++) {
            Cylinder leg = new Cylinder(LEG_RADIUS, HEIGHT - PANEL_WIDTH);
            leg.setTranslateY(PANEL_WIDTH / 2);
            leg.setTranslateX((WIDTH / 2 - 3 * LEG_RADIUS) * (i % 2 == 0 ? 1 : -1));
            leg.setTranslateZ((DEPTH / 2 - 2 * LEG_RADIUS) * (i < 2 ? 1 : -1));
            leg.setMaterial(legMat);
            this.getChildren().add(leg);
        }

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }
}
