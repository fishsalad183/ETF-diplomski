package objects;

import geometry.Vector;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.util.Duration;

public class Spikes extends GameObject {

    public static final double SPIKE_RADIUS = 120;
    public static final double SPIKE_HEIGHT = SPIKE_RADIUS * 2;

    // for transition durations
    public static final double COME_UP_TIME = 0.25;
    public static final double STAY_UP_TIME = 0.75;
    public static final double COME_DOWN_TIME = 0.25;

    // for timeline Duration instances, in seconds
    public static final double COME_UP_MOMENT = COME_UP_TIME;
    public static final double STAY_UP_MOMENT = COME_UP_MOMENT + STAY_UP_TIME;
    public static final double COME_DOWN_MOMENT = STAY_UP_MOMENT + COME_DOWN_TIME;

    private static final PhongMaterial SPIKE_MATERIAL = new PhongMaterial(Color.DARKGRAY);
    private static final Image SPIKE_IMAGE = new Image("resources/metal.jpg");
    static {
        SPIKE_MATERIAL.setDiffuseMap(SPIKE_IMAGE);
    }

    private final Group spikes;

    SequentialTransition animation;

    public Spikes(Vector position, double areaWidth, double areaLength) {
        super(position);

        spikes = new Group();
        int xCount = (int) (areaWidth / (SPIKE_RADIUS * 2));
        double xStart = -areaWidth / 2 + (areaWidth - (SPIKE_RADIUS * 2 * xCount)) / 2 + SPIKE_RADIUS;
        int zCount = (int) (areaLength / (SPIKE_RADIUS * 2));
        double zStart = -areaLength / 2 + (areaLength - (SPIKE_RADIUS * 2 * zCount)) / 2 + SPIKE_RADIUS;
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < zCount; j++) {
                Cylinder spike = new Cylinder(SPIKE_RADIUS, SPIKE_HEIGHT);
                spike.setMaterial(SPIKE_MATERIAL);
                spike.setTranslateX(xStart + i * 2 * SPIKE_RADIUS);
                spike.setTranslateZ(zStart + j * 2 * SPIKE_RADIUS);
                spikes.getChildren().add(spike);
            }
        }
        spikes.setTranslateY(SPIKE_HEIGHT / 2);
        this.getChildren().addAll(spikes);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());

        TranslateTransition comeUp = new TranslateTransition(Duration.seconds(COME_UP_TIME), spikes);
        comeUp.setByY(-SPIKE_HEIGHT);
        PauseTransition stayUp = new PauseTransition(Duration.seconds(STAY_UP_TIME));
        TranslateTransition comeDown = new TranslateTransition(Duration.seconds(COME_DOWN_TIME), spikes);
        comeDown.setByY(SPIKE_HEIGHT);
        animation = new SequentialTransition(comeUp, stayUp, comeDown);
    }

    public void trigger() {
        if (animation.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        animation.play();
    }

}
