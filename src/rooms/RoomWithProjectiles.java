package rooms;

import concepts.Vector;
import game.Game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import objects.Floor;
import objects.FloorRegular;
import objects.FloorWithFlowers;
import objects.FloorWithProjectiles;
import objects.FloorWithTiles;
import objects.Projectile;
import objects.Wall;

public class RoomWithProjectiles extends Room {

    public static final double DEFAULT_WIDTH = 3_000;
    public static final double DEFAULT_LENGTH = 4_400;
    public static final double DEFAULT_SAFE_ZONE_LENGTH = 650;
    public static final int DEFAULT_TILES_X = 5;
    public static final int DEFAULT_TILES_Z = 6;

    private final double safeZoneLength;

    private final FloorWithProjectiles floorWithProjectiles;
    public static final PhongMaterial PROJECTILE_SPAWNING_GROUND_MATERIAL = new PhongMaterial(Color.PURPLE);
    private final List<Projectile> projectilesToAdd = Collections.synchronizedList(new ArrayList<>(2));
    private final List<Projectile> projectilesToRemove = Collections.synchronizedList(new ArrayList<>(2));

    public RoomWithProjectiles(Vector position, Game game, double width, double length, double safeZoneLength, int tilesX, int tilesZ) {
        super(position, game, width, length);
        this.safeZoneLength = safeZoneLength;

        collisionObjects.addAll(createWalls());

        final double leftRightDistanceToWalls = width / 9;
        final double launchDistance = leftRightDistanceToWalls / 2;
        final double floorWithProjectilesWidth = width - 2 * leftRightDistanceToWalls;
        final double floorWithProjectilesLength = length - 2 * safeZoneLength;
        floorWithProjectiles = new FloorWithProjectiles(new Vector(0, Floor.FLOOR_HEIGHT / 2, 0), floorWithProjectilesWidth, floorWithProjectilesLength, tilesX, tilesZ, launchDistance, this);

        FloorWithFlowers floorFront = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, -length / 2 + safeZoneLength / 2), width, safeZoneLength);
        FloorWithFlowers floorRear = new FloorWithFlowers(new Vector(0, Floor.FLOOR_HEIGHT / 2, length / 2 - safeZoneLength / 2), width, safeZoneLength);
        FloorRegular floorLeft = new FloorRegular(new Vector(-width / 2 + leftRightDistanceToWalls / 2, Floor.FLOOR_HEIGHT / 2, 0), leftRightDistanceToWalls, floorWithProjectilesLength, PROJECTILE_SPAWNING_GROUND_MATERIAL);
        FloorRegular floorRight = new FloorRegular(new Vector(width / 2 - leftRightDistanceToWalls / 2, Floor.FLOOR_HEIGHT / 2, 0), leftRightDistanceToWalls, floorWithProjectilesLength, PROJECTILE_SPAWNING_GROUND_MATERIAL);

        final double guardWallThickness = leftRightDistanceToWalls / 5;
        final double guardWallHeight = Math.abs(FloorWithTiles.POPULATING_OBJECT_START_Y * 0.9);
        PhongMaterial guardWallMaterial = new PhongMaterial(Color.GRAY);

        collisionObjects.add(new Wall(new Vector(-width / 2 + leftRightDistanceToWalls - guardWallThickness / 2, -guardWallHeight / 2, 0), guardWallThickness, guardWallHeight, floorWithProjectilesLength - 2 * guardWallThickness, guardWallMaterial));
        collisionObjects.add(new Wall(new Vector(-width / 2 + leftRightDistanceToWalls / 2, -guardWallHeight / 2, -floorWithProjectilesLength / 2 + guardWallThickness / 2), leftRightDistanceToWalls, guardWallHeight, guardWallThickness, guardWallMaterial));
        collisionObjects.add(new Wall(new Vector(-width / 2 + leftRightDistanceToWalls / 2, -guardWallHeight / 2, floorWithProjectilesLength / 2 - guardWallThickness / 2), leftRightDistanceToWalls, guardWallHeight, guardWallThickness, guardWallMaterial));

        collisionObjects.add(new Wall(new Vector(width / 2 - leftRightDistanceToWalls + guardWallThickness / 2, -guardWallHeight / 2, 0), guardWallThickness, guardWallHeight, floorWithProjectilesLength - 2 * guardWallThickness, guardWallMaterial));
        collisionObjects.add(new Wall(new Vector(width / 2 - leftRightDistanceToWalls / 2, -guardWallHeight / 2, -floorWithProjectilesLength / 2 + guardWallThickness / 2), leftRightDistanceToWalls, guardWallHeight, guardWallThickness, guardWallMaterial));
        collisionObjects.add(new Wall(new Vector(width / 2 - leftRightDistanceToWalls / 2, -guardWallHeight / 2, floorWithProjectilesLength / 2 - guardWallThickness / 2), leftRightDistanceToWalls, guardWallHeight, guardWallThickness, guardWallMaterial));

        this.getChildren().addAll(floorWithProjectiles, floorFront, floorRear, floorLeft, floorRight);
        this.getChildren().addAll(collisionObjects);
    }

    public RoomWithProjectiles(Vector position, Game game) {
        this(position, game, DEFAULT_WIDTH, DEFAULT_LENGTH, DEFAULT_SAFE_ZONE_LENGTH, DEFAULT_TILES_X, DEFAULT_TILES_Z);
    }

    @Override
    public void update() {
        if (Game.RANDOM.nextDouble() > 0.986) {
            floorWithProjectiles.triggerProjectileOnRandomRow();
        }
        synchronized (projectilesToAdd) {
            if (!projectilesToAdd.isEmpty()) {
                for (Projectile p : projectilesToAdd) {
                    this.getChildren().add(p);
                    collisionObjects.add(p);
                    game.getCollisionObjects().add(p);
                }
                projectilesToAdd.clear();
            }
        }
        synchronized (projectilesToRemove) {
            if (!projectilesToRemove.isEmpty()) {
                for (Projectile p : projectilesToRemove) {
                    this.getChildren().remove(p);
                    collisionObjects.remove(p);
                    game.getCollisionObjects().remove(p);
                }
                projectilesToRemove.clear();
            }
        }
    }

    public void markForAdding(Projectile p) {
        synchronized (projectilesToAdd) {
            projectilesToAdd.add(p);
        }
    }

    public void markForRemoval(Projectile p) {
        synchronized (projectilesToRemove) {
            projectilesToRemove.add(p);
        }
    }

    @Override
    protected Color movingLightColor() {
        return Color.SLATEGRAY;
    }

    @Override
    protected FloorWithTiles getFloorWithTiles() {
        return floorWithProjectiles;
    }

    public double getSafeZoneLength() {
        return safeZoneLength;
    }

}
