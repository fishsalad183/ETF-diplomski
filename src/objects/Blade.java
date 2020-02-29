package objects;

import concepts.Animated;
import concepts.DamagingObject;
import concepts.Vector;
import game.Game;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.value.WritableValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Blade extends DamagingObject implements Animated {

    public static final double HEIGHT = 120;
    public static final double THICKNESS = 15;

    // for transition durations
    public static final double COME_DOWN_TIME = 0.25;
    public static final double STAY_DOWN_TIME = 0.75;
    public static final double COME_UP_TIME = 0.25;

    public static final double ROTATION_TIME = 0.75;

    // for timeline Duration instances, in seconds
    public static final double COME_DOWN_MOMENT = COME_DOWN_TIME;
    public static final double STAY_DOWN_MOMENT = COME_DOWN_MOMENT + STAY_DOWN_TIME;
    public static final double COME_UP_MOMENT = STAY_DOWN_MOMENT + COME_UP_TIME;

    public static final PhongMaterial BLADE_MATERIAL = Spikes.SPIKE_MATERIAL;
    public static final PhongMaterial ROPE_MATERIAL = new PhongMaterial(Color.BLACK);

    private final SequentialTransition downUp;
    private final Rotate rotate;
    private final Timeline rotation;

    public Blade(Vector position, double width, double fallDistance) {
        super(position);

        Box blade = new Box(width, HEIGHT, THICKNESS);
        blade.setMaterial(BLADE_MATERIAL);
        this.getChildren().add(blade);
        
        Cylinder rope = new Cylinder(THICKNESS / 3, 1);
        rope.setMaterial(ROPE_MATERIAL);
        rope.setTranslateY(-blade.getHeight() / 2);
        this.getChildren().add(rope);

        TranslateTransition bladeDown = new TranslateTransition(Duration.seconds(COME_DOWN_TIME), blade);
        bladeDown.setByY(fallDistance);
        TranslateTransition ropeDown = new TranslateTransition(Duration.seconds(COME_DOWN_TIME), rope);
        ropeDown.setByY(fallDistance / 2);
        ScaleTransition ropeExtend = new ScaleTransition(Duration.seconds(COME_DOWN_TIME), rope);
        ropeExtend.setToY(fallDistance);
        ParallelTransition down = new ParallelTransition(bladeDown, ropeDown, ropeExtend);
        
        PauseTransition stayDown = new PauseTransition(Duration.seconds(STAY_DOWN_TIME));
        
        TranslateTransition bladeUp = new TranslateTransition(Duration.seconds(COME_UP_TIME), blade);
        bladeUp.setByY(-fallDistance);
        TranslateTransition ropeUp = new TranslateTransition(Duration.seconds(COME_UP_TIME), rope);
        ropeUp.setByY(-fallDistance / 2);
        ScaleTransition ropeContract = new ScaleTransition(Duration.seconds(COME_UP_TIME), rope);
        ropeContract.setToY(1);
        ParallelTransition up = new ParallelTransition(bladeUp, ropeUp, ropeContract);
        
        downUp = new SequentialTransition(down, stayDown, up);
        downUp.setOnFinished(e -> this.setDamaging(true));

        rotate = new Rotate();
        rotate.setAxis(Rotate.Y_AXIS);
        this.getTransforms().add(rotate);
        rotation = new Timeline(    // see rotateToRandomAngle() before adding any more keyframes
                new KeyFrame(Duration.seconds(ROTATION_TIME), new KeyValue(rotate.angleProperty(), -180. + Game.RANDOM.nextDouble() * 360., Interpolator.EASE_BOTH))
        );
//        rotation.setOnFinished(e -> {
//            WritableValue<Double> target = (WritableValue<Double>) rotation.getKeyFrames().get(0).getValues().iterator().next().getTarget();
//            target.setValue(-180. + Game.RANDOM.nextDouble() * 360.);
//        });
    }

    public void trigger() {
        if (downUp.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        downUp.play();
    }

    public Double rotateToRandomAngle() {
        if (rotation.getStatus() != Animation.Status.STOPPED) {
            return null;
        }
        WritableValue<Double> target = (WritableValue<Double>) rotation.getKeyFrames().get(0).getValues().iterator().next().getTarget();    // assumes there is only one KeyFrame
        Double angle = -180. + Game.RANDOM.nextDouble() * 360.;
        target.setValue(angle);
        rotation.play();
        return angle;
    }

    @Override
    public List<Animation> getAnimations() {
        return Arrays.asList(downUp, rotation);
    }

}
