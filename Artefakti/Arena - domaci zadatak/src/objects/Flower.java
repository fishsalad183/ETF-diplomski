package objects;

import concepts.GameObject;
import concepts.Vector;
import game.Game;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class Flower extends GameObject {
    
    public static final double STEM_HEIGHT = 50;
    
    private static final int MIN_PETALS = 4;
    private static final int MAX_PETALS = 6;
    
    public Flower(Vector position) {
        super(position);
        
        Cylinder stem = new Cylinder(STEM_HEIGHT / 20, STEM_HEIGHT);
        stem.setMaterial(new PhongMaterial(new Color(0, 0.2 + Game.RANDOM.nextDouble() * 0.7, 0, 1)));
        Sphere center = new Sphere(STEM_HEIGHT / 5);
        center.setMaterial(new PhongMaterial(new Color(Game.RANDOM.nextDouble(), Game.RANDOM.nextDouble(), Game.RANDOM.nextDouble(), 1)));
        center.setTranslateY(-stem.getHeight() / 2 - center.getRadius() / 2);
        this.getChildren().addAll(stem, center);
        
        final int numOfPetals = MIN_PETALS + (int) (Game.RANDOM.nextDouble() * (MAX_PETALS - MIN_PETALS + 1));
        final double angleStep = (360.0 / numOfPetals) * Math.PI / 180;
        double angle = Game.RANDOM.nextDouble() * angleStep;
        final PhongMaterial petalMaterial = new PhongMaterial(new Color(Game.RANDOM.nextDouble(), Game.RANDOM.nextDouble(), Game.RANDOM.nextDouble(), 1));
        for (int i = 0; i < numOfPetals; i++) {
            Cylinder petal = new Cylinder(center.getRadius(), center.getRadius() / 1.3);
            petal.setRotationAxis(Rotate.X_AXIS);
            petal.setRotate(90);
            petal.setTranslateX(center.getRadius() * Math.sin(angle));
            petal.setTranslateY(center.getTranslateY() + -center.getRadius() * Math.cos(angle));
            petal.setMaterial(petalMaterial);
            this.getChildren().add(petal);
            angle += angleStep;
        }
    }
    
}
