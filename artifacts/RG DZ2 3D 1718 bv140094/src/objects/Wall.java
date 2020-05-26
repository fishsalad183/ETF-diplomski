package objects;

import geometry.Vector;
//import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Wall extends GameObject {

    private final double width;
    private final double height;
    private final double depth;

    static final PhongMaterial WALL_MATERIAL = new PhongMaterial(Color.rgb(0xFB, 0xFB, 0xFB));
//    static final Image WALL_IMAGE = new Image("resources/wall.jpg");
//    static {
//        WALL_MATERIAL.setDiffuseMap(WALL_IMAGE);
//    }

    public Wall(Vector position, double width, double height, double depth) {
        super(position);
        Box wall = new Box(this.width = width, this.height = height, this.depth = depth);
        wall.setMaterial(WALL_MATERIAL);
        this.getChildren().add(wall);
        this.setTranslateX(position.getX());
        this.setTranslateY(position.getY() - height / 2);
        this.setTranslateZ(position.getZ());
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getDepth() {
        return depth;
    }
}
