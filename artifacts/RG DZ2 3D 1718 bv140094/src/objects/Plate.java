package objects;

import geometry.Vector;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class Plate extends GameObject {

    public static final double RADIUS = 20;
    public static final double INNER_RADIUS = RADIUS * 2 / 3;
    public static final double HEIGHT = 6;
    public static final double INNER_HEIGHT = HEIGHT / 4.5;
    public static final double OUTER_HEIGHT = HEIGHT - INNER_HEIGHT;

    private static final int VERTICES_PER_CIRCLE = 15;

    public Plate(Vector position) {
        super(position);

        Cylinder inner = new Cylinder(INNER_RADIUS, INNER_HEIGHT);
        inner.setTranslateY(INNER_HEIGHT / 2);
        inner.setMaterial(new PhongMaterial(Color.WHITE));

        MeshView outer = createBorder();

        this.getChildren().addAll(inner, outer);

        this.setTranslateX(this.getTranslateX() + position.getX());
        this.setTranslateY(this.getTranslateY() + position.getY());
        this.setTranslateZ(this.getTranslateZ() + position.getZ());
    }

    private MeshView createBorder() {
        float[] points = new float[2 * VERTICES_PER_CIRCLE * 3];
        final double angleStep = (360.0 / VERTICES_PER_CIRCLE) * Math.PI / 180;
        double angleOuter = 0;
        double angleInner = angleStep / 2;
        for (int i = 0; i < points.length; i += 6) {
            points[i] = (float) (RADIUS * Math.cos(angleOuter));
            points[i + 1] = (float) (-OUTER_HEIGHT / 2);
            points[i + 2] = (float) (RADIUS * Math.sin(angleOuter));
            angleOuter += angleStep;

            points[i + 3] = (float) (INNER_RADIUS * Math.cos(angleInner));
            points[i + 4] = 0;
            points[i + 5] = (float) (INNER_RADIUS * Math.sin(angleInner));
            angleInner += angleStep;
        }

        float[] texCoords = {
            0.0f, 1.0f,
            0.5f, 0.0f,
            1.0f, 1.0f
        };

        int[] faces = new int[2 * VERTICES_PER_CIRCLE * 12];
        final int modulo = points.length / 3;
        for (int j = 0, i = 0; i < faces.length; j += 2) {
            faces[i] = j % modulo;
            faces[i + 1] = 0;
            faces[i + 2] = (j + 2) % modulo;
            faces[i + 3] = 2;
            faces[i + 4] = (j + 1) % modulo;
            faces[i + 5] = 1;

            faces[i + 6] = j % modulo;
            faces[i + 7] = 0;
            faces[i + 8] = (j + 1) % modulo;
            faces[i + 9] = 1;
            faces[i + 10] = (j + 2) % modulo;
            faces[i + 11] = 2;

            i += 12;

            faces[i] = (j + 1) % modulo;
            faces[i + 1] = 0;
            faces[i + 2] = (j + 2) % modulo;
            faces[i + 3] = 1;
            faces[i + 4] = (j + 3) % modulo;
            faces[i + 5] = 2;

            faces[i + 6] = (j + 1) % modulo;
            faces[i + 7] = 0;
            faces[i + 8] = (j + 3) % modulo;
            faces[i + 9] = 2;
            faces[i + 10] = (j + 2) % modulo;
            faces[i + 11] = 1;

            i += 12;
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        MeshView outer = new MeshView(mesh);

        PhongMaterial mat = new PhongMaterial(Color.WHITE);
        outer.setMaterial(mat);

        return outer;
    }

    public static Plate[] createStackOfPlates(Vector bottomPosition, int numOfPlates) {
        Plate[] stack = new Plate[numOfPlates];
        final double startY = -INNER_HEIGHT / 2;
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Plate(bottomPosition.duplicate().add(0, startY - i * HEIGHT + i * (i != 0 ? HEIGHT / 2 : 0), 0));
        }
        return stack;
    }
}
