package rooms;

import concepts.GameObject;
import concepts.Updatable;
import concepts.Vector;
import game.Game;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import objects.Clock;
import objects.Coin;
import objects.FloorWithTiles;
import objects.Heart;
import objects.Player;
import objects.Wall;

public abstract class Room extends GameObject implements Updatable {

    protected final Game game;

    protected final double width;
    protected final double length;

    protected final ArrayList<GameObject> collisionObjects = new ArrayList<>();

    protected PointLight movingLight;
    private SequentialTransition lightMovement;
    private static final double LIGHT_DISTANCE_FROM_WALL = 30;

    public Room(Vector position, Game game, double width, double length) {
        super(position);
        this.game = game;
        this.width = width;
        this.length = length;
        createMovingLight();
    }

    protected abstract FloorWithTiles getFloorWithTiles();

    public GameObject populateRandomTile(FloorWithTiles.PopulateOption option, Player player) {
        GameObject object = getFloorWithTiles().populateRandomTile(option, player);
        this.getChildren().add(object);
        collisionObjects.add(object);
        return object;
    }

    public void removeObject(GameObject object) {
        if (object instanceof Coin || object instanceof Heart || object instanceof Clock) {
            getFloorWithTiles().unpopulateTile(object);
        }
        this.getChildren().remove(object);
        collisionObjects.remove(object);
    }

    protected List<Wall> createWalls() {
        ArrayList<Wall> walls = new ArrayList<>(6);

        final double wallHeight = Wall.DEFAULT_HEIGHT;
        final double wallThickness = Wall.DEFAULT_THICKNESS;

        walls.add(new Wall(new Vector(0, -wallHeight / 2, length / 2 + wallThickness / 2), width, wallHeight, wallThickness));
        walls.add(new Wall(new Vector(0, -wallHeight / 2, -length / 2 - wallThickness / 2), width, wallHeight, wallThickness));
        walls.add(new Wall(new Vector(-width / 2 - wallThickness / 2, -wallHeight / 2, 0), wallThickness, wallHeight, length));
        walls.add(new Wall(new Vector(width / 2 + wallThickness / 2, -wallHeight / 2, 0), wallThickness, wallHeight, length));

        return walls;
    }

    private void createMovingLight() {
        movingLight = new PointLight(movingLightColor());
        movingLight.setTranslateX(-width / 2 + LIGHT_DISTANCE_FROM_WALL);
        movingLight.setTranslateY(-Wall.DEFAULT_HEIGHT / 2);
        movingLight.setTranslateZ(-length / 2 + LIGHT_DISTANCE_FROM_WALL);
        this.getChildren().add(movingLight);

        final double timeAlongX = 2;
        final double timeAlongZ = timeAlongX / width * length;

        lightMovement = new SequentialTransition(movingLight);
        TranslateTransition tx1 = new TranslateTransition(Duration.seconds(timeAlongX));
        tx1.setByX(width - 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx2 = new TranslateTransition(Duration.seconds(timeAlongZ));
        tx2.setByZ(length - 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx3 = new TranslateTransition(Duration.seconds(timeAlongX));
        tx3.setByX(-width + 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx4 = new TranslateTransition(Duration.seconds(timeAlongZ));
        tx4.setByZ(-length + 2 * LIGHT_DISTANCE_FROM_WALL);
        lightMovement.getChildren().addAll(tx1, tx2, tx3, tx4);
        lightMovement.setCycleCount(Timeline.INDEFINITE);
        lightMovement.play();
    }

    protected abstract Color movingLightColor();

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public List<GameObject> getCollisionObjects() {
        return collisionObjects;
    }

    public PointLight getMovingLight() {
        return movingLight;
    }

    public Game getGame() {
        return game;
    }

}
