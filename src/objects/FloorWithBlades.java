package objects;

import concepts.Animated;
import concepts.Vector;
import game.Game;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

public class FloorWithBlades extends FloorWithTiles implements Animated {

    public static final double DEFAULT_BLADE_FALL_DISTANCE = Wall.DEFAULT_HEIGHT - Blade.HEIGHT;

    private final Blade[][] blades;
    private final Timeline[][] bladesAndTilesAnimations;
    private final ArrayDeque<Blade> bladesToFall = new ArrayDeque<>();

    public FloorWithBlades(Vector position, double width, double length, int numOfTilesX, int numOfTilesZ, double bladeFallDistance) {
        super(position, width, length, numOfTilesX, numOfTilesZ);

        blades = new Blade[numOfTilesX - 1][numOfTilesZ - 1];
        bladesAndTilesAnimations = new Timeline[numOfTilesX - 1][numOfTilesZ - 1];
        final double bladeWidth = width / numOfTilesX < length / numOfTilesZ ? width / numOfTilesX : length / numOfTilesZ;
        for (int i = 0; i < blades.length; i++) {
            for (int j = 0; j < blades[i].length; j++) {
                Blade b = new Blade(new Vector(tiles[i][j].getTranslateX() + tiles[i][j].getWidth() / 2, -Blade.HEIGHT / 2 - bladeFallDistance, tiles[i][j].getTranslateZ() + tiles[i][j].getDepth() / 2), bladeWidth, bladeFallDistance);
                b.rotateToRandomAngle();
                blades[i][j] = b;
                final int xIndex = i;
                final int zIndex = j;
                bladesAndTilesAnimations[i][j] = new Timeline(
                        new KeyFrame(Duration.ZERO, e -> rotateBladeAndPrepareToFall(xIndex, zIndex)),
                        new KeyFrame(Duration.seconds(Blade.ROTATION_TIME), e -> lowerBlade(), tilesKeyValuesAroundBlade(xIndex, zIndex, Color.RED)),
                        new KeyFrame(Duration.seconds(Blade.ROTATION_TIME + Blade.STAY_DOWN_MOMENT), tilesKeyValuesAroundBlade(xIndex, zIndex, Color.RED)),
                        new KeyFrame(Duration.seconds(Blade.ROTATION_TIME + Blade.COME_UP_MOMENT), tilesKeyValuesAroundBlade(xIndex, zIndex, DEFAULT_FLOOR_COLOR))
                );
            }
            this.getChildren().addAll(blades[i]);
        }
    }

    public FloorWithBlades(Vector position, double width, double length, int numOfTilesX, int numOfTilesZ) {
        this(position, width, length, numOfTilesX, numOfTilesZ, DEFAULT_BLADE_FALL_DISTANCE);
    }

    private KeyValue[] tilesKeyValuesAroundBlade(int i, int j, Color color) {
        KeyValue[] keyValues = new KeyValue[4];
        keyValues[0] = new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), color, Interpolator.LINEAR);
        keyValues[1] = new KeyValue(((PhongMaterial) tiles[i + 1][j].getMaterial()).diffuseColorProperty(), color, Interpolator.LINEAR);
        keyValues[2] = new KeyValue(((PhongMaterial) tiles[i][j + 1].getMaterial()).diffuseColorProperty(), color, Interpolator.LINEAR);
        keyValues[3] = new KeyValue(((PhongMaterial) tiles[i + 1][j + 1].getMaterial()).diffuseColorProperty(), color, Interpolator.LINEAR);
        return keyValues;
    }

    public void triggerRandomBlade() {
        int i = Game.RANDOM.nextInt(bladesAndTilesAnimations.length);
        int j = Game.RANDOM.nextInt(bladesAndTilesAnimations[i].length);
        if (bladesAndTilesAnimations[i][j].getStatus() != Animation.Status.STOPPED
                || tiles[i][j].getUserData() != null
                || tiles[i + 1][j].getUserData() != null
                || tiles[i][j + 1].getUserData() != null
                || tiles[i + 1][j + 1].getUserData() != null) {
            return;
        }
        bladesAndTilesAnimations[i][j].play();
    }

    public void rotateBladeAndPrepareToFall(int i, int j) {
        blades[i][j].rotateToRandomAngle();
        bladesToFall.add(blades[i][j]);
    }

    public void lowerBlade() {
        Blade blade = bladesToFall.poll();
        if (blade != null) {
            blade.trigger();
        }
    }

    public Blade[][] getBlades() {
        return blades;
    }

    @Override
    public List<Animation> getAnimations() {
        List<Animation> animations = new ArrayList<>();
        for (Blade[] bArr : blades) {
            for (Blade b : bArr) {
                animations.addAll(b.getAnimations());
            }
        }
        for (Animation[] aArr : bladesAndTilesAnimations) {
            animations.addAll(Arrays.asList(aArr));
        }
        return animations;
    }

}
