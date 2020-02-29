package objects;

import concepts.GameObject;
import concepts.Updatable;
import game.Game;
import concepts.Vector;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class Player extends GameObject implements EventHandler<Event>, Updatable {

    public static final double HEIGHT = 190;
    public static final double MOUSE_SENSITIVITY = 0.05;

    public static final double WALK_SPEED = 12;
    public static final double TEST_MODE_WALK_SPEED = 40;
    public static final double SPRINT_FACTOR = 1.9;
    public static final double SPRINT_SPEED = WALK_SPEED * SPRINT_FACTOR;
    public static final double TEST_MODE_SPRINT_SPEED = TEST_MODE_WALK_SPEED * SPRINT_FACTOR;
    // Values divided by square root of 2 are used in calculations.
    private static final double WALK_SPEED_DIVIDED_BY_SQRT2;
    private static final double TEST_MODE_WALK_SPEED_DIVIDED_BY_SQRT2;
    private static final double SPRINT_SPEED_DIVIDED_BY_SQRT2;
    private static final double TEST_MODE_SPRINT_SPEED_DIVIDED_BY_SQRT2;

    static {
        final double sqrt2 = Math.sqrt(2);
        WALK_SPEED_DIVIDED_BY_SQRT2 = WALK_SPEED / sqrt2;
        TEST_MODE_WALK_SPEED_DIVIDED_BY_SQRT2 = TEST_MODE_WALK_SPEED / sqrt2;
        SPRINT_SPEED_DIVIDED_BY_SQRT2 = SPRINT_SPEED / sqrt2;
        TEST_MODE_SPRINT_SPEED_DIVIDED_BY_SQRT2 = TEST_MODE_SPRINT_SPEED / sqrt2;
    }

    private double longitudinalVelocity = 0;
    private double lateralVelocity = 0;

    private enum LongitudinalStates {
        FORWARD, BACKWARD, STALL;
    }

    private enum LateralStates {
        LEFT, RIGHT, STALL;
    }

    private LongitudinalStates longitudinalState = LongitudinalStates.STALL;
    private LateralStates lateralState = LateralStates.STALL;

    private Group body;

    public static final double NEAR_CLIP = 1;
    public static final double FAR_CLIP = 18_000;
    private Group viewCarrier = new Group();
    private PerspectiveCamera view;
    private Rotate upDownRotation;
    private Rotate leftRightRotation;

    public static final int MAX_LIVES = 2;
    private int lives = MAX_LIVES;

    public static final int MAX_STAMINA = 60 * 5;
    private int stamina = MAX_STAMINA;
    private boolean sprinting = false;
    private boolean sprintKeyPressed = false;
    public static final int SPRINT_STAMINA_THRESHOLD = (int) (MAX_STAMINA / 5.);
    private static final int STAMINA_REGENERATION_CYCLES = 4;
    private int staminaRegenerationCycleCounter = 0;
    private boolean staminaRegeneratingBelowCritical = false;

    private Game game;

    private Robot mouseMover;   // Keeps the cursor centered.

    public Player(Vector position, Game game) {
        super(position);
        this.game = game;

        // body
        body = new Group();
        
        final double yTranslate = HEIGHT / 7;
        
        Sphere head = new Sphere(HEIGHT / 14);
        head.setTranslateY(-3 * yTranslate);
        
        Box torso = new Box(head.getRadius() * 2.5, HEIGHT / 7 * 3, head.getRadius() * 1.5);
        torso.setTranslateY(-yTranslate);

        Cylinder armLeft = new Cylinder(torso.getDepth() * 0.35, torso.getHeight() * 0.85);
        armLeft.setTranslateX(-torso.getWidth() / 2 - armLeft.getRadius());
        armLeft.setTranslateY(torso.getTranslateY());
        Cylinder armRight = new Cylinder(torso.getDepth() * 0.35, torso.getHeight() * 0.85);
        armRight.setTranslateX(torso.getWidth() / 2 + armLeft.getRadius());
        armRight.setTranslateY(torso.getTranslateY());
        
        Cylinder legLeft = new Cylinder(torso.getDepth() * 0.35, HEIGHT / 7 * 3);
        legLeft.setTranslateX(-torso.getWidth() / 2 + legLeft.getRadius());
        legLeft.setTranslateY(2 * yTranslate);
        Cylinder legRight = new Cylinder(torso.getDepth() * 0.35, HEIGHT / 7 * 3);
        legRight.setTranslateX(torso.getWidth() / 2 - legLeft.getRadius());
        legRight.setTranslateY(2 * yTranslate);
        
        final PhongMaterial bodyMaterial = new PhongMaterial(Color.LIGHTYELLOW);
        torso.setMaterial(bodyMaterial);
        armLeft.setMaterial(bodyMaterial);
        armRight.setMaterial(bodyMaterial);
        legLeft.setMaterial(bodyMaterial);
        legRight.setMaterial(bodyMaterial);

        body.getChildren().addAll(head, torso, armLeft, armRight, legLeft, legRight);
        body.setVisible(false);

        // camera and rotation
        view = new PerspectiveCamera(true);
        view.setNearClip(NEAR_CLIP);
        view.setFarClip(FAR_CLIP);
        view.setFieldOfView(45.0);

        viewCarrier.getChildren().add(view);
        viewCarrier.translateXProperty().bind(body.translateXProperty());
        viewCarrier.translateYProperty().bind(body.translateYProperty().subtract(HEIGHT / 2));
        viewCarrier.translateZProperty().bind(body.translateZProperty());
        upDownRotation = new Rotate(0.0, Rotate.X_AXIS);
        leftRightRotation = new Rotate(0.0, Rotate.Y_AXIS);
        viewCarrier.getTransforms().addAll(upDownRotation, leftRightRotation);
        body.getTransforms().add(leftRightRotation);

        try {
            mouseMover = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.getChildren().addAll(body, viewCarrier);
    }

    public Camera getView() {
        return view;
    }

    public Group getBody() {
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

        double speed;
        if (longitudinal != 0 && lateral != 0) {
            if (!game.isTestMode()) {
                speed = sprinting ? SPRINT_SPEED_DIVIDED_BY_SQRT2 : WALK_SPEED_DIVIDED_BY_SQRT2;
            } else {
                speed = sprinting ? TEST_MODE_SPRINT_SPEED_DIVIDED_BY_SQRT2 : TEST_MODE_WALK_SPEED_DIVIDED_BY_SQRT2;
            }
            longitudinalVelocity = speed * longitudinal;
            lateralVelocity = speed * lateral;
        } else {
            if (!game.isTestMode()) {
                speed = sprinting ? SPRINT_SPEED : WALK_SPEED;
            } else {
                speed = sprinting ? TEST_MODE_SPRINT_SPEED : TEST_MODE_WALK_SPEED;
            }
            longitudinalVelocity = speed * longitudinal;
            lateralVelocity = speed * lateral;
        }
    }

    private void handleKeyEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE && e.getEventType() == KeyEvent.KEY_PRESSED) {
            game.pause();

            // simulate button releasing when the game is paused
            longitudinalState = LongitudinalStates.STALL;
            lateralState = LateralStates.STALL;
            sprintKeyPressed = false;
            setVelocity();

            return;
        }

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

        if (e.getCode() == KeyCode.SHIFT) {
            if (e.getEventType() == KeyEvent.KEY_PRESSED) {
                sprintKeyPressed = true;
            } else if (e.getEventType() == KeyEvent.KEY_RELEASED) {
                sprintKeyPressed = false;
            }
        }

        if (e.getCode() == KeyCode.L && e.getEventType() == KeyEvent.KEY_RELEASED) {
            game.switchMovingLight();
        } else if (e.getCode() == KeyCode.DIGIT1 && e.getEventType() == KeyEvent.KEY_RELEASED) {
            game.switchCamera(1);
        } else if (e.getCode() == KeyCode.DIGIT2 && e.getEventType() == KeyEvent.KEY_RELEASED) {
            game.switchCamera(2);
        } else if (e.getCode() == KeyCode.DIGIT3 && e.getEventType() == KeyEvent.KEY_RELEASED) {
            game.switchCamera(3);
        } else if (e.getCode() == KeyCode.DIGIT4 && e.getEventType() == KeyEvent.KEY_RELEASED) {
            game.switchCamera(4);
        }

        if (e.getCode() == KeyCode.RIGHT && e.getEventType() == KeyEvent.KEY_PRESSED) {
            game.rotateCamCarrierY(true);
        } else if (e.getCode() == KeyCode.LEFT && e.getEventType() == KeyEvent.KEY_PRESSED) {
            game.rotateCamCarrierY(false);
        } else if (e.getCode() == KeyCode.UP && e.getEventType() == KeyEvent.KEY_PRESSED) {
            game.rotateCamCarrierX(true);
        } else if (e.getCode() == KeyCode.DOWN && e.getEventType() == KeyEvent.KEY_PRESSED) {
            game.rotateCamCarrierX(false);
        }
    }

    private void handleScrollEvent(ScrollEvent e) {
        game.translateCamCarrierY(-e.getDeltaY());
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
        } else if (event instanceof ScrollEvent) {
            handleScrollEvent((ScrollEvent) event);
        }
    }

    @Override
    public void update() {
        double horizontalAngleRadians = leftRightRotation.getAngle() * Math.PI / 180;  // Deviation from Z axis.

        this.setTranslateZ(this.getTranslateZ() + Math.cos(horizontalAngleRadians) * longitudinalVelocity);
        this.setTranslateX(this.getTranslateX() + Math.sin(horizontalAngleRadians) * longitudinalVelocity);

        this.setTranslateX(this.getTranslateX() + Math.cos(horizontalAngleRadians) * lateralVelocity);
        this.setTranslateZ(this.getTranslateZ() - Math.sin(horizontalAngleRadians) * lateralVelocity);

        regulateSprinting();
    }

    private void regulateSprinting() {
        if (sprintKeyPressed && stamina > 0 && !staminaRegeneratingBelowCritical && (longitudinalVelocity != 0 || lateralVelocity != 0)) {
            if (sprinting) {    // already sprinting
                --stamina;
            } else {    // start sprinting
                sprinting = true;
                staminaRegenerationCycleCounter = 0;
                staminaRegeneratingBelowCritical = false;
                setVelocity();
            }
        } else {
            if (sprinting) {
                sprinting = false;
                setVelocity();
            }
            if (stamina < MAX_STAMINA) {
                staminaRegeneratingBelowCritical = stamina < SPRINT_STAMINA_THRESHOLD;
                if (++staminaRegenerationCycleCounter >= STAMINA_REGENERATION_CYCLES) {   // >= or ==, possible issues ?
                    ++stamina;
                    staminaRegenerationCycleCounter = 0;
                    if (sprintKeyPressed && !staminaRegeneratingBelowCritical && stamina >= SPRINT_STAMINA_THRESHOLD) {  // in case the player holds the sprint key pressed
                        sprinting = true;
                        setVelocity();
                    }
                }
            }
        }
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

}
