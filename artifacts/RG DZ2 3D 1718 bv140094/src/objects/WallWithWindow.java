package objects;

import geometry.Vector;

public class WallWithWindow extends GameObject {

    private final double width;
    private final double height;
    private final double depth;
    private final double windowY;

    private static final double WINDOW_HEIGHT_FACTOR = 0.5;
    private static final double WINDOW_Y_FACTOR = -0.05;

    public WallWithWindow(Vector position, double wallWidth, double wallHeight, double wallDepth, double windowWidth, double relativeWindowPosition) {
        super(position);    // This object's Vector is appropriate in parent's coordinate system, but its children's are not!!!

        if (relativeWindowPosition < 0 || relativeWindowPosition > 1) {
            throw new IllegalArgumentException("relativeWindowPosition must be between 0 and 1");
        }

        width = wallWidth;
        height = wallHeight;
        depth = wallDepth;
        windowY = height * WINDOW_Y_FACTOR;
        
        final double windowX = (relativeWindowPosition - 0.5) * width;
        final double windowHeight = height * WINDOW_HEIGHT_FACTOR;
        
        Window window = new Window(new Vector(windowX, windowY, 0), windowWidth, height / 2, depth);

        final double leftWallWidth = width / 2 - (windowX + windowWidth / 2);
        Wall left = new Wall(new Vector(width / 2 - leftWallWidth / 2, height / 2, 0), leftWallWidth, height, depth);
        final double topWallHeight = height / 2 - windowHeight / 2 + windowY;
        Wall top = new Wall(new Vector(windowX, windowY - windowHeight / 2, 0), windowWidth, topWallHeight, depth);
        final double bottomWallHeight = height - topWallHeight - windowHeight;
        Wall bottom = new Wall(new Vector(windowX, height / 2, 0), windowWidth, bottomWallHeight, depth);
        final double rightWallWidth = width - leftWallWidth - windowWidth;
        Wall right = new Wall(new Vector(-width / 2 + rightWallWidth / 2, height / 2, 0), rightWallWidth, height, depth);
        
        this.getChildren().addAll(window, left, right, top, bottom);
        this.setTranslateX(position.getX());
        this.setTranslateY(position.getY() - height / 2);
        this.setTranslateZ(position.getZ());
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getDepth() {
        return depth;
    }
}
