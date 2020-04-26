package objects;

import concepts.GameObject;
import concepts.Vector;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Heart extends GameObject {

    /*
    The heart is made of two halves, each of which is a half-cylinder (half-circle in 2D) at the top that extends to
    a triangular mesh with a rounded side at the bottom. The vertices for a half-heart begin from where two
    half-cylinders will meet (with negative Z).
      _     _
     /1\   /2\     <--- half-cylinders
    /___\ /___\
    \ 1  |  2 /
     \   |   /
      \  |  /      <--- triangular meshes, each with a rounded side
       \ | /
        \|/
     */
    private static final double HALF_CIRCLE_RADIUS = 25;
    private static final double TRIANGULAR_MESH_HEIGHT = 60;
    public static final double TOTAL_HEIGHT = HALF_CIRCLE_RADIUS + TRIANGULAR_MESH_HEIGHT;
    public static final double THICKNESS = 15;

    private static final int VERTICES_IN_HALF_CIRCLE = 13;
    private static final int VERTICES_IN_MESH_CURVE = 9;
    private static final double MAX_CURVE_FACTOR = 0.3;

    private static final PhongMaterial HEART_MATERIAL = new PhongMaterial(Color.CRIMSON);

    private static final double ROTATION_TIME = 2;
    private final RotateTransition rotateTransition;

    public Heart(Vector position) {
        super(position);

        MeshView leftHalf = createHalfHeart();
        leftHalf.getTransforms().add(new Translate(-HALF_CIRCLE_RADIUS, 0, 0));
        MeshView rightHalf = createHalfHeart();
        rightHalf.getTransforms().addAll(
                new Rotate(180., Rotate.Y_AXIS),
                new Translate(-HALF_CIRCLE_RADIUS, 0, 0)
        );

        this.getChildren().addAll(leftHalf, rightHalf);

        rotateTransition = new RotateTransition(Duration.seconds(ROTATION_TIME), this);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.play();
    }

    private static MeshView createHalfHeart() {
        float[] points = new float[(2 * VERTICES_IN_HALF_CIRCLE + 2 * VERTICES_IN_MESH_CURVE) * 3];

        int i = 0;

        // half-cylinder
        double angleStep = (180. / (VERTICES_IN_HALF_CIRCLE - 1)) * Math.PI / 180.;
        double angle = 0;
        while (i < 2 * VERTICES_IN_HALF_CIRCLE * 3) {
            final float x = (float) (HALF_CIRCLE_RADIUS * Math.cos(angle));
            final float y = (float) (-HALF_CIRCLE_RADIUS * Math.sin(angle));
            points[i] = x;
            points[i + 1] = y;
            points[i + 2] = (float) (-THICKNESS / 2);
            points[i + 3] = x;
            points[i + 4] = y;
            points[i + 5] = (float) (THICKNESS / 2);
            angle += angleStep;
            i += 6;
        }

        // rounded triangular mesh
        angleStep = (180. / (VERTICES_IN_MESH_CURVE - 1)) * Math.PI / 180.;
        angle = 0;
        int j = 0;
        while (i < points.length) {
            final double factor = 1 + MAX_CURVE_FACTOR * Math.sin(angle);
            final float x = (float) ((-HALF_CIRCLE_RADIUS + j * (2 * HALF_CIRCLE_RADIUS / (VERTICES_IN_MESH_CURVE - 1))) * factor);
            final float y = (float) (j * TRIANGULAR_MESH_HEIGHT / (VERTICES_IN_MESH_CURVE - 1));
            points[i] = x;
            points[i + 1] = y;
            points[i + 2] = (float) (-THICKNESS / 2);
            points[i + 3] = x;
            points[i + 4] = y;
            points[i + 5] = (float) (THICKNESS / 2);
            angle += angleStep;
            i += 6;
            j++;
        }

        float[] texCoords = {
            0.0f, 1.0f,
            0.5f, 0.0f,
            1.0f, 1.0f
        };

        final int totalVertices = (VERTICES_IN_HALF_CIRCLE + VERTICES_IN_MESH_CURVE) * 2;
        int faces[] = new int[(totalVertices + (VERTICES_IN_HALF_CIRCLE + VERTICES_IN_MESH_CURVE - 2) * 2) * 12];
        int l = 0;
        for (int k = 0; l < (totalVertices - 1) * 12; k++, l += 12) {  // the frame of the half-heart
            faces[l] = k % totalVertices;
            faces[l + 1] = 0;
            faces[l + 2] = (k + 1) % totalVertices;
            faces[l + 3] = 2;
            faces[l + 4] = (k + 2) % totalVertices;
            faces[l + 5] = 1;

            faces[l + 6] = k % totalVertices;
            faces[l + 7] = 0;
            faces[l + 8] = (k + 2) % totalVertices;
            faces[l + 9] = 1;
            faces[l + 10] = (k + 1) % totalVertices;
            faces[l + 11] = 2;
        }
        l -= 12;
        for (int k = 0; l < faces.length; k++, l += 24) {   // front and back of the half-heart
            // front side of the half-heart
            faces[l] = 0;
            faces[l + 1] = 0;
            faces[l + 2] = (2 * (k + 1)) % totalVertices;
            faces[l + 3] = 2;
            faces[l + 4] = (2 * (k + 2)) % totalVertices;
            faces[l + 5] = 1;

            faces[l + 6] = 0;
            faces[l + 7] = 0;
            faces[l + 8] = (2 * (k + 2)) % totalVertices;
            faces[l + 9] = 1;
            faces[l + 10] = (2 * (k + 1)) % totalVertices;
            faces[l + 11] = 2;

            //back side of the half-heart
            faces[l + 12] = 1;
            faces[l + 13] = 0;
            faces[l + 14] = (2 * (k + 2) + 1) % totalVertices;
            faces[l + 15] = 2;
            faces[l + 16] = (2 * (k + 1) + 1) % totalVertices;
            faces[l + 17] = 1;

            faces[l + 18] = 1;
            faces[l + 19] = 0;
            faces[l + 20] = (2 * (k + 1) + 1) % totalVertices;
            faces[l + 21] = 1;
            faces[l + 22] = (2 * (k + 2) + 1) % totalVertices;
            faces[l + 23] = 2;
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        MeshView halfHeart = new MeshView(mesh);

        halfHeart.setMaterial(HEART_MATERIAL);

        return halfHeart;
    }

}
