package objects;

import concepts.GameObject;
import concepts.Vector;
import java.util.Calendar;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Clock extends GameObject {

    public static final double RADIUS = 50;
    public static final double THICKNESS = 15;

    private static final double ROTATION_TIME = 2;

    private static final PhongMaterial BLACK = new PhongMaterial(Color.BLACK);
    private static final PhongMaterial WHITE = new PhongMaterial(Color.WHITE);
    private static final PhongMaterial SLATEGRAY = new PhongMaterial(Color.SLATEGRAY);

    private static final double MINUTE_HAND_ANGLE;
    private static final double HOUR_HAND_ANGLE;

    static {
        Calendar cal = Calendar.getInstance();
        MINUTE_HAND_ANGLE = cal.get(Calendar.MINUTE) / 60. * 360.;
        HOUR_HAND_ANGLE = cal.get(Calendar.HOUR) / 12. * 360.;
    }

    private final RotateTransition rotateTransition;

    public Clock(Vector position) {
        super(position);

        Cylinder frame = new Cylinder(RADIUS, THICKNESS);
        frame.setMaterial(BLACK);
        Group frontFace = createClockFace();
        frontFace.getTransforms().add(new Translate(0, -THICKNESS * 0.1, 0));
        Group rearFace = createClockFace();
        rearFace.getTransforms().addAll(
                new Rotate(180., Rotate.Z_AXIS),
                new Translate(0, -THICKNESS * 0.1, 0)
        );
        this.getChildren().addAll(frame, frontFace, rearFace);

        this.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        rotateTransition = new RotateTransition(Duration.seconds(ROTATION_TIME), this);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.play();
    }

    private Group createClockFace() {
        Group clockFace = new Group();

        Cylinder face = new Cylinder(RADIUS * 9 / 10, THICKNESS);
        face.setMaterial(WHITE);
        clockFace.getChildren().add(face);

        for (double angle = 0; angle < 12 * 30; angle += 30) {
            Box hourMark = new Box(RADIUS / 3.8, THICKNESS * 0.125, RADIUS / 30);
            hourMark.setMaterial(SLATEGRAY);
            hourMark.getTransforms().addAll(
                    new Rotate(angle, Rotate.Y_AXIS),
                    new Translate(RADIUS * 0.7, -THICKNESS / 2., 0)
            );
            clockFace.getChildren().add(hourMark);
        }

        Box hourHand = new Box(RADIUS / 1.7, THICKNESS * 0.175, RADIUS / 15);
        hourHand.setMaterial(BLACK);
        hourHand.getTransforms().addAll(
                new Rotate(270. + HOUR_HAND_ANGLE + MINUTE_HAND_ANGLE / 12, Rotate.Y_AXIS),
                new Translate(hourHand.getWidth() / 2. - RADIUS * 0.2, -THICKNESS / 2., 0)
        );
        Box minuteHand = new Box(RADIUS / 1.3, THICKNESS * 0.175, RADIUS / 15);
        minuteHand.setMaterial(BLACK);
        minuteHand.getTransforms().addAll(
                new Rotate(270. + MINUTE_HAND_ANGLE, Rotate.Y_AXIS),
                new Translate(minuteHand.getWidth() / 2. - RADIUS * 0.2, -THICKNESS / 2., 0)
        );
        clockFace.getChildren().addAll(hourHand, minuteHand);

        return clockFace;
    }

}
