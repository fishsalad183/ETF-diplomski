package objects;

import concepts.Animated;
import concepts.DamagingObject;
import concepts.Vector;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Projectile extends DamagingObject implements Animated {

    public static final double RADIUS = 60;
    public static final double DEFAULT_SPEED = Player.SPRINT_SPEED * 1.2;

    private final Timeline creation;
    private final Timeline movement;
    private final Timeline decay;   // removal of Projectile from the node graph is performed after decay animation
    private final Timeline colorChange;

    private final FloorWithProjectiles parentFloor;
    
    public static Projectile createAndAddToChildrenAndForCollision(Vector position, Vector target, double creationDurationSeconds, double speed, FloorWithProjectiles parentFloor) {
        Projectile projectile = new Projectile(position, target, creationDurationSeconds, speed, parentFloor);
        parentFloor.getParentRoom().markForAdding(projectile);
        // adding to collisionObjects is done in RoomWithProjectiles::update()
        return projectile;
    }

    private Projectile(Vector position, Vector target, double creationDurationSeconds, double speed, FloorWithProjectiles parentFloor) {
        super(position);
        this.parentFloor = parentFloor;
        
        this.setDamaging(false);

        Sphere projectile = new Sphere(RADIUS);
        projectile.setMaterial(new PhongMaterial(Color.BLACK));
        this.getChildren().add(projectile);

        Scale scale = new Scale(0, 0, 0);
        projectile.getTransforms().add(scale);  // scale transform has to be added to the projectile node (not this) because it would affect the translate of this otherwise
        creation = new Timeline(
                new KeyFrame(Duration.seconds(creationDurationSeconds),
                        new KeyValue(scale.xProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(scale.yProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(scale.zProperty(), 1, Interpolator.EASE_BOTH)
                )
        );
        creation.setOnFinished(e -> this.setDamaging(true));

        Translate translate = new Translate();
        this.getTransforms().add(translate);
        final double dx = target.getX() - position.getX();
        final double dy = target.getY() - position.getY();
        final double dz = target.getZ() - position.getZ();
        final Duration movementDuration = Duration.seconds(Math.sqrt(dx * dx + dy * dy + dz * dz) / speed / 60.);
        movement = new Timeline(
                new KeyFrame(movementDuration,
                        new KeyValue(translate.xProperty(), dx, Interpolator.EASE_BOTH),
                        new KeyValue(translate.yProperty(), dy, Interpolator.EASE_BOTH),
                        new KeyValue(translate.zProperty(), dz, Interpolator.EASE_BOTH)
                )
        );
        movement.setOnFinished(e -> {
            animateDecayAndRemove();
        });

        decay = new Timeline(
                new KeyFrame(Duration.seconds(creationDurationSeconds / 2),
                        new KeyValue(scale.xProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(scale.yProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(scale.zProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(((PhongMaterial) projectile.getMaterial()).diffuseColorProperty(), Color.BLACK, Interpolator.EASE_BOTH)
                )
        );
        decay.setOnFinished(e -> parentFloor.getParentRoom().markForRemoval(this));

        colorChange = new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(((PhongMaterial) projectile.getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR))
        );
        colorChange.setAutoReverse(true);
        colorChange.setCycleCount(Timeline.INDEFINITE);
        colorChange.play();
    }

    public void animateCreation() {
        creation.play();
    }

    public void animateMovementThenDecayAndRemove() {
        movement.play();
    }

    public void animateDecayAndRemove() {
        setDamaging(false);
        movement.stop();
        colorChange.stop();
        decay.play();
        // removal is in decay.setOnFinished(...)
    }

    @Override
    public List<Animation> getAnimations() {
        return Arrays.asList(creation, movement, decay, colorChange);
    }

    public FloorWithProjectiles getParentFloor() {
        return parentFloor;
    }

}
