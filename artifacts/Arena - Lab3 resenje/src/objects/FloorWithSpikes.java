package objects;

import geometry.Vector;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;

public class FloorWithSpikes extends Group {

    public static final double FLOOR_HEIGHT = 1;

    private static final Color DEFAULT_FLOOR_COLOR = Color.BURLYWOOD;

    private static final double SEPARATOR_THICKNESS = 10;
    private static final PhongMaterial SEPARATOR_MATERIAL = new PhongMaterial(Color.BLACK);

    public static final double ADDITIONAL_REACT_TIME_SEC = 1.25;

    private final Box[][] tiles;
    private final Spikes[][] spikes;
    private final Timeline[][] spikesAnimations;

    public FloorWithSpikes(double width, double length, int numOfTilesX, int numOfTilesZ) {
        tiles = new Box[numOfTilesX][numOfTilesZ];
        spikes = new Spikes[numOfTilesX][numOfTilesZ];
        spikesAnimations = new Timeline[numOfTilesX][numOfTilesZ];

        final double tileWidth = width / numOfTilesX;
        final double tileLength = length / numOfTilesZ;
        final double timeToReact = ADDITIONAL_REACT_TIME_SEC + (tileWidth > tileLength ? tileLength : tileWidth) / Player.PLAYER_VELOCITY / 60; // 60 is the frequency of UpdateTimer
        for (int i = 0; i < numOfTilesX; i++) {
            for (int j = 0; j < numOfTilesZ; j++) {
                tiles[i][j] = new Box(tileWidth, FLOOR_HEIGHT, tileLength);
                tiles[i][j].setTranslateX(-width / 2 + tileWidth / 2 + i * tileWidth);
                tiles[i][j].setTranslateZ(-length / 2 + tileLength / 2 + j * tileLength);
                PhongMaterial tileMaterial = new PhongMaterial(DEFAULT_FLOOR_COLOR);
                tiles[i][j].setMaterial(tileMaterial);

                Spikes s = new Spikes(new Vector(tiles[i][j].getTranslateX(), tiles[i][j].getTranslateY() + Spikes.SPIKE_HEIGHT / 20, tiles[i][j].getTranslateZ()), tileWidth, tileLength); // the additional Y value is to avoid texture bugs
                spikes[i][j] = s;
                spikesAnimations[i][j] = new Timeline(
                        new KeyFrame(Duration.seconds(timeToReact), e -> s.trigger(), new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR)),
                        new KeyFrame(Duration.seconds(timeToReact + Spikes.STAY_UP_MOMENT), new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR)),
                        new KeyFrame(Duration.seconds(timeToReact + Spikes.COME_DOWN_MOMENT), new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), DEFAULT_FLOOR_COLOR, Interpolator.LINEAR))
                );
            }
            this.getChildren().addAll(tiles[i]);
            this.getChildren().addAll(spikes[i]);
        }

        for (int i = 0; i < numOfTilesX - 1; i++) {
            Box separator = new Box(SEPARATOR_THICKNESS, FLOOR_HEIGHT, length);
            separator.setTranslateX(-width / 2 + (i + 1) * tileWidth);
            separator.setTranslateY(-FLOOR_HEIGHT);
            separator.setMaterial(SEPARATOR_MATERIAL);
            this.getChildren().add(separator);
        }
        for (int i = 0; i < numOfTilesZ - 1; i++) {
            Box separator = new Box(width, FLOOR_HEIGHT, SEPARATOR_THICKNESS);
            separator.setTranslateZ(-length / 2 + (i + 1) * tileLength);
            separator.setTranslateY(-FLOOR_HEIGHT);
            separator.setMaterial(SEPARATOR_MATERIAL);
            this.getChildren().add(separator);
        }
    }

    public void triggerSpikesOnRandomTile() {
        int i = (int) (Math.random() * tiles.length);
        int j = (int) (Math.random() * tiles[i].length);
        if (spikesAnimations[i][j].getStatus() != Animation.Status.STOPPED) {
            return;
        }
        spikesAnimations[i][j].play();
    }

    public Spikes[][] getSpikes() {
        return spikes;
    }
}
