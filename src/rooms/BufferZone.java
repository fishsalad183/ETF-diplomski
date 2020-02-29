package rooms;

import concepts.Animated;
import concepts.GameObject;
import concepts.Vector;
import game.Game;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import objects.Floor;
import objects.FloorRegular;
import objects.Gate;
import objects.Picture;
import objects.Player;
import objects.Wall;

public class BufferZone extends GameObject implements Animated {

    public static final double DEFAULT_FLOOR_WIDTH = 600;
    public static final double DEFAULT_FLOOR_LENGTH = 1200;
    public static final double DEFAULT_ENTRANCE_HEIGHT = Wall.DEFAULT_HEIGHT / 2;

    protected static final Color DEFAULT_FLOOR_COLOR = Color.PERU;
    public static final PhongMaterial FLOOR_MATERIAL = new PhongMaterial(DEFAULT_FLOOR_COLOR);

    protected final Game game;

    protected final double width;
    protected final double length;

    private final Gate startGate;
    private final Gate endGate;

    protected final ArrayList<GameObject> collisionObjects = new ArrayList<>();

    public BufferZone(Vector position, Game game, boolean gateAtStart, boolean gateAtEnd) {
        this(position, game, DEFAULT_FLOOR_WIDTH, DEFAULT_FLOOR_LENGTH, gateAtStart, gateAtEnd);
    }

    public BufferZone(Vector position, Game game, double width, double length, boolean gateAtStart, boolean gateAtEnd) {
        super(position);
        this.game = game;
        this.width = width;
        this.length = length;

        final double wallThickness = Wall.DEFAULT_THICKNESS;
        final double wallHeight = Wall.DEFAULT_HEIGHT;
        final double wallLength = length - 2 * Wall.DEFAULT_THICKNESS;
        final double entranceWallHeight = Wall.DEFAULT_HEIGHT - DEFAULT_ENTRANCE_HEIGHT;
        collisionObjects.add(new Wall(new Vector(-width / 2 - wallThickness / 2, -wallHeight / 2, 0), wallThickness, wallHeight, wallLength));
        collisionObjects.add(new Wall(new Vector(width / 2 + wallThickness / 2, -wallHeight / 2, 0), wallThickness, wallHeight, wallLength));
        if (gateAtStart) {
            startGate = new Gate(new Vector(0, -DEFAULT_ENTRANCE_HEIGHT / 2, -length / 2 - Gate.BAR_RADIUS * 7 / 8));
            collisionObjects.add(startGate);
            collisionObjects.add(new Wall(new Vector(0, -wallHeight * 3 / 4, -length / 2 + wallThickness / 2), width, entranceWallHeight, wallThickness));
        } else {
            startGate = null;
            collisionObjects.add(new Wall(new Vector(0, -wallHeight / 2, -length / 2 + wallThickness / 2), width, wallHeight, wallThickness));
        }
        if (gateAtEnd) {
            endGate = new Gate(new Vector(0, -DEFAULT_ENTRANCE_HEIGHT / 2, length / 2 + Gate.BAR_RADIUS * 7 / 8));
            collisionObjects.add(endGate);
            collisionObjects.add(new Wall(new Vector(0, -wallHeight * 3 / 4, length / 2 - wallThickness / 2), width, entranceWallHeight, wallThickness));
        } else {
            endGate = null;
            collisionObjects.add(new Wall(new Vector(0, -wallHeight / 2, length / 2 - wallThickness / 2), width, wallHeight, wallThickness));
        }

        Floor floor = new FloorRegular(new Vector(0, 0, 0), width, length, FLOOR_MATERIAL);

        this.getChildren().addAll(floor);
        this.getChildren().addAll(collisionObjects);
    }

    public BufferZone(Vector position, Game g, boolean gateAtStart, boolean gateAtEnd, String leftWallPictureURL, String rightWallPictureURL) {
        this(position, g, DEFAULT_FLOOR_WIDTH, DEFAULT_FLOOR_LENGTH, gateAtStart, gateAtEnd, leftWallPictureURL, rightWallPictureURL);
    }

    public BufferZone(Vector position, Game g, double w, double l, boolean gateAtStart, boolean gateAtEnd, String leftWallPictureURL, String rightWallPictureURL) {
        this(position, g, w, l, gateAtStart, gateAtEnd);
        
        if (leftWallPictureURL != null) {
            Picture leftPic = new Picture(new Vector(-width / 2 + Picture.TOTAL_THICKNESS / 2, -Player.HEIGHT, 0), leftWallPictureURL);
            leftPic.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
            this.getChildren().add(leftPic);
        }
        if (rightWallPictureURL != null) {
            Picture rightPic = new Picture(new Vector(width / 2 - Picture.TOTAL_THICKNESS / 2, -Player.HEIGHT, 0), rightWallPictureURL);
            rightPic.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
            this.getChildren().add(rightPic);
        }
    }

    public void triggerStartGate() {
        triggerGate(startGate);
    }

    public void triggerEndGate() {
        triggerGate(endGate);
    }
    
    public void openGates() {
        if (startGate != null && !startGate.isOpen()) {
            triggerStartGate();
        }
        if (endGate != null && !endGate.isOpen()) {
            triggerEndGate();
        }
    }
    
    private void triggerGate(Gate gate) {
        if (gate == null) {
            return;
        }
        boolean open = gate.isOpen();
        if (gate.trigger() == true) {
            if (open) {  // the gate will be closed after the animation ends
                this.collisionObjects.add(gate);
                game.getCollisionObjects().add(gate);
            } else {
                this.collisionObjects.remove(gate);
                game.getCollisionObjects().remove(gate);
            }
        }
    }
    
    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public ArrayList<GameObject> getCollisionObjects() {
        return collisionObjects;
    }

}
