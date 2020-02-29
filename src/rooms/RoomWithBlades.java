package rooms;

import concepts.GameObject;
import concepts.Vector;
import game.Game;
import java.util.Arrays;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import objects.Blade;
import objects.Floor;
import objects.FloorWithBlades;
import objects.FloorWithFlowers;
import objects.FloorWithTiles;
import objects.Wall;

public class RoomWithBlades extends Room {

    public static final double DEFAULT_SAFE_ZONE_LENGTH = 700;
    public static final double DEFAULT_WIDTH = 2_700;
    public static final double DEFAULT_LENGTH = DEFAULT_WIDTH + 2 * DEFAULT_SAFE_ZONE_LENGTH;
    public static final int DEFAULT_TILES_X = 6;
    public static final int DEFAULT_TILES_Z = DEFAULT_TILES_X;

    public static final double DISTANCE_FROM_FLOOR_TO_CEILING = Wall.DEFAULT_HEIGHT * 0.65;

    private final double safeZoneLength;

    private final FloorWithBlades floorWithBlades;

    public RoomWithBlades(Vector position, Game game, double width, double length, double safeZoneLength, int tilesX, int tilesZ) {
        super(position, game, width, length);
        this.safeZoneLength = safeZoneLength;

        collisionObjects.addAll(createWalls());

        Wall ceiling = new Wall(new Vector(0, -DISTANCE_FROM_FLOOR_TO_CEILING - Wall.DEFAULT_THICKNESS / 2, 0), width, Wall.DEFAULT_THICKNESS, length - 2 * safeZoneLength, new PhongMaterial(Color.rgb(0x80, 0x80, 0x80, 0.7)));

        floorWithBlades = new FloorWithBlades(new Vector(0, Floor.FLOOR_HEIGHT / 2, 0), width, length - 2 * safeZoneLength, tilesX, tilesZ, DISTANCE_FROM_FLOOR_TO_CEILING - Blade.HEIGHT);
        Blade[][] blades = floorWithBlades.getBlades();
        for (Blade[] b : blades) {
            collisionObjects.addAll(Arrays.asList(b));
        }

        FloorWithFlowers floor1 = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, -length / 2 + safeZoneLength / 2), width, safeZoneLength);
        FloorWithFlowers floor2 = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, length / 2 - safeZoneLength / 2), width, safeZoneLength);

        this.getChildren().addAll(ceiling, floorWithBlades, floor1, floor2);
        this.getChildren().addAll(collisionObjects);
    }

    public RoomWithBlades(Vector position, Game game) {
        this(position, game, DEFAULT_WIDTH, DEFAULT_LENGTH, DEFAULT_SAFE_ZONE_LENGTH, DEFAULT_TILES_X, DEFAULT_TILES_Z);
    }

    @Override
    public List<GameObject> removeUnnecessaryObjectsAndFinalize() {
        List<GameObject> objectsToRemove = super.removeUnnecessaryObjectsAndFinalize();
        for (Box[] tileArray : floorWithBlades.getTiles()) {
            for (Box tile : tileArray) {
                ((PhongMaterial) tile.getMaterial()).setDiffuseColor(FloorWithBlades.DEFAULT_FLOOR_COLOR);  // to avoid problems with color change animations
            }
        }
        return objectsToRemove;
    }

    @Override
    protected FloorWithTiles getFloorWithTiles() {
        return floorWithBlades;
    }

    @Override
    public void update() {
        if (Game.RANDOM.nextDouble() > 0.98) {
            floorWithBlades.triggerRandomBlade();
        }
    }

    @Override
    protected Color movingLightColor() {
        return Color.AQUAMARINE;
    }

    public double getSafeZoneLength() {
        return safeZoneLength;
    }

}
