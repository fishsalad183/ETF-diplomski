package objects;

import concepts.Animated;
import concepts.Vector;
import game.Game;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import rooms.RoomWithProjectiles;

public class FloorWithProjectiles extends FloorWithTiles implements Animated {

    public static final double TILES_BECOMING_RED_MOMENT = 0.8;
    public static final double TILES_REVERTING_TO_DEFAULT_COLOR_MOMENT = TILES_BECOMING_RED_MOMENT * 1.5;
    public static final double TILES_COOLDOWN_MOMENT = TILES_REVERTING_TO_DEFAULT_COLOR_MOMENT * 4. / 3.;

    private final double launchDistance;
    private final Timeline[] launching;
    private final ConcurrentLinkedDeque<Projectile> projectilesToLaunch = new ConcurrentLinkedDeque<>();    // or use ArrayDeque<Projectile> and synchronize?

    private final RoomWithProjectiles parentRoom;

    public FloorWithProjectiles(Vector position, double width, double length, int numOfTilesX, int numOfTilesZ, double launchDistance, RoomWithProjectiles parentRoom) {
        super(position, width, length, numOfTilesX, numOfTilesZ);
        this.parentRoom = parentRoom;
        this.launchDistance = launchDistance;

        launching = new Timeline[numOfTilesZ];
        for (int i = 0; i < launching.length; i++) {
            final int zIndex = i;
            launching[i] = new Timeline(
                    new KeyFrame(Duration.ZERO, r -> createProjectileOnRandomSide(zIndex, TILES_BECOMING_RED_MOMENT)),
                    new KeyFrame(Duration.seconds(TILES_BECOMING_RED_MOMENT), "warnAndLaunch", r -> launchLastCreatedProjectile(), Arrays.stream(tiles).map(col -> col[zIndex]).map(tile -> new KeyValue(((PhongMaterial) tile.getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR)).toArray(KeyValue[]::new)),
                    new KeyFrame(Duration.seconds(TILES_REVERTING_TO_DEFAULT_COLOR_MOMENT), "revertColor", Arrays.stream(tiles).map(col -> col[zIndex]).map(tile -> new KeyValue(((PhongMaterial) tile.getMaterial()).diffuseColorProperty(), DEFAULT_FLOOR_COLOR, Interpolator.LINEAR)).toArray(KeyValue[]::new)),
                    new KeyFrame(Duration.seconds(TILES_COOLDOWN_MOMENT), "cooldown")
            );
        }
    }

    public void triggerProjectileOnRandomRow() {
        int i = Game.RANDOM.nextInt(launching.length);
        if (launching[i].getStatus() != Animation.Status.STOPPED) {
            return;
        }
        launching[i].play();
    }

    private void createProjectileOnRandomSide(int tileZIndex, double creationDurationSeconds) {
        final int indexFactor = Game.RANDOM.nextInt(2);
        final int tileXIndex = indexFactor * (tiles.length - 1);
        Box closestTile = tiles[tileXIndex][tileZIndex];
        final Vector creationPosition = new Vector(closestTile.getTranslateX() + (indexFactor == 0 ? -1 : 1) * (closestTile.getWidth() / 2 + launchDistance), POPULATING_OBJECT_START_Y - Projectile.RADIUS, closestTile.getTranslateZ());
        final Vector targetPosition = creationPosition.duplicate().setX(-creationPosition.getX());
        Projectile projectile = Projectile.createAndAddToChildrenAndForCollision(creationPosition, targetPosition, creationDurationSeconds, Projectile.DEFAULT_SPEED, this);
        projectilesToLaunch.add(projectile);
        projectile.animateCreation();
    }

    private void launchLastCreatedProjectile() {
        Projectile projectile = projectilesToLaunch.poll();
        if (projectile != null) {
            projectile.animateMovementThenDecayAndRemove();
        }
    }

    @Override
    protected boolean isTileActive(int i, int j) {
        return launching[j].getStatus() != Animation.Status.STOPPED;
    }

    @Override
    public List<Animation> getAnimations() {
        List<Animation> animations = Animated.super.getAnimations();
        animations.addAll(Arrays.asList(launching));
        return animations;
    }

    public double getLaunchDistance() {
        return launchDistance;
    }

    public RoomWithProjectiles getParentRoom() {
        return parentRoom;
    }

}
