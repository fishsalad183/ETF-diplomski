package sprites;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

public class Life extends Group {

    private static final double RADIUS = 10;
    public static final double TOTAL_WIDTH = 4 * RADIUS;

    private final Path heart;

    public Life() {
        heart = new Path(
                new MoveTo(0, 0),
                new QuadCurveTo(0, -RADIUS, RADIUS, -RADIUS),
                new CubicCurveTo(RADIUS * 2, -RADIUS, RADIUS * 2, RADIUS, 0, RADIUS * 2.2),
                new CubicCurveTo(-RADIUS * 2, RADIUS, -RADIUS * 2, -RADIUS, -RADIUS, -RADIUS),
                new QuadCurveTo(0, -RADIUS, 0, 0),
                new ClosePath()
        );
        heart.setFill(Color.RED);
        heart.setStrokeWidth(2.);
        this.getChildren().add(heart);
    }

    public void setActive(boolean active) {
        heart.setFill(active ? Color.RED : Color.LIGHTGRAY);
    }

}
