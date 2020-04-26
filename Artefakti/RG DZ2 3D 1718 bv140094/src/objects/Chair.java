package objects;

import geometry.Vector;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Chair extends GameObject {

    public static final double WIDTH = 60;
    public static final double HEIGHT = 140;
    public static final double DEPTH = 60;

    private static final double PANEL_WIDTH = 10;

    private static final PhongMaterial CHAIR_MATERIAL = new PhongMaterial(Color.SIENNA);
    private static final PhongMaterial LEG_MATERIAL = new PhongMaterial(Color.rgb(0xBE, 0x98, 0x67));
    private static final Image CHAIR_MATERIAL_IMAGE = new Image("resources/chairFabric.jpg");
    private static final Image LEG_MATERIAL_IMAGE = new Image("resources/wood3.jpg");

    static {
        CHAIR_MATERIAL.setDiffuseMap(CHAIR_MATERIAL_IMAGE);
        LEG_MATERIAL.setDiffuseMap(LEG_MATERIAL_IMAGE);
    }

    public Chair(Vector position) {
        super(position);

        final double seatY = HEIGHT / 7;

        Box seat = new Box(WIDTH, PANEL_WIDTH, DEPTH);
        seat.setTranslateY(seatY);
        seat.setMaterial(CHAIR_MATERIAL);

        final double backHeight = HEIGHT / 2 + seatY - PANEL_WIDTH / 2;

        Box back = new Box(WIDTH, backHeight, PANEL_WIDTH);
        back.setTranslateY(seatY - backHeight / 2 - PANEL_WIDTH / 2);
        back.setTranslateZ(DEPTH / 2 - PANEL_WIDTH / 2);
        back.setMaterial(CHAIR_MATERIAL);

        this.getChildren().addAll(seat, back);

        final double legHeight = HEIGHT - backHeight - PANEL_WIDTH;
        for (int i = 0; i < 4; i++) {
            Box leg = new Box(PANEL_WIDTH, legHeight, PANEL_WIDTH);
            leg.setTranslateY(seatY + legHeight / 2 + PANEL_WIDTH / 2);
            leg.setTranslateX((WIDTH / 2 - PANEL_WIDTH / 2) * (i % 2 == 0 ? 1 : -1));
            leg.setTranslateZ((DEPTH / 2 - PANEL_WIDTH / 2) * (i < 2 ? 1 : -1));
            leg.setMaterial(LEG_MATERIAL);
            this.getChildren().add(leg);
        }

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }
}
