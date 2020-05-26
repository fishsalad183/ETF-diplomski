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
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import objects.Coin;
import objects.Floor;
import objects.Flower;
import objects.Spikes;
import objects.Wall;

public class Game extends Application {

    private static final double WINDOW_WIDTH = 1366;
    private static final double WINDOW_HEIGHT = 768;

    private SubScene mainSubscene;
    private SubScene textSubscene;
    private Stage stage;

    private Group globalRoot;
    private Group mainSubsceneRoot;
    private Group textSubsceneRoot;

    private boolean theEnd = false;

    private static final double FLOOR_WIDTH = 3_000;
    private static final double FLOOR_LENGTH = 5_000;
    private static final double SAFE_ZONE_LENGTH = 750;
    private static final int TILES_X = 4;
    private static final int TILES_Z = 5;
    private FloorWithSpikes floorWithSpikes;

    private Player player;

    private static final int NUMBER_OF_COINS = 3;

    private static final int NUMBER_OF_FLOWERS = 20;

    private AmbientLight ambientLight;
    private final Color defaultAmbientColor = new Color(.7, .7, .7, .7);

    private PointLight centralLight;

    private PointLight movingLight;
    private static final double LIGHT_SOURCE_TIME_ALONG_X = 2;
    private static final double LIGHT_SOURCE_TIME_ALONG_Z = LIGHT_SOURCE_TIME_ALONG_X / FLOOR_WIDTH * FLOOR_LENGTH;
    private static final double LIGHT_DISTANCE_FROM_WALL = 30;

//    private ParallelCamera parallelCam;
//    private static final double PARALLEL_CAM_SPEED = 40;
    private PerspectiveCamera cam;
    private Group camCarrier;
    private Rotate camCarrierRotX, camCarrierRotY;
    public static final double CAM_CARRIER_ROTATE_STEP = 5;

    private int points = 0;
    private Text pointsText;
    private double remainingTime = 60;
    private Text remainingTimeText;

    private final ArrayList<GameObject> collisionObjects = new ArrayList<>();

    private final UpdateTimer timer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {

        @Override
        public void handle(long now) {
            if (!theEnd) {
                player.update();
                checkForCollisions();

                if (Math.random() > 0.98) {
                    floorWithSpikes.triggerSpikesOnRandomTile();
                }

                pointsText.setText("Points: " + points);

                remainingTime -= 1. / 60;
                remainingTimeText.setText("Time remaining: " + (int) remainingTime);
                if (remainingTime <= 0) {
                    endGame("Game over, time is up!");
                    return;
                }
            }
        }
    }

    private void checkForCollisions() {
        Iterator<GameObject> it = collisionObjects.iterator();
        while (it.hasNext()) {
            GameObject object = it.next();
            Bounds objectBounds = object.getBoundsInParent();
            if (objectBounds.intersects((player.localToScene(player.getBody().getBoundsInParent())))) {
                if (object instanceof Spikes) {
                    endGame("Game over, you died!");
                    return;
                } else if (object instanceof Coin) {
                    it.remove();
                    if (floorWithSpikes.unpopulateTile(object) == false) {
                        System.err.println("Could not unpopulate any tile with object " + object);
                    }
                    mainSubsceneRoot.getChildren().remove(object);
                    ++points;
                    GameObject coin = floorWithSpikes.populateRandomTile(FloorWithSpikes.PopulateOption.COIN, player);
                    collisionObjects.add(coin);
                    mainSubsceneRoot.getChildren().add(coin);
                    break;
                } else {
                    player.setTranslateX(player.getPosition().getX());
                    player.setTranslateZ(player.getPosition().getZ());
                    break;
                }
            }
        }
        player.getPosition().setX(player.getTranslateX());
        player.getPosition().setZ(player.getTranslateZ());
    }

    private void endGame(String message) {
        theEnd = true;
        Text text = new Text(message);
        text.setFont(Font.font("Tahoma", 100));
        text.setFill(Color.ALICEBLUE);
        text.setTranslateX(WINDOW_WIDTH / 2 - text.getLayoutBounds().getWidth() / 2);
        text.setTranslateY(WINDOW_HEIGHT / 2 - text.getLayoutBounds().getHeight() / 2);
        textSubsceneRoot.getChildren().add(text);
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

        centralLight = new PointLight();
        centralLight.setTranslateY(-Wall.DEFAULT_HEIGHT * 16);

        movingLight = new PointLight(Color.BLUEVIOLET);
        movingLight.setTranslateX(-FLOOR_WIDTH / 2 + LIGHT_DISTANCE_FROM_WALL);
        movingLight.setTranslateY(-Wall.DEFAULT_HEIGHT / 2);
        movingLight.setTranslateZ(-FLOOR_LENGTH / 2 + LIGHT_DISTANCE_FROM_WALL);
        SequentialTransition seq = new SequentialTransition(movingLight);
        TranslateTransition tx1 = new TranslateTransition(Duration.seconds(LIGHT_SOURCE_TIME_ALONG_X));
        tx1.setByX(FLOOR_WIDTH - 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx2 = new TranslateTransition(Duration.seconds(LIGHT_SOURCE_TIME_ALONG_Z));
        tx2.setByZ(FLOOR_LENGTH - 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx3 = new TranslateTransition(Duration.seconds(LIGHT_SOURCE_TIME_ALONG_X));
        tx3.setByX(-FLOOR_WIDTH + 2 * LIGHT_DISTANCE_FROM_WALL);
        TranslateTransition tx4 = new TranslateTransition(Duration.seconds(LIGHT_SOURCE_TIME_ALONG_Z));
        tx4.setByZ(-FLOOR_LENGTH + 2 * LIGHT_DISTANCE_FROM_WALL);
        seq.getChildren().addAll(tx1, tx2, tx3, tx4);
        seq.setCycleCount(Timeline.INDEFINITE);
        seq.play();

        for (int i = 0; i < NUMBER_OF_COINS; i++) {
            GameObject coin = floorWithSpikes.populateRandomTile(FloorWithSpikes.PopulateOption.COIN, player);
            collisionObjects.add(coin);
        }

        /* add to mainSubsceneRoot */
        mainSubsceneRoot.getChildren().addAll(player, ambientLight, floorWithSpikes, floor1, floor2, centralLight, movingLight);
        mainSubsceneRoot.getChildren().addAll(collisionObjects);
        /* *********************** */

        for (int i = 0; i < NUMBER_OF_FLOWERS; i++) {
            Flower flower = new Flower(new Vector(-FLOOR_WIDTH / 2 + Math.random() * FLOOR_WIDTH, -Flower.STEM_HEIGHT / 2, (FLOOR_LENGTH / 2 - (Math.random() * SAFE_ZONE_LENGTH)) * (Math.random() >= 0.5 ? 1 : -1)));
            flower.getTransforms().add(new Rotate(Math.random() * 360, Rotate.Y_AXIS));
            mainSubsceneRoot.getChildren().add(flower);
        }
    }

    private void createTextSubscene() {
        textSubscene = new SubScene(textSubsceneRoot = new Group(), WINDOW_WIDTH, WINDOW_HEIGHT);

        pointsText = new Text();
        pointsText.setFont(Font.font("Tahoma", 35));
        pointsText.setFill(Color.ORCHID);
        pointsText.setTranslateX(WINDOW_WIDTH / 1.15);
        pointsText.setTranslateY(WINDOW_HEIGHT / 18);

        remainingTimeText = new Text();
        remainingTimeText.setFont(Font.font("Tahoma", 35));
        remainingTimeText.setFill(Color.ORANGE);
        remainingTimeText.setTranslateX(WINDOW_WIDTH / 22);
        remainingTimeText.setTranslateY(WINDOW_HEIGHT / 18);

        textSubsceneRoot.getChildren().addAll(pointsText, remainingTimeText);
    }

    private void createCameras() {
//        parallelCam = new ParallelCamera();
//        parallelCam.getTransforms().addAll(
//                new Rotate(-90, Rotate.X_AXIS),
//                new Translate(-WINDOW_WIDTH / 2, -WINDOW_HEIGHT / 2)
//        );

        cam = new PerspectiveCamera(true);
        cam.setNearClip(Player.NEAR_CLIP);
        cam.setFarClip(Player.FAR_CLIP);
        camCarrier = new Group(cam);
        camCarrier.setTranslateY(-Wall.DEFAULT_HEIGHT / 2);
        camCarrierRotX = new Rotate(-15, Rotate.X_AXIS);    // hardcoded starting value
        camCarrierRotY = new Rotate(0, Rotate.Y_AXIS);
        camCarrier.getTransforms().addAll(camCarrierRotY, camCarrierRotX);
        mainSubsceneRoot.getChildren().add(camCarrier);
    }

    public void switchCamera(int digit) {
        switch (digit) {
            case 1:
                mainSubscene.setCamera(player.getView());
                break;
            case 2:
                mainSubscene.setCamera(cam);
                break;
//            case 3:
//                mainSubscene.setCamera(parallelCam);
//                break;
        }
        if (mainSubscene.getCamera() == player.getView()) {
            player.getBody().setVisible(false);
        } else {
            player.getBody().setVisible(true);
        }
    }

    public void rotateCamCarrierY(boolean right) {
        camCarrierRotY.setAngle(camCarrierRotY.getAngle() + (right ? CAM_CARRIER_ROTATE_STEP : -CAM_CARRIER_ROTATE_STEP));
    }

    public void rotateCamCarrierX(boolean up) {
        double newAngle = camCarrierRotX.getAngle() + (up ? CAM_CARRIER_ROTATE_STEP : -CAM_CARRIER_ROTATE_STEP);
        if (newAngle > 0) {
            newAngle = 0;
        } else if (newAngle < -90) {
            newAngle = -90;
        }
        camCarrierRotX.setAngle(newAngle);
    }

    public void translateCamCarrierY(double deltaY) {
        double newY = camCarrier.getTranslateY() + deltaY;
        if (newY < -cam.getFarClip()) {
            newY = -cam.getFarClip();
        } else if (newY > -Floor.FLOOR_HEIGHT) {
            newY = -Floor.FLOOR_HEIGHT;
        }
        camCarrier.setTranslateY(newY);
    }

//    public void moveParallelCameraHorizontal(boolean right) {
//        final double newX = parallelCam.getTranslateX() + (right ? 1 : -1) * PARALLEL_CAM_SPEED;
//        if (newX < FLOOR_WIDTH / 2 && newX > -FLOOR_WIDTH / 2) {
//            parallelCam.setTranslateX(newX);
//        }
//    }
//
//    public void moveParallelCameraVertical(boolean up) {
//        final double newZ = parallelCam.getTranslateZ() + (up ? 1 : -1) * PARALLEL_CAM_SPEED;
//        if (newZ < FLOOR_LENGTH / 2 && newZ > -FLOOR_LENGTH / 2) {
//            parallelCam.setTranslateZ(newZ);
//        }
//    }
    public void switchLight() {
        movingLight.setLightOn(!movingLight.isLightOn());
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        globalRoot = new Group();
        Scene globalScene = new Scene(globalRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        createTextSubscene();

        mainSubscene = new SubScene(mainSubsceneRoot = new Group(), WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        createGameObjects();
        mainSubscene.setFill(Color.BLUE);
        mainSubscene.setCamera(player.getView());

        globalRoot.getChildren().addAll(mainSubscene, textSubscene);   // THE LATTER IS DRAWN FIRST

        createCameras();

        globalScene.setOnMouseMoved(player);
        globalScene.setOnKeyPressed(player);
        globalScene.setOnKeyReleased(player);
        globalScene.setOnScroll(player);
        // CLASS Player KEEPS THE CURSOR CENTERED.
        globalScene.setCursor(Cursor.NONE);

        primaryStage.setTitle("Arena");
        primaryStage.setScene(globalScene);
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
