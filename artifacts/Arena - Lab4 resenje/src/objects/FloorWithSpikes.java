package objects;

import geometry.Vector;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;

public class FloorWithSpikes extends Group {

    public static final double FLOOR_HEIGHT = 1;

    private static final Color DEFAULT_FLOOR_COLOR = Color.BURLYWOOD;
    private static final Image FLOOR_IMAGE = new Image("resources/sand.jpg");

    private static final double SEPARATOR_THICKNESS = 10;
    private static final PhongMaterial SEPARATOR_MATERIAL = new PhongMaterial(Color.BLACK);

    public static final double ADDITIONAL_REACT_TIME_SEC = 1.25;

    public enum PopulateOption {
        COIN;
    }

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
                tileMaterial.setDiffuseMap(FLOOR_IMAGE);
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
        if (tiles[i][j].getUserData() != null || spikesAnimations[i][j].getStatus() != Animation.Status.STOPPED) {
            return;
        }
        spikesAnimations[i][j].play();
    }

    public Spikes[][] getSpikes() {
        return spikes;
    }

    public GameObject populateRandomTile(PopulateOption option, Player player) {
        int i = (int) (Math.random() * tiles.length);
        int j = (int) (Math.random() * tiles[i].length);
        int cnt = 0;
        while (tiles[i][j].getUserData() != null
                || spikesAnimations[i][j].getStatus() != Animation.Status.STOPPED
                || // potentially use !tiles[i][j].intersects(player.localToScene(player.getBody().getBoundsInParent())) or something alike in case the player position conditions don't work properly
                ((Math.abs(tiles[i][j].getTranslateX() - player.getTranslateX()) < tiles[i][j].getWidth() / 2)
                && (Math.abs(tiles[i][j].getTranslateZ() - player.getTranslateZ()) < tiles[i][j].getDepth() / 2))) { // select the next tile
            if (j == tiles[i].length - 1) {
                j = 0;
                if (i == tiles.length - 1) {
                    i = 0;
                } else {
                    i++;
                }
            } else {
                j++;
            }
            if (++cnt == tiles.length * tiles[i].length) {  // against errors (infinite loops) ?
                return null;
            }
        }
        GameObject object = null;
        switch (option) {
            case COIN:
                object = new Coin(new Vector(tiles[i][j].getTranslateX(), -Coin.RADIUS * 2, tiles[i][j].getTranslateZ()));
                break;
        }
        tiles[i][j].setUserData(object);
        return object;
    }

    public boolean unpopulateTile(GameObject object) {
        for (Box[] tileRow : tiles) {
            for (Box tile : tileRow) {
                if (tile.getUserData() == object) {
                    tile.setUserData(null);
                    return true;
                }
            }
        }
        return false;
    }

}
