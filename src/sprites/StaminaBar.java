package sprites;

import concepts.Updatable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import objects.Player;

public class StaminaBar extends Group implements Updatable {

    public static final double WIDTH = 150;
    public static final double HEIGHT = 30;

    private final Rectangle bar;

    private final Player player;
    private static final double CRITICAL_RATIO = ((double) Player.SPRINT_STAMINA_THRESHOLD) / Player.MAX_STAMINA;

    private static final Color COLOR_NORMAL = Color.LIME;
    private static final Color COLOR_CRITICAL = Color.TOMATO;

    public StaminaBar(Player p) {
        player = p;

        Rectangle frame = new Rectangle(WIDTH, HEIGHT, Color.TRANSPARENT);
        frame.setStroke(Color.BLACK);
        frame.setStrokeType(StrokeType.OUTSIDE);
        frame.setStrokeWidth(2.);
        bar = new Rectangle(WIDTH, HEIGHT, COLOR_NORMAL);
        bar.setStrokeWidth(0);

        this.getChildren().addAll(frame, bar);
    }

    @Override
    public void update() {
        double ratio = ((double) player.getStamina()) / Player.MAX_STAMINA;
        bar.setWidth(WIDTH * ratio);
        bar.setTranslateX(WIDTH * (1. - ratio));
        if (ratio < CRITICAL_RATIO) {
            if (bar.getFill() != COLOR_CRITICAL) {
                bar.setFill(COLOR_CRITICAL);
            }
        } else if (bar.getFill() != COLOR_NORMAL) {
            if (bar.getFill() != COLOR_NORMAL) {
                bar.setFill(COLOR_NORMAL);
            }
        }
    }

}
