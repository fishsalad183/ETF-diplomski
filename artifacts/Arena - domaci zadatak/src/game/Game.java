package game;

import concepts.DamagingObject;
import java.util.ArrayList;
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
import concepts.GameObject;
import concepts.Vector;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import objects.Player;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import objects.Clock;
import objects.Coin;
import objects.FloorWithFlowers;
import objects.FloorWithTiles;
import objects.Heart;
import objects.Wall;
import rooms.Room;
import rooms.RoomWithSpikes;
import sprites.Life;
import sprites.StaminaBar;

public class Game extends Application {

    public static final double WINDOW_WIDTH = 1366;
    public static final double WINDOW_HEIGHT = 768;

    private Scene globalScene;
    private SubScene mainSubscene;
    private SubScene HUDSubscene;
    private Stage stage;

    private final Group globalRoot = new Group();
    private final Group mainSubsceneRoot = new Group();
    private final StackPane HUDSubsceneRoot = new StackPane();

    private boolean theEnd = false;

    private AmbientLight ambientLight;
    private final Color DEFAULT_AMBIENT_COLOR = new Color(.55, .55, .55, 1);

    private Player player;
    private Life[] lives2D;
    private StaminaBar staminaBar;
    private Timeline damageRedFlash;    // brief ambient color change when hurt
    
    private Room room = null;

    public static final int NUMBER_OF_COINS = 3;

    private int points = 0;
    private Text pointsText;
    private VBox pointsVBox;

    public static final double DEFAULT_STARTING_TIME = 60;
    private double remainingTime = DEFAULT_STARTING_TIME;
    private Text remainingTimeText;
    private int clocksAppeared = 0;
    private boolean firstClockTaken = false;

    private boolean heartExists = false;

    private PerspectiveCamera cam;
    private Group camCarrier;
    private Rotate camCarrierRotX, camCarrierRotY;
    public static final double CAM_CARRIER_ROTATE_STEP = 5;

    private final List<GameObject> collisionObjects = new ArrayList<>();

    public static final Random RANDOM = new Random();

    private final UpdateTimer timer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {

        @Override
        public void handle(long now) {
            if (!theEnd) {
                player.update();
                staminaBar.update();

                checkForCollisions();

                room.update();

                manageTimeAndClocks();
                manageHearts();
            }
        }

        private void manageTimeAndClocks() {
            remainingTime -= 1. / 60;
            remainingTimeText.setText("Time remaining: " + (int) remainingTime);

            if ((remainingTime <= 10 && clocksAppeared == 0) || (remainingTime <= 5 && clocksAppeared == 1 && firstClockTaken == true)) {
                collisionObjects.add(room.populateRandomTile(FloorWithTiles.PopulateOption.CLOCK, player));
                ++clocksAppeared;
            } else if (remainingTime <= 0) {
                remainingTimeText.setText("Time remaining: -");
                endGame("Game over, time is up!");
            }
        }

        private void manageHearts() {
            if (player.getLives() < Player.MAX_LIVES && !heartExists) {
                if (Game.RANDOM.nextDouble() > 0.998) {
                    collisionObjects.add(room.populateRandomTile(FloorWithTiles.PopulateOption.HEART, player));
                    heartExists = true;
                }
            }
        }

    }

    private void checkForCollisions() {
        ListIterator<GameObject> it = collisionObjects.listIterator();
        while (it.hasNext()) {
            GameObject currObj = it.next();
            Bounds currObjBounds = currObj.localToScene(currObj.getBoundsInLocal());
            if (currObjBounds.intersects((player.localToScene(player.getBody().getBoundsInParent())))) {
                if (currObj instanceof DamagingObject) {
                    DamagingObject damagingObject = (DamagingObject) currObj;
                    if (damagingObject.isDamaging()) {
                        player.setLives(player.getLives() - 1);
                        lives2D[lives2D.length - player.getLives() - 1].setActive(false);
                        damageRedFlash.play();

                        damagingObject.setDamaging(false);  // Disables getting hurt from the same currObj in the next instant. It is set back to true in the currObj itself if necessary.

                        if (player.getLives() == 0) {
                            endGame("Game over, you died!");
                        }
                    }
                    player.setTranslateX(player.getPosition().getX());
                    player.setTranslateZ(player.getPosition().getZ());
                    break;
                } else if (currObj instanceof Coin) {
                    room.removeObject(currObj);
                    it.set(room.populateRandomTile(FloorWithTiles.PopulateOption.COIN, player)); // avoid potential concurrency issues
                    ++points;
                    pointsText.setText("Points: " + points);
                    break;
                } else if (currObj instanceof Clock) {
                    it.remove();
                    room.removeObject(currObj);
                    if (clocksAppeared == 1) {
                        remainingTime += 30;
                        firstClockTaken = true;
                    } else if (clocksAppeared == 2) {
                        remainingTime += 15;
                    }
                    break;
                } else if (currObj instanceof Heart) {
                    it.remove();
                    room.removeObject(currObj);
                    if (player.getLives() < Player.MAX_LIVES) {
                        player.setLives(player.getLives() + 1);
                        lives2D[lives2D.length - player.getLives()].setActive(true);
                    }
                    heartExists = false;
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
        Group bottomGroup = new Group();
        HUDSubsceneRoot.getChildren().add(bottomGroup);
        StackPane.setAlignment(bottomGroup, Pos.BOTTOM_CENTER);
        Text text = new Text(message);
        text.getStyleClass().add("hud-text");
        text.setScaleX(2);
        text.setScaleY(2);
        text.setScaleZ(2);
        text.setFill(Color.ALICEBLUE);
        bottomGroup.getChildren().add(text);
    }

    public void reset() {
        timer.stop();

        theEnd = false;

        remainingTime = DEFAULT_STARTING_TIME;
        points = 0;
        clocksAppeared = 0;
        firstClockTaken = false;
        heartExists = false;

        collisionObjects.clear();
        mainSubsceneRoot.getChildren().clear();
        HUDSubsceneRoot.getChildren().clear();
        addGameObjectsToMainSubscene();
        addElementsToHUDSubscene();

        mainSubscene.setFill(Color.BLUE);
        mainSubscene.setCamera(player.getView());

        timer.start();

        globalScene.setOnMouseMoved(player);
        globalScene.setOnKeyPressed(player);
        globalScene.setOnKeyReleased(player);
        globalScene.setOnScroll(player);
        globalScene.setCursor(Cursor.NONE);
        // CLASS Player KEEPS THE CURSOR CENTERED.
    }

    private void addGameObjectsToMainSubscene() {
        ambientLight = new AmbientLight(DEFAULT_AMBIENT_COLOR);
        mainSubsceneRoot.getChildren().add(ambientLight);

        damageRedFlash = new Timeline(
                new KeyFrame(Duration.seconds(0.1), new KeyValue(ambientLight.colorProperty(), Color.RED, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.25), new KeyValue(ambientLight.colorProperty(), Color.RED, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(ambientLight.colorProperty(), DEFAULT_AMBIENT_COLOR, Interpolator.EASE_IN))
        );

        room = new RoomWithSpikes(new Vector(0, 0, 0), this);
        mainSubsceneRoot.getChildren().add(room);
        collisionObjects.addAll(room.getCollisionObjects());

        player = new Player(new Vector(0, -Player.HEIGHT / 2, -RoomWithSpikes.DEFAULT_LENGTH / 2 + RoomWithSpikes.DEFAULT_SAFE_ZONE_LENGTH / 2), this);
        mainSubsceneRoot.getChildren().add(player);

        for (int i = 0; i < NUMBER_OF_COINS; i++) {
            collisionObjects.add(room.populateRandomTile(FloorWithTiles.PopulateOption.COIN, player));
        }

        createCameras();
    }

    private void addElementsToHUDSubscene() {
        HUDSubsceneRoot.setBackground(Background.EMPTY);
        HUDSubsceneRoot.getStylesheets().add("game/styles.css");

        // HUD upper right
        Group rightGroup = new Group();
        HUDSubsceneRoot.getChildren().add(rightGroup);
        StackPane.setAlignment(rightGroup, Pos.TOP_RIGHT);

        pointsVBox = new VBox();
        pointsVBox.getStyleClass().add("hud-vbox");
        rightGroup.getChildren().add(pointsVBox);

        pointsText = new Text();
        pointsText.getStyleClass().add("hud-text");
        pointsText.setTextAlignment(TextAlignment.RIGHT);
        pointsText.setFill(Color.ORCHID);
        pointsText.setText("Points: 0");
        pointsVBox.getChildren().add(pointsText);

        // HUD upper left
        Group leftGroup = new Group();
        HUDSubsceneRoot.getChildren().add(leftGroup);
        StackPane.setAlignment(leftGroup, Pos.TOP_LEFT);

        VBox leftVBox = new VBox();
        leftVBox.getStyleClass().add("hud-vbox");
        leftGroup.getChildren().add(leftVBox);

        remainingTimeText = new Text();
        remainingTimeText.getStyleClass().add("hud-text");
        remainingTimeText.setFill(Color.ORANGE);
        remainingTimeText.setText("Time remaining: -");
        leftVBox.getChildren().add(remainingTimeText);

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        leftVBox.getChildren().add(hbox);
        lives2D = new Life[Player.MAX_LIVES];
        for (int i = 0; i < lives2D.length; i++) {
            lives2D[i] = new Life();
            hbox.getChildren().add(lives2D[i]);
        }

        staminaBar = new StaminaBar(player);
        leftVBox.getChildren().add(staminaBar);
    }

    private void createCameras() {
        cam = new PerspectiveCamera(true);
        cam.setNearClip(Player.NEAR_CLIP);
        cam.setFarClip(Player.FAR_CLIP);
        camCarrier = new Group(cam);
        camCarrier.setTranslateZ(room.getTranslateZ());
        camCarrier.setTranslateY(-Wall.DEFAULT_HEIGHT / 2);
        camCarrierRotX = new Rotate(-15, Rotate.X_AXIS);    // hardcoded starting value
        camCarrierRotY = new Rotate(0, Rotate.Y_AXIS);
        camCarrier.getTransforms().addAll(camCarrierRotY, camCarrierRotX);
        mainSubsceneRoot.getChildren().add(camCarrier);
    }

    public void switchCamera(int number) {
        switch (number) {
            case 1:
                mainSubscene.setCamera(player.getView());
                break;
            case 2:
                mainSubscene.setCamera(cam);
                break;
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
        } else if (newY > -FloorWithFlowers.FLOOR_HEIGHT) {
            newY = -FloorWithFlowers.FLOOR_HEIGHT;
        }
        camCarrier.setTranslateY(newY);
    }

    public void switchMovingLight() {
        room.getMovingLight().setLightOn(!room.getMovingLight().isLightOn());
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        globalScene = new Scene(globalRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        mainSubscene = new SubScene(mainSubsceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        HUDSubscene = new SubScene(HUDSubsceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        globalRoot.getChildren().addAll(mainSubscene, HUDSubscene);   // THE LATTER IS DRAWN FIRST
        
        primaryStage.setTitle("Arena");
        primaryStage.setScene(globalScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
        
        reset();
    }

    public int getCenterX() {
        return (int) (stage.getX() + stage.getWidth() / 2.0);
    }

    public int getCenterY() {
        return (int) (stage.getY() + stage.getHeight() / 2.0);
    }

    public List<GameObject> getCollisionObjects() {
        return collisionObjects;
    }

    public Scene getGlobalScene() {
        return globalScene;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
