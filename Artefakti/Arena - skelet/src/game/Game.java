package game;

import geometry.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import objects.FloorWithSpikes;
import objects.GameObject;
import objects.Player;
import java.util.Arrays;
import objects.Floor;
import objects.Spikes;
import objects.Wall;

public class Game extends Application {

    private static final double WINDOW_WIDTH = 1366;
    private static final double WINDOW_HEIGHT = 768;

    private Group root;
    private Scene scene;
    private Stage stage;

    private static final double FLOOR_WIDTH = 3_000;
    private static final double FLOOR_LENGTH = 5_000;
    private static final double SAFE_ZONE_LENGTH = 750;
    private static final int TILES_X = 4;
    private static final int TILES_Z = 5;
    private FloorWithSpikes floorWithSpikes;

    private Player player;

    private AmbientLight ambientLight;
    private final Color defaultAmbientColor = new Color(.7, .7, .7, .7);

    private final ArrayList<GameObject> collisionObjects = new ArrayList<>();

    private final UpdateTimer timer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {

        @Override
        public void handle(long now) {
            player.update();
            checkForCollisions();

            if (Math.random() > 0.98) {
                floorWithSpikes.triggerSpikesOnRandomTile();
            }
        }
    }

    private void checkForCollisions() {
        Iterator<GameObject> it = collisionObjects.iterator();
        while (it.hasNext()) {
            GameObject object = it.next();
            Bounds objectBounds = object.getBoundsInParent();
            if (objectBounds.intersects((player.localToScene(player.getBody().getBoundsInParent())))) {
                player.setTranslateX(player.getPosition().getX());
                player.setTranslateZ(player.getPosition().getZ());
                break;
            }
        }
        player.getPosition().setX(player.getTranslateX());
        player.getPosition().setZ(player.getTranslateZ());
    }

    private void createGameObjects() {
        player = new Player(new Vector(0, -Player.HEIGHT / 1.5, -FLOOR_LENGTH / 2 + SAFE_ZONE_LENGTH / 2), this);

        ambientLight = new AmbientLight(defaultAmbientColor);

        floorWithSpikes = new FloorWithSpikes(FLOOR_WIDTH, FLOOR_LENGTH - 2 * SAFE_ZONE_LENGTH, TILES_X, TILES_Z);
        floorWithSpikes.setTranslateY(FloorWithSpikes.FLOOR_HEIGHT / 2);
        Spikes[][] spikes = floorWithSpikes.getSpikes();
        for (Spikes[] s : spikes) {
            collisionObjects.addAll(Arrays.asList(s));
        }

        Floor floor1 = new Floor(FLOOR_WIDTH, SAFE_ZONE_LENGTH);
        floor1.setTranslateY(FloorWithSpikes.FLOOR_HEIGHT / 2);
        floor1.setTranslateZ(-FLOOR_LENGTH / 2 + SAFE_ZONE_LENGTH / 2);
        Floor floor2 = new Floor(FLOOR_WIDTH, SAFE_ZONE_LENGTH);
        floor2.setTranslateY(FloorWithSpikes.FLOOR_HEIGHT / 2);
        floor2.setTranslateZ(FLOOR_LENGTH / 2 - SAFE_ZONE_LENGTH / 2);

        collisionObjects.add(new Wall(new Vector(0, 0, FLOOR_LENGTH / 2 + Wall.DEFAULT_THICKNESS / 2), FLOOR_WIDTH, Wall.DEFAULT_HEIGHT, Wall.DEFAULT_THICKNESS));
        collisionObjects.add(new Wall(new Vector(0, 0, -FLOOR_LENGTH / 2 - Wall.DEFAULT_THICKNESS / 2), FLOOR_WIDTH, Wall.DEFAULT_HEIGHT, Wall.DEFAULT_THICKNESS));
        collisionObjects.add(new Wall(new Vector(-FLOOR_WIDTH / 2 - Wall.DEFAULT_THICKNESS / 2, 0, 0), Wall.DEFAULT_THICKNESS, Wall.DEFAULT_HEIGHT, FLOOR_LENGTH));
        collisionObjects.add(new Wall(new Vector(FLOOR_WIDTH / 2 + Wall.DEFAULT_THICKNESS / 2, 0, 0), Wall.DEFAULT_THICKNESS, Wall.DEFAULT_HEIGHT, FLOOR_LENGTH));

        /* add to mainSubsceneRoot */
        root.getChildren().addAll(player, ambientLight, floorWithSpikes, floor1, floor2);
        root.getChildren().addAll(collisionObjects);
        /* *********************** */
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        scene = new Scene(root = new Group(), WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        createGameObjects();
        scene.setFill(Color.BLUE);
        scene.setCamera(player.getView());

        scene.setOnMouseMoved(player);
        scene.setOnKeyPressed(player);
        scene.setOnKeyReleased(player);
        // CLASS Player KEEPS THE CURSOR CENTERED.
        scene.setCursor(Cursor.NONE);

        primaryStage.setTitle("Arena");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();

        timer.start();
    }

    public int getCenterX() {
        return (int) (stage.getX() + stage.getWidth() / 2.0);
    }

    public int getCenterY() {
        return (int) (stage.getY() + stage.getHeight() / 2.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
