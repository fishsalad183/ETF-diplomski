package objects;

import concepts.GameObject;
import concepts.Vector;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;

public class Picture extends GameObject {

    public static final double DEFAULT_LONGER_SIDE_LENGTH = 170;
    public static final double TOTAL_THICKNESS = 12;
    public static final double FRAME_HEIGHT_AND_WIDTH = 7;

    public Picture(Vector position, String pictureURL) {
        this(position, pictureURL, DEFAULT_LONGER_SIDE_LENGTH);
    }

    public Picture(Vector position, String pictureURL, double longerSideLength) {
        super(position);

        Image image = new Image(pictureURL);
        MeshView picture = createPicture(image);

        final double height, width, imgHeight = image.getHeight(), imgWidth = image.getWidth();
        if (imgHeight > imgWidth) {
            height = longerSideLength;
            width = longerSideLength * imgWidth / imgHeight;
        } else {
            width = longerSideLength;
            height = longerSideLength * imgHeight / imgWidth;
        }
        picture.getTransforms().addAll(new Scale(width, height, TOTAL_THICKNESS / 1.2));

        Box frameTop = new Box(width + 2 * FRAME_HEIGHT_AND_WIDTH, FRAME_HEIGHT_AND_WIDTH, TOTAL_THICKNESS);
        frameTop.setTranslateY(-height / 2 - FRAME_HEIGHT_AND_WIDTH / 2);
        Box frameBottom = new Box(width + 2 * FRAME_HEIGHT_AND_WIDTH, FRAME_HEIGHT_AND_WIDTH, TOTAL_THICKNESS);
        frameBottom.setTranslateY(height / 2 + FRAME_HEIGHT_AND_WIDTH / 2);
        Box frameLeft = new Box(FRAME_HEIGHT_AND_WIDTH, height, TOTAL_THICKNESS);
        frameLeft.setTranslateX(width / 2 + FRAME_HEIGHT_AND_WIDTH / 2);
        Box frameRight = new Box(FRAME_HEIGHT_AND_WIDTH, height, TOTAL_THICKNESS);
        frameRight.setTranslateX(-width / 2 - FRAME_HEIGHT_AND_WIDTH / 2);

        PhongMaterial frameMaterial = new PhongMaterial(Color.rgb(0xEF, 0xC7, 0x00));
        frameMaterial.setSpecularColor(Color.GOLDENROD);
        frameTop.setMaterial(frameMaterial);
        frameBottom.setMaterial(frameMaterial);
        frameLeft.setMaterial(frameMaterial);
        frameRight.setMaterial(frameMaterial);

        this.getChildren().addAll(picture, frameTop, frameBottom, frameLeft, frameRight);
    }

    private MeshView createPicture(Image image) {
        float[] vertices = {
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f
        };

        float[] texCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        };

        int[] faces = {
            0, 0, 2, 2, 1, 1,
            0, 0, 1, 1, 2, 2,
            1, 1, 2, 2, 3, 3,
            1, 1, 3, 3, 2, 2,
            4, 0, 0, 2, 5, 1,
            4, 0, 5, 1, 0, 2,
            5, 1, 0, 2, 1, 3,
            5, 1, 1, 3, 0, 2,
            // The image is rotated the same way on front and back.
            5, 0, 7, 2, 4, 1,
            5, 0, 4, 1, 7, 2,
            4, 1, 7, 2, 6, 3,
            4, 1, 6, 3, 7, 2,
            2, 0, 6, 2, 3, 1,
            2, 0, 3, 1, 6, 2,
            1, 1, 6, 2, 7, 3,
            1, 1, 7, 3, 6, 2,
            1, 0, 3, 2, 5, 1,
            1, 0, 5, 1, 3, 2,
            5, 1, 3, 2, 7, 3,
            5, 1, 7, 3, 3, 2,
            4, 0, 6, 2, 0, 1,
            4, 0, 0, 1, 6, 2,
            0, 1, 6, 2, 2, 3,
            0, 1, 2, 3, 6, 2
        };

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        MeshView picture = new MeshView(mesh);

        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseMap(image);
        picture.setMaterial(mat);

        return picture;
    }

}
