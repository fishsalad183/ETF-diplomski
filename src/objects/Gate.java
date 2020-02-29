package objects;

import concepts.Animated;
import concepts.GameObject;
import concepts.Vector;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.util.Duration;
import rooms.BufferZone;

public class Gate extends GameObject implements Animated {

    public static final double BAR_RADIUS = Wall.DEFAULT_THICKNESS / 8;

    private static final PhongMaterial GATE_MATERIAL = Spikes.SPIKE_MATERIAL;
    private static final Image GATE_IMAGE = Spikes.SPIKE_IMAGE;

    static {
        GATE_MATERIAL.setDiffuseMap(GATE_IMAGE);
    }

    public static final double ANIMATION_DURATION_SEC = 2;
    private final TranslateTransition openClose;

    private boolean open = false;

    public Gate(Vector position) {
        this(position, BufferZone.DEFAULT_FLOOR_WIDTH, BufferZone.DEFAULT_ENTRANCE_HEIGHT, Wall.DEFAULT_THICKNESS);
    }

    public Gate(Vector position, double width, double height, double depth) {
        super(position);

        final int numOfBars = (int) (width / BAR_RADIUS / 6);
        final double barDistance = width / numOfBars;
        for (int i = 0; i < numOfBars; i++) {
            Cylinder bar = new Cylinder(BAR_RADIUS, height);
            bar.setMaterial(GATE_MATERIAL);
            bar.setTranslateX(-width / 2 + barDistance / 2 + i * barDistance);
            this.getChildren().add(bar);
        }

        openClose = new TranslateTransition(Duration.seconds(ANIMATION_DURATION_SEC), this);
        openClose.setByY(-height);
        openClose.setOnFinished(e -> {
            open = !open;
            openClose.setByY(-openClose.getByY());
        });
    }

    public boolean trigger() {
        if (openClose.getStatus() == Animation.Status.STOPPED) {
            openClose.play();
            return true;
        }
        return false;
    }

    @Override
    public List<Animation> getAnimations() {
        return Arrays.asList(openClose);
    }

    public boolean isOpen() {
        return open;
    }

}
