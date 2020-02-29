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
import objects.FloorWithSpikes;
import concepts.GameObject;
import concepts.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.animation.Animation;
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
import objects.Picture;
import objects.Projectile;
import objects.Wall;
import rooms.BufferZone;
import rooms.Room;
import rooms.RoomWithBlades;
import rooms.RoomWithProjectiles;
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
    private boolean paused;
    private final List<Animation> pausedAnimations = new ArrayList<>();

    private AmbientLight ambientLight;
    private final Color DEFAULT_AMBIENT_COLOR = new Color(.55, .55, .55, 1);

    private Player player;
    private Life[] lives2D;
    private StaminaBar staminaBar;
    private Timeline damageRedFlash;    // brief ambient color change when hurt
    private Timeline finishWhiteFlash;  // brief ambient color change when a room is successfully passed

    private BufferZone[] bufferZones = null;
    private BufferZone previousBufferZone = null;
    private BufferZone nextBufferZone = null;
    private Room[] rooms = null;
    private int currentRoomIndex = -1;
    private Room currentRoom = null;
    private RoomWithSpikes roomWithSpikes = null;
    private RoomWithProjectiles roomWithProjectiles = null;
    private RoomWithBlades roomWithBlades = null;

    public static final int NUMBER_OF_COINS = 3;

    private int points = 0;
    private Text pointsText;
    private VBox pointsVBox;

    public static final double DEFAULT_STARTING_TIME = 60;
    public static final double TEST_MODE_STARTING_TIME = 7;
    private boolean roomOngoing = false;
    private double remainingTime = DEFAULT_STARTING_TIME;
    private Text remainingTimeText;
    private int clocksAppeared = 0;
    private boolean firstClockTaken = false;

    private boolean heartExists = false;

    private PerspectiveCamera cam;
    private Group camCarrier;
    private Rotate camCarrierRotX, camCarrierRotY;
    public static final double CAM_CARRIER_ROTATE_STEP = 5;

    private final List<GameObject> collisionObjects = new ArrayList<>(); // careful with concurrency issues; potentially use Collections.synchronizedList(new ArrayList<>())

    private final HighScores highScores = HighScores.getInstance();
    private boolean scoreSaved = false;

    private final SoundPlayer soundPlayer = SoundPlayer.getInstance();

    private boolean testMode = false;
    
    private final Menu menu = new Menu(this);   // Due to the importing of settings in the constructor of the Menu class, it is necessary to make sure that Menu is constructed after Game has the soundPlayer field initialized.

    public static final Random RANDOM = new Random();

    private final UpdateTimer timer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {

        @Override
        public void handle(long now) {
            if (!paused) {
                player.update();
                staminaBar.update();

                checkForCollisions();

                if (!theEnd) {
                    currentRoom.update();
                    if (roomOngoing) {
                        manageTimeAndClocks();
                        manageHearts();
                    } else {
                        checkIfRoomEntered();
                    }
                }
            }
        }

        private void manageTimeAndClocks() {
            remainingTime -= 1. / 60;
            remainingTimeText.setText("Time remaining: " + (int) remainingTime);

            if (remainingTime <= 0.15 && finishWhiteFlash.getStatus() != Animation.Status.RUNNING) {
                finishWhiteFlash.play();
            }

            if ((remainingTime <= 10 && clocksAppeared == 0) || (remainingTime <= 5 && clocksAppeared == 1 && firstClockTaken == true)) {
                populateTileInCurrentRoomAndAddToCollisionObjects(FloorWithTiles.PopulateOption.CLOCK);
                ++clocksAppeared;
            } else if (remainingTime <= 0) {
                remainingTimeText.setText("Time remaining: -");
                roomOngoing = false;
                collisionObjects.removeAll(currentRoom.removeUnnecessaryObjectsAndFinalize());

                if (currentRoomIndex < rooms.length - 1) {
                    nextBufferZone.triggerStartGate();
                    nextBufferZone.triggerEndGate();
                    currentRoom = rooms[++currentRoomIndex];
                    for (int i = 0; i < NUMBER_OF_COINS; i++) {
                        populateTileInCurrentRoomAndAddToCollisionObjects(FloorWithTiles.PopulateOption.COIN);
                    }
                } else {
                    for (BufferZone bz : bufferZones) {
                        bz.openGates();
                    }
                    endGame(true, "Well done, you survived!");
                }
            }
        }

        private void manageHearts() {
            if (player.getLives() < Player.MAX_LIVES && !heartExists) {
                if (Game.RANDOM.nextDouble() > 0.998) {
                    populateTileInCurrentRoomAndAddToCollisionObjects(FloorWithTiles.PopulateOption.HEART);
                    heartExists = true;
                }
            }
        }

    }

    private void checkForCollisions() {
        ListIterator<GameObject> it = collisionObjects.listIterator();
        final List<GameObject> forRemovalAfterIterating = new ArrayList<>(2);
        while (it.hasNext()) {
            GameObject currObj = it.next();
            Bounds currObjBounds = currObj.localToScene(currObj.getBoundsInLocal());
            if (currObjBounds.intersects((player.localToScene(player.getBody().getBoundsInParent())))) {
                if (currObj instanceof DamagingObject) {
                    DamagingObject damagingObject = (DamagingObject) currObj;
                    if (damagingObject.isDamaging()) {
                        soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.DAMAGE);
                        player.setLives(player.getLives() - 1);
                        lives2D[lives2D.length - player.getLives() - 1].setActive(false);
                        damageRedFlash.play();

                        damagingObject.setDamaging(false);  // Disables getting hurt from the same currObj in the next instant. It is set back to true in the currObj itself if necessary.

                        if (currObj instanceof Projectile) {
                            it.remove();
                            ((Projectile) currObj).animateDecayAndRemove();
                        }

                        if (player.getLives() == 0) {
                            endGame(false, "Game over, you died!");
                        }
                    }
                    player.setTranslateX(player.getPosition().getX());
                    player.setTranslateZ(player.getPosition().getZ());
                    break;
                } else if (currObj instanceof Coin) {
                    soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.COIN);
                    currentRoom.removeObject(currObj);
                    it.set(currentRoom.populateRandomTile(FloorWithTiles.PopulateOption.COIN, player)); // avoid potential concurrency issues
                    ++points;
                    pointsText.setText("Points: " + points);
                    break;
                } else if (currObj instanceof Clock) {
                    soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.CLOCK);
                    it.remove();
                    currentRoom.removeObject(currObj);
                    if (clocksAppeared == 1) {
                        remainingTime += 30;
                        firstClockTaken = true;
                    } else if (clocksAppeared == 2) {
                        remainingTime += 15;
                    }
                    break;
                } else if (currObj instanceof Heart) {
                    soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.HEART);
                    it.remove();
                    currentRoom.removeObject(currObj);
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
            } else if (currObj instanceof Projectile) { // projectiles colliding with other collision objects
                List<GameObject> collidingWithCurrentProjectile = collisionObjects.stream()
                        .filter(o -> (o != currObj && o.localToScene(o.getBoundsInLocal()).intersects(currObjBounds)))
                        .collect(Collectors.toList());
                // since multiple collision objects may be removed, it is done after iterating
                if (!collidingWithCurrentProjectile.isEmpty()) {
                    forRemovalAfterIterating.addAll(
                            collidingWithCurrentProjectile.stream()
                                    .filter(o -> o instanceof Projectile)
                                    .collect(Collectors.toList())
                    );
                    forRemovalAfterIterating.add(currObj);
                    forRemovalAfterIterating.stream()
                            .filter(o -> o instanceof Projectile)
                            .forEach(o -> ((Projectile) o).animateDecayAndRemove());    // animateDecayAndRemove() performs setDamaging(false)
                }
            }
        }

        collisionObjects.removeAll(forRemovalAfterIterating);

        player.getPosition().setX(player.getTranslateX());
        player.getPosition().setZ(player.getTranslateZ());
    }

    private void checkIfRoomEntered() {
        if (!theEnd && !roomOngoing) {
            if (currentRoom.getBoundsInParent().intersects(player.localToScene(player.getBody().getBoundsInParent()))) {
                roomOngoing = true;
                
                if (!testMode) {
                    remainingTime = DEFAULT_STARTING_TIME;
                } else {
                    remainingTime = TEST_MODE_STARTING_TIME;
                }

                clocksAppeared = 0;
                firstClockTaken = false;
                heartExists = false;

                if (currentRoomIndex > 0) {
                    previousBufferZone.triggerEndGate();
                    previousBufferZone = bufferZones[currentRoomIndex];
                    nextBufferZone.triggerStartGate();
                    nextBufferZone = bufferZones[currentRoomIndex + 1];
                }
            } else if (previousBufferZone.getBoundsInParent().intersects(player.localToScene(player.getBody().getBoundsInParent()))) {
                // TODO: ...?
            }
        }
    }

    private void populateTileInCurrentRoomAndAddToCollisionObjects(FloorWithTiles.PopulateOption option) {  // for convenience
        collisionObjects.add(currentRoom.populateRandomTile(option, player));
    }

    public void switchToMenu(Menu.MenuKind menuKind) {
        if (menuKind == Menu.MenuKind.MAIN) {
            soundPlayer.stopMusic();
        }
        if (!globalRoot.getChildren().contains(menu.getMenuSubscene())) {
            menu.setMenuKind(menuKind);
            globalRoot.getChildren().add(menu.getMenuSubscene());
            globalScene.setOnMouseMoved(null);
            globalScene.setOnKeyPressed(menu);
            globalScene.setOnKeyReleased(menu);
            globalScene.setOnScroll(null);
            globalScene.setCursor(Cursor.DEFAULT);
        } else {
            menu.setMenuKind(menuKind);
        }
    }

    public void switchToGame() {
        globalRoot.getChildren().remove(menu.getMenuSubscene());
        globalScene.setOnMouseMoved(player);
        globalScene.setOnKeyPressed(player);
        globalScene.setOnKeyReleased(player);
        globalScene.setOnScroll(player);
        globalScene.setCursor(Cursor.NONE);
        // CLASS Player KEEPS THE CURSOR CENTERED.
    }

    public void pause() {
        if (!paused) {
            pausedAnimations.clear();
            pausedAnimations.addAll(Arrays.stream(rooms).flatMap(r -> r.getAnimations().stream()).filter(a -> a.getStatus() == Animation.Status.RUNNING).collect(Collectors.toList()));
            pausedAnimations.addAll(Arrays.stream(bufferZones).flatMap(bz -> bz.getAnimations().stream()).filter(a -> a.getStatus() == Animation.Status.RUNNING).collect(Collectors.toList()));
            if (damageRedFlash.getStatus() == Animation.Status.RUNNING) {
                pausedAnimations.add(damageRedFlash);
            }
            if (finishWhiteFlash.getStatus() == Animation.Status.RUNNING) {
                pausedAnimations.add(finishWhiteFlash);
            }
            pausedAnimations.forEach(a -> a.pause());
            switchToMenu(Menu.MenuKind.PAUSE);
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            pausedAnimations.forEach(a -> a.play());
            pausedAnimations.clear();
            switchToGame();
            paused = false;
        }
    }

    private void endGame(boolean victory, String message) {
        theEnd = true;
        saveScoreIfNotAlreadySaved(victory);
        pointsVBox.getChildren().setAll(menu.createHighScoreTable());
        if (victory) {
            soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.VICTORY);
        } else {
            timer.stop();
            soundPlayer.playSoundEffect(SoundPlayer.SoundEffect.DEFEAT);
        }

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
        paused = false;
        scoreSaved = false;

        roomOngoing = false;
        remainingTime = DEFAULT_STARTING_TIME;
        points = 0;
        clocksAppeared = 0;
        firstClockTaken = false;
        heartExists = false;

        collisionObjects.clear();
        pausedAnimations.clear();
        mainSubsceneRoot.getChildren().clear();
        HUDSubsceneRoot.getChildren().clear();
        addGameObjectsToMainSubscene();
        addElementsToHUDSubscene();

        mainSubscene.setFill(Color.BLUE);
        mainSubscene.setCamera(player.getView());

        timer.start();

        switchToGame();

        soundPlayer.playMusic();
    }

    private void addGameObjectsToMainSubscene() {
        ambientLight = new AmbientLight(DEFAULT_AMBIENT_COLOR);
        mainSubsceneRoot.getChildren().add(ambientLight);

        damageRedFlash = new Timeline(
                new KeyFrame(Duration.seconds(0.1), new KeyValue(ambientLight.colorProperty(), Color.RED, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.25), new KeyValue(ambientLight.colorProperty(), Color.RED, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(ambientLight.colorProperty(), DEFAULT_AMBIENT_COLOR, Interpolator.EASE_IN))
        );

        finishWhiteFlash = new Timeline(
                new KeyFrame(Duration.seconds(0.1), new KeyValue(ambientLight.colorProperty(), Color.WHITE, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.25), new KeyValue(ambientLight.colorProperty(), Color.WHITE, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(ambientLight.colorProperty(), DEFAULT_AMBIENT_COLOR, Interpolator.EASE_IN))
        );

        final Vector firstRoomPosition = new Vector(0, 0, 0);
        final Vector playerStartPosition = firstRoomPosition.duplicate().setY(-Player.HEIGHT / 2).setZ(-BufferZone.DEFAULT_FLOOR_LENGTH / 4);

        bufferZones = new BufferZone[4];
        rooms = new Room[3];

        BufferZone bz0 = new BufferZone(firstRoomPosition, this, false, true, "resources/pic1.jpg", "resources/pic2.jpg");
        bufferZones[0] = bz0;
        previousBufferZone = bz0;
        mainSubsceneRoot.getChildren().add(bz0);
        collisionObjects.addAll(bz0.getCollisionObjects());
        previousBufferZone.triggerEndGate();

        roomWithSpikes = new RoomWithSpikes(bz0.getPosition().duplicate().add(0, 0, BufferZone.DEFAULT_FLOOR_LENGTH / 2 + RoomWithSpikes.DEFAULT_LENGTH / 2), this);
        rooms[0] = roomWithSpikes;
        currentRoomIndex = 0;
        currentRoom = roomWithSpikes;
        mainSubsceneRoot.getChildren().add(roomWithSpikes);
        collisionObjects.addAll(roomWithSpikes.getCollisionObjects());

        BufferZone bz1 = new BufferZone(roomWithSpikes.getPosition().duplicate().add(0, 0, RoomWithSpikes.DEFAULT_LENGTH / 2 + BufferZone.DEFAULT_FLOOR_LENGTH / 2), this, true, true, "resources/pic3.jpg", "resources/pic4.jpg");
        bufferZones[1] = bz1;
        nextBufferZone = bz1;
        mainSubsceneRoot.getChildren().add(bz1);
        collisionObjects.addAll(bz1.getCollisionObjects());

        roomWithProjectiles = new RoomWithProjectiles(bz1.getPosition().duplicate().add(0, 0, BufferZone.DEFAULT_FLOOR_LENGTH / 2 + RoomWithProjectiles.DEFAULT_LENGTH / 2), this);
        rooms[1] = roomWithProjectiles;
        mainSubsceneRoot.getChildren().add(roomWithProjectiles);
        collisionObjects.addAll(roomWithProjectiles.getCollisionObjects());

        BufferZone bz2 = new BufferZone(roomWithProjectiles.getPosition().duplicate().add(0, 0, RoomWithProjectiles.DEFAULT_LENGTH / 2 + BufferZone.DEFAULT_FLOOR_LENGTH / 2), this, true, true, "resources/pic5.jpg", "resources/pic6.jpg");
        bufferZones[2] = bz2;
        mainSubsceneRoot.getChildren().add(bz2);
        collisionObjects.addAll(bz2.getCollisionObjects());

        roomWithBlades = new RoomWithBlades(bz2.getPosition().duplicate().add(0, 0, BufferZone.DEFAULT_FLOOR_LENGTH / 2 + RoomWithBlades.DEFAULT_LENGTH / 2), this);
        rooms[2] = roomWithBlades;
        mainSubsceneRoot.getChildren().add(roomWithBlades);
        collisionObjects.addAll(roomWithBlades.getCollisionObjects());

        BufferZone bz3 = new BufferZone(roomWithBlades.getPosition().duplicate().add(0, 0, RoomWithBlades.DEFAULT_LENGTH / 2 + BufferZone.DEFAULT_FLOOR_LENGTH / 2), this, true, false);
        bufferZones[3] = bz3;
        mainSubsceneRoot.getChildren().add(bz3);
        collisionObjects.addAll(bz3.getCollisionObjects());

        Picture arenaPic = new Picture(bz3.getPosition().duplicate().add(0, -Player.HEIGHT, BufferZone.DEFAULT_FLOOR_LENGTH / 2 - Wall.DEFAULT_THICKNESS - Picture.TOTAL_THICKNESS), "resources/logo2.png", BufferZone.DEFAULT_FLOOR_WIDTH / 2);
        mainSubsceneRoot.getChildren().add(arenaPic);

        player = new Player(playerStartPosition, this);
        mainSubsceneRoot.getChildren().add(player);

        for (int i = 0; i < NUMBER_OF_COINS; i++) {
            populateTileInCurrentRoomAndAddToCollisionObjects(FloorWithSpikes.PopulateOption.COIN);
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
        camCarrier.setTranslateZ(rooms[0].getTranslateZ());
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
                camCarrier.setTranslateZ(rooms[0].getTranslateZ());
                break;
            case 3:
                mainSubscene.setCamera(cam);
                camCarrier.setTranslateZ(rooms[1].getTranslateZ());
                break;
            case 4:
                mainSubscene.setCamera(cam);
                camCarrier.setTranslateZ(rooms[2].getTranslateZ());
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
        if (currentRoom != null && currentRoom.getMovingLight() != null) {
            currentRoom.getMovingLight().setLightOn(!currentRoom.getMovingLight().isLightOn());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        globalScene = new Scene(globalRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        mainSubscene = new SubScene(mainSubsceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        HUDSubscene = new SubScene(HUDSubsceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        globalRoot.getChildren().addAll(mainSubscene, HUDSubscene);   // THE LATTER IS DRAWN FIRST

        switchToMenu(Menu.MenuKind.MAIN);

        primaryStage.setTitle("Arena");
        primaryStage.setScene(globalScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public int getCenterX() {
        return (int) (stage.getX() + stage.getWidth() / 2.0);
    }

    public int getCenterY() {
        return (int) (stage.getY() + stage.getHeight() / 2.0);
    }

    public void saveScoreIfNotAlreadySaved(boolean gameWon) {
        if (!scoreSaved) {
            highScores.add(menu.getPlayerName(), points, gameWon);
            scoreSaved = true;
        }
    }

    public List<GameObject> getCollisionObjects() {
        return collisionObjects;
    }

    public List<Animation> getPausedAnimations() {
        return pausedAnimations;
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    public HighScores getHighScores() {
        return highScores;
    }

    public Scene getGlobalScene() {
        return globalScene;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
