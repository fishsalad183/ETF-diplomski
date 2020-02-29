package objects;

import concepts.Animated;
import concepts.Vector;
import game.Game;
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

public class FloorWithSpikes extends FloorWithTiles implements Animated {

    public static final double FIXED_ADDITIONAL_REACT_TIME_SEC = 0.5;

    private final Spikes[][] spikes;
    private final Timeline[][] spikesAndTilesAnimations;

    public FloorWithSpikes(Vector position, double width, double length, int numOfTilesX, int numOfTilesZ) {
        super(position, width, length, numOfTilesX, numOfTilesZ);
        
        spikes = new Spikes[numOfTilesX][numOfTilesZ];
        spikesAndTilesAnimations = new Timeline[numOfTilesX][numOfTilesZ];

        final double tileWidth = width / numOfTilesX;
        final double tileLength = length / numOfTilesZ;
        final double timeToReact = FIXED_ADDITIONAL_REACT_TIME_SEC + (tileWidth > tileLength ? tileLength : tileWidth) / Player.WALK_SPEED / 60; // 60 is the frequency of UpdateTimer
        for (int i = 0; i < numOfTilesX; i++) {
            for (int j = 0; j < numOfTilesZ; j++) {
                Spikes s = new Spikes(new Vector(tiles[i][j].getTranslateX(), tiles[i][j].getTranslateY() + Spikes.SPIKE_HEIGHT / 20, tiles[i][j].getTranslateZ()), tileWidth, tileLength); // the additional Y value is to avoid texture bugs
                spikes[i][j] = s;
                spikesAndTilesAnimations[i][j] = new Timeline(
                        new KeyFrame(Duration.seconds(timeToReact), e -> s.trigger(), new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR)),
                        new KeyFrame(Duration.seconds(timeToReact + Spikes.STAY_UP_MOMENT), new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), Color.RED, Interpolator.LINEAR)),
                        new KeyFrame(Duration.seconds(timeToReact + Spikes.COME_DOWN_MOMENT), /*e -> s.setDamaging(true), --- done in Spikes object itself*/ new KeyValue(((PhongMaterial) tiles[i][j].getMaterial()).diffuseColorProperty(), DEFAULT_FLOOR_COLOR, Interpolator.LINEAR))
                );
            }
            this.getChildren().addAll(spikes[i]);
        }
    }

    public void triggerSpikesOnRandomTile() {
        int i = Game.RANDOM.nextInt(tiles.length);
        int j = Game.RANDOM.nextInt(tiles[i].length);
        if (tiles[i][j].getUserData() != null || spikesAndTilesAnimations[i][j].getStatus() != Animation.Status.STOPPED) {
            return;
        }
        spikesAndTilesAnimations[i][j].play();
    }

    public Spikes[][] getSpikes() {
        return spikes;
    }
    
    @Override
    protected boolean isTileActive(int i, int j) {
        return spikesAndTilesAnimations[i][j].getStatus() != Animation.Status.STOPPED;
    }

    @Override
    public List<Animation> getAnimations() {
        ArrayList<Animation> list = new ArrayList<>();
        for (Spikes[] sArr : spikes) {
            for (Spikes s : sArr) {
                list.addAll(s.getAnimations());
            }
        }
        for (Animation[] aArr : spikesAndTilesAnimations) {
            list.addAll(Arrays.asList(aArr));
        }
        return list;
    }
    
}
