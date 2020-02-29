package objects;

import concepts.Animated;
import concepts.DamagingObject;
import concepts.Vector;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;

public class Spikes extends DamagingObject implements Animated {

    public static final double SPIKE_RADIUS = 120;
    public static final double SPIKE_HEIGHT = SPIKE_RADIUS * 2;

    private static final int VERTICES_IN_CIRCLE = 8;

    // for transition durations
    public static final double COME_UP_TIME = 0.25;
    public static final double STAY_UP_TIME = 0.75;
    public static final double COME_DOWN_TIME = 0.25;

    // for timeline Duration instances, in seconds
    public static final double COME_UP_MOMENT = COME_UP_TIME;
    public static final double STAY_UP_MOMENT = COME_UP_MOMENT + STAY_UP_TIME;
    public static final double COME_DOWN_MOMENT = STAY_UP_MOMENT + COME_DOWN_TIME;

    public static final PhongMaterial SPIKE_MATERIAL = new PhongMaterial(Color.DARKGRAY);
    public static final Image SPIKE_IMAGE = new Image("resources/metal.jpg");

    static {
        SPIKE_MATERIAL.setDiffuseMap(SPIKE_IMAGE);
    }

    private final SequentialTransition upDown;

    public Spikes(Vector position, double areaWidth, double areaLength) {
        super(position);

        Group spikes = new Group();
        int xCount = (int) (areaWidth / (SPIKE_RADIUS * 2));
        double xStart = -areaWidth / 2 + (areaWidth - (SPIKE_RADIUS * 2 * xCount)) / 2 + SPIKE_RADIUS;
        int zCount = (int) (areaLength / (SPIKE_RADIUS * 2));
        double zStart = -areaLength / 2 + (areaLength - (SPIKE_RADIUS * 2 * zCount)) / 2 + SPIKE_RADIUS;
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < zCount; j++) {
                MeshView spike = createSpike();
                spike.setTranslateX(xStart + i * 2 * SPIKE_RADIUS);
                spike.setTranslateZ(zStart + j * 2 * SPIKE_RADIUS);
                spikes.getChildren().add(spike);
            }
        }
        spikes.setTranslateY(SPIKE_HEIGHT / 2);
        this.getChildren().addAll(spikes);

        TranslateTransition comeUp = new TranslateTransition(Duration.seconds(COME_UP_TIME), spikes);
        comeUp.setByY(-SPIKE_HEIGHT);
        PauseTransition stayUp = new PauseTransition(Duration.seconds(STAY_UP_TIME));
        TranslateTransition comeDown = new TranslateTransition(Duration.seconds(COME_DOWN_TIME), spikes);
        comeDown.setByY(SPIKE_HEIGHT);
        upDown = new SequentialTransition(comeUp, stayUp, comeDown);
        upDown.setOnFinished(e -> this.setDamaging(true));
    }

    private MeshView createSpike() {
        float[] points = new float[(VERTICES_IN_CIRCLE + 1) * 3];   // + 1 because the last point is the tip
        final double angleStep = (360. / VERTICES_IN_CIRCLE) * Math.PI / 180.;
        double angle = 0;
        for (int i = 0; i < points.length - 3; i += 3) {
            points[i] = (float) (SPIKE_RADIUS * Math.cos(angle));
            points[i + 1] = (float) (SPIKE_HEIGHT / 2);
            points[i + 2] = (float) (SPIKE_RADIUS * Math.sin(angle));
            angle += angleStep;
        }
        points[points.length - 3] = 0;
        points[points.length - 2] = (float) (-SPIKE_HEIGHT / 2);
        points[points.length - 1] = 0;

        float[] texCoords = {
            0.0f, 1.0f,
            0.5f, 0.0f,
            1.0f, 1.0f
        };

        int faces[] = new int[VERTICES_IN_CIRCLE * 12];
        for (int j = 0, i = 0; i < faces.length; j++, i += 12) {
            faces[i] = j % VERTICES_IN_CIRCLE;
            faces[i + 1] = 0;
            faces[i + 2] = (j + 1) % VERTICES_IN_CIRCLE;
            faces[i + 3] = 2;
            faces[i + 4] = VERTICES_IN_CIRCLE;  // tip of the spike
            faces[i + 5] = 1;

            faces[i + 6] = j % VERTICES_IN_CIRCLE;
            faces[i + 7] = 0;
            faces[i + 8] = VERTICES_IN_CIRCLE;  // tip of the spike
            faces[i + 9] = 1;
            faces[i + 10] = (j + 1) % VERTICES_IN_CIRCLE;
            faces[i + 11] = 2;
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        MeshView spike = new MeshView(mesh);

        spike.setMaterial(SPIKE_MATERIAL);

        return spike;
    }

    public void trigger() {
        if (upDown.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        upDown.play();
    }

    @Override
    public List<Animation> getAnimations() {
        return Arrays.asList(upDown);
    }

}
