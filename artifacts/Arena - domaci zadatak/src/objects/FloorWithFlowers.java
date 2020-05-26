package objects;

import concepts.Vector;
import game.Game;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;

public class FloorWithFlowers extends FloorRegular {

    public static final Color DEFAULT_FLOOR_COLOR = Color.GREEN;
    public static final PhongMaterial FLOOR_MATERIAL = new PhongMaterial(DEFAULT_FLOOR_COLOR);
    public static final Image FLOOR_IMAGE = new Image("resources/grass.jpg");

    static {
        FLOOR_MATERIAL.setDiffuseMap(FLOOR_IMAGE);
    }

    protected static final int DEFAULT_FLOWERS_MIN = 5;
    protected static final int DEFAULT_FLOWERS_MAX = 20;

    public FloorWithFlowers(Vector position, double width, double length) {
        this(position, width, length, DEFAULT_FLOWERS_MIN, DEFAULT_FLOWERS_MAX);
    }
    
    public FloorWithFlowers(Vector position, double width, double length, int minFlowers, int maxFlowers) {
        super(position, width, length, FLOOR_MATERIAL);

        final int numOfFlowers = Game.RANDOM.nextInt(maxFlowers - minFlowers + 1) + minFlowers;
        for (int i = 0; i < numOfFlowers; i++) {
            Flower flower = new Flower(new Vector(-width / 2 + Game.RANDOM.nextDouble() * width, -Flower.STEM_HEIGHT / 2, -length / 2 + Game.RANDOM.nextDouble() * length));
            flower.getTransforms().add(new Rotate(Game.RANDOM.nextDouble() * 360, Rotate.Y_AXIS));
            this.getChildren().add(flower);
        }
    }

}
