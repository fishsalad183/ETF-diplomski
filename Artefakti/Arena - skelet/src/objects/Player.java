package objects;

import game.Game;
import geometry.Vector;
import java.awt.AWTException;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

public class Player extends GameObject implements EventHandler<Event> {

    public static final double HEIGHT = 190;
    public static final double RADIUS = 60;
    public static final double MOUSE_SENSITIVITY = 0.05;

    public static final double PLAYER_VELOCITY = 10;
    private static final double PLAYER_VELOCITY_DIVIDED_BY_SQRT2;   // Used in calculations.
    static {
        PLAYER_VELOCITY_DIVIDED_BY_SQRT2 = PLAYER_VELOCITY / Math.sqrt(2);
    }
    
    private double longitudinalSpeed = 0;
    private double lateralSpeed = 0;

    private enum LongitudinalStates {
        FORWARD, BACKWARD, STALL;
    }

    private enum LateralStates {
        LEFT, RIGHT, STALL;
    }

    private LongitudinalStates longitudinalState = LongitudinalStates.STALL;
    private LateralStates lateralState = LateralStates.STALL;

    private Cylinder body;

    public static final double NEAR_CLIP = 0.1;
    public static final double FAR_CLIP = 10_000;
    private Group head = new Group();
    private PerspectiveCamera view;
    private Rotate upDownRotation;
    private Rotate leftRightRotation;

    private Game game;

    private Robot mouseMover;   // Keeps the cursor centered.

    public Player(Vector position, Game game) {
        super(position);
        this.game = game;

        body = new Cylinder(RADIUS, HEIGHT);
        PhongMaterial transparentMaterial = new PhongMaterial(new Color(1, 1, 1, 0));
        body.setMaterial(transparentMaterial);
        body.setVisible(false);

        view = new PerspectiveCamera(true);
        view.setNearClip(NEAR_CLIP);
        view.setFarClip(FAR_CLIP);
        view.setFieldOfView(45.0);

        head.getChildren().add(view);
        head.translateXProperty().bind(body.translateXProperty());
        head.translateYProperty().bind(body.translateYProperty().subtract(HEIGHT / 2));
        head.translateZProperty().bind(body.translateZProperty());
        upDownRotation = new Rotate(0.0, Rotate.X_AXIS);
        leftRightRotation = new Rotate(0.0, Rotate.Y_AXIS);
        head.getTransforms().addAll(upDownRotation, leftRightRotation);

        try {
            mouseMover = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.getChildren().addAll(body, head);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    public Camera getView() {
        return view;
    }

    public Cylinder getBody() {
        return body;
    }
    
    private void setVelocity() {
        int longitudinal = 0;
        int lateral = 0;

        switch (longitudinalState) {
            case STALL:
                longitudinal = 0;
                break;
            case FORWARD:
                longitudinal = 1;
                break;
            case BACKWARD:
                longitudinal = -1;
                break;
            default:
                break;
        }

        switch (lateralState) {
            case STALL:
                lateral = 0;
                break;
            case RIGHT:
                lateral = 1;
                break;
            case LEFT:
                lateral = -1;
                break;
            default:
                break;
        }

        if (longitudinal != 0 && lateral != 0) {
            longitudinalSpeed = PLAYER_VELOCITY_DIVIDED_BY_SQRT2 * longitudinal;
            lateralSpeed = PLAYER_VELOCITY_DIVIDED_BY_SQRT2 * lateral;
        } else {
            longitudinalSpeed = PLAYER_VELOCITY * longitudinal;
            lateralSpeed = PLAYER_VELOCITY * lateral;
        }
    }

    private void handleKeyEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.W && e.getEventType() == KeyEvent.KEY_PRESSED) {
            longitudinalState = LongitudinalStates.FORWARD;
            setVelocity();
        } else if (e.getCode() == KeyCode.S && e.getEventType() == KeyEvent.KEY_PRESSED) {
            longitudinalState = LongitudinalStates.BACKWARD;
            setVelocity();
        } else if (e.getCode() == KeyCode.W && e.getEventType() == KeyEvent.KEY_RELEASED && longitudinalState != LongitudinalStates.BACKWARD) {
            longitudinalState = LongitudinalStates.STALL;
            setVelocity();
        } else if (e.getCode() == KeyCode.S && e.getEventType() == KeyEvent.KEY_RELEASED && longitudinalState != LongitudinalStates.FORWARD) {
            longitudinalState = LongitudinalStates.STALL;
            setVelocity();
        }

        if (e.getCode() == KeyCode.A && e.getEventType() == KeyEvent.KEY_PRESSED) {
            lateralState = LateralStates.LEFT;
            setVelocity();
        } else if (e.getCode() == KeyCode.D && e.getEventType() == KeyEvent.KEY_PRESSED) {
            lateralState = LateralStates.RIGHT;
            setVelocity();
        } else if (e.getCode() == KeyCode.A && e.getEventType() == KeyEvent.KEY_RELEASED && lateralState != LateralStates.RIGHT) {
            lateralState = LateralStates.STALL;
            setVelocity();
        } else if (e.getCode() == KeyCode.D && e.getEventType() == KeyEvent.KEY_RELEASED && lateralState != LateralStates.LEFT) {
            lateralState = LateralStates.STALL;
            setVelocity();
        }
        
        if (e.getCode() == KeyCode.ESCAPE && e.getEventType() == KeyEvent.KEY_PRESSED) {
            System.exit(0);
        }
    }

    private void handleMouseEvent(MouseEvent e) {
        if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            double horizontalMouseMove = e.getScreenX() - game.getCenterX();
            double horizontalAngle = leftRightRotation.getAngle() + horizontalMouseMove * MOUSE_SENSITIVITY;
            leftRightRotation.setAngle(horizontalAngle);

            double verticalMouseMove = e.getScreenY() - game.getCenterY();
            double verticalAngle = upDownRotation.getAngle() - verticalMouseMove * MOUSE_SENSITIVITY;
            if (verticalAngle > 90.0) {
                verticalAngle = 90.0;
            } else if (verticalAngle < -90.0) {
                verticalAngle = -90.0;
            }
            upDownRotation.setAxis(new Point3D(Math.cos(horizontalAngle * Math.PI / 180), 0, -Math.sin(horizontalAngle * Math.PI / 180)));
            upDownRotation.setAngle(verticalAngle);

            mouseMover.mouseMove(game.getCenterX(), game.getCenterY()); // Keep the cursor in the center of the screen.
        }
    }

    @Override
    public void handle(Event event) {
        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        } else if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        }
    }

    public void update() {
        double horizontalAngleRadians = leftRightRotation.getAngle() * Math.PI / 180;  // Deviation from Z axis.

        this.setTranslateZ(this.getTranslateZ() + Math.cos(horizontalAngleRadians) * longitudinalSpeed);
        this.setTranslateX(this.getTranslateX() + Math.sin(horizontalAngleRadians) * longitudinalSpeed);

        this.setTranslateX(this.getTranslateX() + Math.cos(horizontalAngleRadians) * lateralSpeed);
        this.setTranslateZ(this.getTranslateZ() - Math.sin(horizontalAngleRadians) * lateralSpeed);
    }
}
