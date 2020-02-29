package rooms;

import concepts.Vector;
import game.Game;
import java.util.Arrays;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import objects.Floor;
import objects.FloorWithFlowers;
import objects.FloorWithSpikes;
import objects.FloorWithTiles;
import objects.Spikes;
import objects.Wall;

public class RoomWithSpikes extends Room {

    public static final double DEFAULT_WIDTH = 3_000;
    public static final double DEFAULT_LENGTH = 5_000;
    public static final double DEFAULT_SAFE_ZONE_LENGTH = 750;
    public static final int DEFAULT_TILES_X = 4;
    public static final int DEFAULT_TILES_Z = 5;

    private final double safeZoneLength;

    private final FloorWithSpikes floorWithSpikes;

    private final PointLight centralLight;

    public RoomWithSpikes(Vector position, Game game, double width, double length, double safeZoneLength, int tilesX, int tilesZ) {
        super(position, game, width, length);
        this.safeZoneLength = safeZoneLength;

        collisionObjects.addAll(createWalls());

        floorWithSpikes = new FloorWithSpikes(new Vector(0, Floor.FLOOR_HEIGHT / 2, 0), width, length - 2 * safeZoneLength, tilesX, tilesZ);
        Spikes[][] spikes = floorWithSpikes.getSpikes();
        for (Spikes[] s : spikes) {
            collisionObjects.addAll(Arrays.asList(s));
        }

        FloorWithFlowers floor1 = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, -length / 2 + safeZoneLength / 2), width, safeZoneLength);
        FloorWithFlowers floor2 = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, length / 2 - safeZoneLength / 2), width, safeZoneLength);

        centralLight = new PointLight();
        centralLight.setTranslateY(-Wall.DEFAULT_HEIGHT * 16);

        this.getChildren().addAll(floorWithSpikes, floor1, floor2, centralLight);
        this.getChildren().addAll(collisionObjects);
    }

    public RoomWithSpikes(Vector position, Game game) {
        this(position, game, DEFAULT_WIDTH, DEFAULT_LENGTH, DEFAULT_SAFE_ZONE_LENGTH, DEFAULT_TILES_X, DEFAULT_TILES_Z);
    }

    @Override
    public void update() {
        if (Game.RANDOM.nextDouble() > 0.977) {
            floorWithSpikes.triggerSpikesOnRandomTile();
        }
    }

    @Override
    protected Color movingLightColor() {
        return Color.BLUEVIOLET;
    }

    @Override
    protected FloorWithTiles getFloorWithTiles() {
        return floorWithSpikes;
    }

    public PointLight getCentralLight() {
        return centralLight;
    }

    public double getSafeZoneLength() {
        return safeZoneLength;
    }

}
