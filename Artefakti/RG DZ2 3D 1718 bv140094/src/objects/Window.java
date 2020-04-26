package objects;

import geometry.Vector;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Window extends GameObject {

    private final double width;
    private final double height;
    private final double depth;
    private final double frame_width;

    public Window(Vector position, double w, double h, double d) {
        super(position);

        width = w;
        height = h;
        depth = d;
        frame_width = (width + height) / 2 / 30;

        Box right = new Box(frame_width, height, depth);
        right.setTranslateX(width / 2 - frame_width / 2);
        Box left = new Box(frame_width, height, depth);
        left.setTranslateX(-(width / 2 - frame_width / 2));
        Box top = new Box(width - 2 * frame_width, frame_width, depth);
        top.setTranslateY(-(height / 2 - frame_width / 2));
        Box bottom = new Box(width - 2 * frame_width, frame_width, depth);
        bottom.setTranslateY(height / 2 - frame_width / 2);

//        PhongMaterial frameMat = new PhongMaterial(Color.SIENNA);
        PhongMaterial frameMat = Door.FRAME_MATERIAL;
        right.setMaterial(frameMat);
        left.setMaterial(frameMat);
        top.setMaterial(frameMat);
        bottom.setMaterial(frameMat);

        Box glass = new Box(width - 2 * frame_width, height - 2 * frame_width, frame_width);
        PhongMaterial glassMat = new PhongMaterial(Color.rgb(0xF0, 0xF0, 0xFF, 0.15));
        glass.setMaterial(glassMat);

        this.getChildren().addAll(right, left, top, bottom, glass);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
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
