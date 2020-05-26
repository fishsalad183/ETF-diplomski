package game;

import geometry.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import objects.Cabinet;
import objects.CeilingLight;
import objects.Dishwasher;
import objects.Door;
import objects.Drawers;
import objects.FloorCeiling;
import objects.GameObject;
import interfaces.Interactive;
import objects.KitchenSink;
import objects.LightSwitch;
import objects.Oven;
import objects.Picture;
import objects.Player;
import objects.Refrigerator;
import interfaces.SpecificallyBounded;
import javafx.scene.ParallelCamera;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import objects.Chair;
import objects.Countertop;
import objects.Plate;
import objects.Table;
import objects.Wall;
import objects.WallWithWindow;

public class Game extends Application {

    public static final int WINDOW_WIDTH = 1366;
    public static final int WINDOW_HEIGHT = 768;

    private Group root;

    private Scene scene;
    private Stage stage;

    private static final double FLOOR_WIDTH = 2000;
    private static final double FLOOR_LENGTH = 1100;
    private static final double ROOM_HEIGHT = 300;

    private AmbientLight ambientLight;
    private final Color defaultAmbientColor = new Color(.9, .9, .9, 1.0);

    private final ArrayList<Camera> cameras = new ArrayList<>();
    private final ParallelCamera parallelCam = new ParallelCamera();
    private static final double PARALLEL_CAM_SPEED = 20;
    private static final double CORNER_CAMERA_VERTICAL_ANGLE;
    private static final double CORNER_CAMERA_HORIZONTAL_ANGLE_1;   // for the camera at x < 0 && z < 0

    static {
        final double x = (FLOOR_WIDTH / 2 - Door.DEPTH / 2) / 2;
        final double roomCenterFloorDiagonal = Math.sqrt(Math.pow(x, 2) + Math.pow(FLOOR_LENGTH / 2, 2));
        CORNER_CAMERA_VERTICAL_ANGLE = -90 + Math.atan2(roomCenterFloorDiagonal, ROOM_HEIGHT) * 180 / Math.PI;  // -90 is necessary because the angle is between a vertical (y-axis) line and the diagonal from the corner to the center of the room

        CORNER_CAMERA_HORIZONTAL_ANGLE_1 = Math.asin(x / roomCenterFloorDiagonal) * 180 / Math.PI;
    }

    private Player player;

    private final ArrayList<GameObject> collisionObjects = new ArrayList<>();
    private final ArrayList<Interactive> interactiveObjects = new ArrayList<>();
    private FloorCeiling ceiling;
    private Oven oven;  // If there are several dangerous kitchen appliances, then it should be a list.

    private final UpdateTimer timer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {

        @Override
        public void handle(long now) {

            player.update();
            checkForCollisions();
            checkProximityToDanger();
        }
    }

    private void checkProximityToDanger() {
        final double DANGEROUS_PROXIMITY = 230;
        double distance = Math.sqrt(Math.pow(player.getTranslateX() - oven.getTranslateX(), 2) + Math.pow(player.getTranslateZ() - oven.getTranslateZ(), 2));
        if (distance >= DANGEROUS_PROXIMITY) {
            ambientLight.setColor(defaultAmbientColor);
        } else {
            double r = defaultAmbientColor.getRed(), g = defaultAmbientColor.getGreen(), b = defaultAmbientColor.getBlue(), o = defaultAmbientColor.getOpacity();
            ambientLight.setColor(new Color(r, g * distance / DANGEROUS_PROXIMITY, b * distance / DANGEROUS_PROXIMITY, o));
        }
    }

    public void interact() {
        Iterator<Interactive> it = interactiveObjects.iterator();
        while (it.hasNext()) {
            Interactive object = it.next();
            if (((GameObject) object).getBoundsInParent().intersects(player.getInteractionBounds())) {
                object.interact();
                break;
            }
        }
    }

    private void checkForCollisions() {
        Iterator<GameObject> it = collisionObjects.iterator();
        while (it.hasNext()) {
            GameObject object = it.next();
            Bounds objectBounds = object instanceof SpecificallyBounded ? ((SpecificallyBounded) object).getSpecificBounds() : object.getBoundsInParent();
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
        player = new Player(new Vector(-Door.DEPTH * 3, -Player.HEIGHT / 2, 0), this);
        player.setVisible(false);

        FloorCeiling floor = new FloorCeiling(FLOOR_WIDTH, FLOOR_LENGTH, FloorCeiling.Type.FLOOR);
        floor.setTranslateY(FloorCeiling.FLOOR_HEIGHT / 2);

        ceiling = new FloorCeiling(FLOOR_WIDTH, FLOOR_LENGTH, FloorCeiling.Type.CEILING);
        ceiling.setTranslateY(-(ROOM_HEIGHT + 0.5));

        FloorCeiling ground = new FloorCeiling(FLOOR_WIDTH * 2.5, FLOOR_LENGTH * 2.5, FloorCeiling.Type.GROUND);
        ground.setTranslateY(FloorCeiling.FLOOR_HEIGHT * 1.5);

        final double exteriorWallAndWindowDepth = Door.DEPTH;

        // z > 0 && x < 0 walls:
        final double wallsPositiveZ = FLOOR_LENGTH / 2 + exteriorWallAndWindowDepth / 2;
        collisionObjects.add(new WallWithWindow(new Vector(-FLOOR_WIDTH / 4, 0, wallsPositiveZ), FLOOR_WIDTH / 2, ROOM_HEIGHT, exteriorWallAndWindowDepth, FLOOR_WIDTH / 6, 0.7));

        // z > 0 && x > 0 walls:
        collisionObjects.add(new WallWithWindow(new Vector(FLOOR_WIDTH / 4, 0, wallsPositiveZ), FLOOR_WIDTH / 2, ROOM_HEIGHT, exteriorWallAndWindowDepth, FLOOR_WIDTH / 4, 0.5));

        // z < 0 && x < 0 walls:
        final double wallsNegativeZ = -FLOOR_LENGTH / 2 - exteriorWallAndWindowDepth / 2;
        collisionObjects.add(new WallWithWindow(new Vector(-FLOOR_WIDTH / 4, 0, wallsNegativeZ), FLOOR_WIDTH / 2, ROOM_HEIGHT, exteriorWallAndWindowDepth, FLOOR_WIDTH / 3.5, 0.5));

        // z < 0 && x > 0 walls and door
        final double doorX = FLOOR_LENGTH / 4;
        final double wall1Width = doorX - Door.WIDTH / 2;
        collisionObjects.add(new Wall(new Vector((wall1Width) / 2, 0, wallsNegativeZ), wall1Width, ROOM_HEIGHT, exteriorWallAndWindowDepth));
        collisionObjects.add(new Wall(new Vector(doorX, -Door.HEIGHT, wallsNegativeZ), Door.WIDTH, ROOM_HEIGHT - Door.HEIGHT, exteriorWallAndWindowDepth));
        final double wall2width = FLOOR_WIDTH / 2 - wall1Width - Door.WIDTH;
        collisionObjects.add(new WallWithWindow(new Vector(doorX + Door.WIDTH / 2 + wall2width / 2, 0, wallsNegativeZ), wall2width, ROOM_HEIGHT, exteriorWallAndWindowDepth, wall2width / 2, 0.5));
        Door outsideDoor = new Door(new Vector(doorX, -Door.HEIGHT / 2, wallsNegativeZ));
        outsideDoor.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        collisionObjects.add(outsideDoor);
        interactiveObjects.add(outsideDoor);

        Box doormat = new Box(Door.WIDTH, 1, Door.DEPTH);
        doormat.setTranslateX(doorX);
        doormat.setTranslateY(doormat.getHeight() / 2);
        doormat.setTranslateZ(wallsNegativeZ);
        PhongMaterial doormatMat = new PhongMaterial(Color.KHAKI);
        doormatMat.setDiffuseMap(new Image("resources/doormat.jpg"));
        doormat.setMaterial(doormatMat);

        // Z-axis walls (x < 0 and x > 0):
        collisionObjects.add(new Wall(new Vector(FLOOR_WIDTH / 2 + exteriorWallAndWindowDepth / 2, 0, 0), exteriorWallAndWindowDepth, ROOM_HEIGHT, FLOOR_LENGTH + 2 * exteriorWallAndWindowDepth));
        collisionObjects.add(new Wall(new Vector(-(FLOOR_WIDTH / 2 + exteriorWallAndWindowDepth / 2), 0, 0), exteriorWallAndWindowDepth, ROOM_HEIGHT, FLOOR_LENGTH + 2 * exteriorWallAndWindowDepth));

        // interior walls and door
        final double interiorWallLength = FLOOR_LENGTH / 2 - Door.WIDTH / 2;
        collisionObjects.add(new Wall(new Vector(0, 0, Door.WIDTH / 2 + interiorWallLength / 2), Door.DEPTH, ROOM_HEIGHT, interiorWallLength));
        collisionObjects.add(new Wall(new Vector(0, 0, -(Door.WIDTH / 2 + interiorWallLength / 2)), Door.DEPTH, ROOM_HEIGHT, interiorWallLength));
        collisionObjects.add(new Wall(new Vector(0, -Door.HEIGHT, 0), Door.DEPTH, ROOM_HEIGHT - Door.HEIGHT, Door.WIDTH));
        Door door = new Door(new Vector(0, -Door.HEIGHT / 2, 0));
        door.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        collisionObjects.add(door);
        interactiveObjects.add(door);

        /*
        *****
        KITCHEN:
        *****
         */
        Refrigerator refrigerator = new Refrigerator(new Vector(-FLOOR_WIDTH / 2 + Refrigerator.WIDTH * 3, -Refrigerator.HEIGHT / 2, FLOOR_LENGTH / 2 - Refrigerator.DEPTH / 2));
        collisionObjects.add(refrigerator);
        interactiveObjects.add(refrigerator);

        double zPosition = -FLOOR_LENGTH / 2 + Oven.WIDTH * 2;
        oven = new Oven(new Vector(-FLOOR_WIDTH / 2 + Oven.DEPTH / 2, -Oven.HEIGHT / 2, zPosition));
        oven.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
        collisionObjects.add(oven);
        interactiveObjects.add(oven);

        zPosition += Oven.WIDTH / 2 + Dishwasher.WIDTH / 2;
        Dishwasher dishwasher = new Dishwasher(new Vector(-FLOOR_WIDTH / 2 + Dishwasher.DEPTH / 2, -Dishwasher.HEIGHT / 2, zPosition));
        dishwasher.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
        collisionObjects.add(dishwasher);
        interactiveObjects.add(dishwasher);

        zPosition += Dishwasher.WIDTH / 2 + KitchenSink.WIDTH / 2;
        KitchenSink kitchenSink = new KitchenSink(new Vector(-FLOOR_WIDTH / 2 + KitchenSink.DEPTH / 2, -KitchenSink.HEIGHT / 2, zPosition));
        kitchenSink.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
        collisionObjects.add(kitchenSink);

        Cabinet[] cabinets = new Cabinet[3];
        Vector centralCabinetPosition = new Vector(-FLOOR_WIDTH / 2 + Cabinet.DEPTH / 2, -ROOM_HEIGHT + Cabinet.HEIGHT * 0.6, zPosition);
        cabinets[0] = new Cabinet(centralCabinetPosition, 1);
        cabinets[1] = new Cabinet(centralCabinetPosition.duplicate().add(0, 0, -Cabinet.WIDTH), 1);
        cabinets[2] = new Cabinet(centralCabinetPosition.duplicate().add(0, 0, Cabinet.WIDTH), 1);
        Rotate cabinetsRotate = new Rotate(-90, Rotate.Y_AXIS);
        for (Cabinet c : cabinets) {
            c.getTransforms().add(cabinetsRotate);
            collisionObjects.add(c);
            interactiveObjects.add(c);
        }

        double drawersXScale = KitchenSink.WIDTH / Drawers.WIDTH / 2;
        double drawersYScale = KitchenSink.HEIGHT / Drawers.HEIGHT;
        double drawersZScale = KitchenSink.DEPTH / Drawers.DEPTH;
        zPosition += KitchenSink.WIDTH / 2 + Drawers.WIDTH / 2 * drawersXScale;
        Drawers drawers1 = new Drawers(new Vector(-FLOOR_WIDTH / 2 + Drawers.DEPTH / 2 * drawersZScale, -Drawers.HEIGHT / 2 * drawersYScale, zPosition), 3);
        drawers1.setDrawersTopAndBottomMaterial(Countertop.getTopMaterial());
        drawers1.getTransforms().addAll(new Rotate(-90, Rotate.Y_AXIS), new Scale(drawersXScale, drawersYScale, drawersZScale));
        collisionObjects.add(drawers1);
        interactiveObjects.add(drawers1);

        double countertop1ScaleX = (FLOOR_LENGTH / 2 - (zPosition + Drawers.WIDTH / 2 * drawersXScale)) / Countertop.WIDTH;
        zPosition += Drawers.WIDTH / 2 * drawersXScale + Countertop.WIDTH / 2 * countertop1ScaleX;
        Countertop countertop1 = new Countertop(new Vector(-FLOOR_WIDTH / 2 + Countertop.DEPTH / 2, -Countertop.HEIGHT / 2, zPosition));
        countertop1.getTransforms().addAll(new Rotate(-90, Rotate.Y_AXIS), new Scale(countertop1ScaleX, 1, 1));
        collisionObjects.add(countertop1);

        /*
        *****
        DINING ROOM:
        *****
         */
        final double tableX = FLOOR_WIDTH / 2 - Table.DEPTH / 2 - FLOOR_WIDTH / 8;
        Table table = new Table(new Vector(tableX, -Table.HEIGHT / 2, 0));
        table.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        collisionObjects.add(table);

        double x = tableX - Table.DEPTH / 2;
        for (int i = 0; i < 6; i++) {
            double z;
            final double rot;
            if (i == 2 || i == 3) {
                z = Table.WIDTH / 2;
                if (i == 2) {
                    rot = 0;
                } else {
                    rot = 180;
                }
            } else {
                z = Table.WIDTH / 4.75;
                if (i == 0 || i == 1) {
                    rot = -90;
                } else {
                    rot = 90;
                }
            }
            z *= (i % 2 == 0 ? 1 : -1);

            Chair chair = new Chair(new Vector(x, -Chair.HEIGHT / 2, z));
            chair.getTransforms().add(new Rotate(rot, Rotate.Y_AXIS));
            collisionObjects.add(chair);

            if (i % 2 == 1) {
                x += Table.DEPTH / 2;
            }
        }

        Picture pic1 = new Picture(new Vector(FLOOR_WIDTH / 2 - Picture.DEPTH / 2, -ROOM_HEIGHT / 1.5, Picture.LONGER_SIDE), "resources/Imperato2r.jpg");
        pic1.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        Picture pic2 = new Picture(new Vector(FLOOR_WIDTH / 2 - Picture.DEPTH / 2, -ROOM_HEIGHT / 1.5, -Picture.LONGER_SIDE), "resources/Mona Lisa.jpg");
        pic2.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        Picture pic3 = new Picture(new Vector(Door.DEPTH / 2 + Picture.DEPTH / 2, -ROOM_HEIGHT / 1.5, Door.WIDTH / 2 + Picture.LONGER_SIDE * 1.5), "resources/Paul Gauguin.jpg");
        pic3.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));

        Vector cornerDrawersPosition = new Vector(FLOOR_WIDTH / 2 - Drawers.DEPTH / 2 * drawersZScale, -Drawers.HEIGHT / 2 * drawersYScale, FLOOR_LENGTH / 2 - Drawers.WIDTH / 2 * drawersXScale);
        Drawers cornerDrawers = new Drawers(cornerDrawersPosition, 4);
        cornerDrawers.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Scale(drawersXScale, drawersYScale, drawersZScale));
        collisionObjects.add(cornerDrawers);
        interactiveObjects.add(cornerDrawers);

        double cabinetScale = Drawers.DEPTH * drawersZScale / Cabinet.DEPTH;
        Cabinet cornerCabinet = new Cabinet(cornerDrawersPosition.duplicate().add(0, -Drawers.HEIGHT * drawersYScale / 2 - cabinetScale * Cabinet.HEIGHT / 2, 0), 2);
        cornerCabinet.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Scale(cabinetScale, cabinetScale, cabinetScale));
        cornerCabinet.setDoorsMaterial(new PhongMaterial(new Color(0.05, 0.0, 0.25, 0.35)));
        collisionObjects.add(cornerCabinet);
        interactiveObjects.add(cornerCabinet);

        final double cabinetX = cornerCabinet.getPosition().getX();
        final double cabinetZ = cornerCabinet.getPosition().getZ();
        for (int i = -1; i <= 1; i++) {
            Plate[] left = Plate.createStackOfPlates(new Vector(cabinetX, cornerCabinet.getShelfTopY(i, cabinetScale), cabinetZ - cabinetScale * Cabinet.WIDTH / 4), 4);
            Plate[] right = Plate.createStackOfPlates(new Vector(cabinetX, cornerCabinet.getShelfTopY(i, cabinetScale), cabinetZ + cabinetScale * Cabinet.WIDTH / 4), 4);
            root.getChildren().addAll(left);
            root.getChildren().addAll(right);
        }

        ambientLight = new AmbientLight(defaultAmbientColor);

        CeilingLight light1 = new CeilingLight();
        light1.getTransforms().add(new Translate(-FLOOR_WIDTH / 4, -ROOM_HEIGHT, 0));
        CeilingLight light2 = new CeilingLight();
        light2.getTransforms().add(new Translate(FLOOR_WIDTH / 4, -ROOM_HEIGHT, -FLOOR_LENGTH / 4));
        CeilingLight light3 = new CeilingLight();
        light3.getTransforms().add(new Translate(FLOOR_WIDTH / 4, -ROOM_HEIGHT, FLOOR_LENGTH / 4));

        LightSwitch switch1 = new LightSwitch(new Vector(-Door.DEPTH / 2, -ROOM_HEIGHT / 2, Door.WIDTH), light1);
        switch1.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS));
        interactiveObjects.add(switch1);
        LightSwitch switch2 = new LightSwitch(new Vector(Door.DEPTH / 2, -ROOM_HEIGHT / 2, -Door.WIDTH * 1.45), light2);
        switch2.getTransforms().addAll(new Rotate(-90, Rotate.Y_AXIS));
        interactiveObjects.add(switch2);
        LightSwitch switch3 = new LightSwitch(new Vector(Door.DEPTH / 2, -ROOM_HEIGHT / 2, -Door.WIDTH * 0.9), light3);
        switch3.getTransforms().addAll(new Rotate(-90, Rotate.Y_AXIS));
        interactiveObjects.add(switch3);

        /*
        *****
        add to root:
        *****
         */
        root.getChildren().addAll(player, floor, ground, ceiling, doormat, pic1, pic2, pic3, ambientLight, light1, light2, light3, switch1, switch2, switch3);
        root.getChildren().addAll(collisionObjects);
    }

    private void createCameras() {
        cameras.add(player.getView());

        for (int i = 0; i < 4; i++) {
            PerspectiveCamera cam = new PerspectiveCamera(true);
            cam.setFarClip(Player.FAR_CLIP);
            cam.setFieldOfView(60);
            final int factor1 = i < 2 ? 1 : -1;
            final int factor2 = i % 2 == 0 ? 1 : -1;
            final double horizontalAngle = (CORNER_CAMERA_HORIZONTAL_ANGLE_1 + 90 * (i % 2)) * factor1;
            // A simpler way to get the rotations right:
            // make a rotation around X-axis and
            // then add the camera to a carrier Group, which will have its translation and additional rotation.
            cam.getTransforms().addAll(
                    new Rotate(CORNER_CAMERA_VERTICAL_ANGLE, Rotate.X_AXIS)
            );
            Group carrier = new Group(cam);
            root.getChildren().add(carrier);
            carrier.getTransforms().addAll(
                    new Translate((-FLOOR_WIDTH / 2 + 3) * factor1, -ROOM_HEIGHT + 3, (-FLOOR_LENGTH / 2 + 3) * factor2),
                    new Rotate(horizontalAngle, Rotate.Y_AXIS)
            );
            cameras.add(cam);
        }

        parallelCam.getTransforms().addAll(
                new Rotate(-90, Rotate.X_AXIS),
                new Translate(-WINDOW_WIDTH / 2, -WINDOW_HEIGHT / 2)
        );
        cameras.add(parallelCam);
    }

    public void setCamera(int cameraIndex) {
        Camera cam = cameras.get(cameraIndex);
        if (cam == parallelCam) {
            ceiling.setVisible(false);
        } else {
            ceiling.setVisible(true);
        }
        scene.setCamera(cam);
    }

    public void moveParallelCameraHorizontal(boolean right) {
        final double newX = parallelCam.getTranslateX() + (right ? 1 : -1) * PARALLEL_CAM_SPEED;
        if (newX < FLOOR_WIDTH / 2 && newX > -FLOOR_WIDTH / 2) {
            parallelCam.setTranslateX(newX);
        }
    }

    public void moveParallelCameraVertical(boolean up) {
        final double newZ = parallelCam.getTranslateZ() + (up ? 1 : -1) * PARALLEL_CAM_SPEED;
        if (newZ < FLOOR_LENGTH / 2 && newZ > -FLOOR_LENGTH / 2) {
            parallelCam.setTranslateZ(newZ);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        root = new Group();
        createGameObjects();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);

        createCameras();
        scene.setCamera(player.getView());
        scene.setOnMouseMoved(player);
        scene.setOnKeyPressed(player);
        scene.setOnKeyReleased(player);
        // CLASS Player KEEPS THE CURSOR CENTERED.
        scene.setCursor(Cursor.NONE);
        scene.setFill(Color.LIGHTCYAN);

        primaryStage.setTitle("Moja trpezarija");
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
