package objects;

import concepts.GameObject;
import concepts.Vector;
import game.Game;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class FloorWithTiles extends Floor {

    public static final Color DEFAULT_FLOOR_COLOR = Color.BURLYWOOD;
    public static final Image FLOOR_IMAGE = new Image("resources/sand.jpg");

    protected static final double SEPARATOR_THICKNESS = 10;
    protected static final PhongMaterial SEPARATOR_MATERIAL = new PhongMaterial(Color.BLACK);

    public enum PopulateOption {
        COIN, CLOCK, HEART;
    }
    public static final double POPULATING_OBJECT_START_Y = -55;

    protected final Box[][] tiles;

    public FloorWithTiles(Vector position, double width, double length, int numOfTilesX, int numOfTilesZ) {
        super(position);
        if (width <= 0 || length <= 0 || numOfTilesX <= 0 || numOfTilesZ <= 0) {
            throw new IllegalArgumentException("The width, length and number of tiles in rows in columns must all be positive numbers");
        }

        tiles = new Box[numOfTilesX][numOfTilesZ];
        final double tileWidth = width / numOfTilesX;
        final double tileLength = length / numOfTilesZ;
        for (int i = 0; i < numOfTilesX; i++) {
            for (int j = 0; j < numOfTilesZ; j++) {
                tiles[i][j] = new Box(tileWidth, FLOOR_HEIGHT, tileLength);
                tiles[i][j].setTranslateX(-width / 2 + tileWidth / 2 + i * tileWidth);
                tiles[i][j].setTranslateZ(-length / 2 + tileLength / 2 + j * tileLength);
                PhongMaterial tileMaterial = new PhongMaterial(DEFAULT_FLOOR_COLOR);
                tileMaterial.setDiffuseMap(FLOOR_IMAGE);
                tiles[i][j].setMaterial(tileMaterial);
            }
            this.getChildren().addAll(tiles[i]);
        }

        for (int i = 0; i < numOfTilesX - 1; i++) {
            Box separator = new Box(SEPARATOR_THICKNESS, FLOOR_HEIGHT + 4, length);
            separator.setTranslateX(-width / 2 + (i + 1) * tileWidth);
            separator.setTranslateY(-FLOOR_HEIGHT);
            separator.setMaterial(SEPARATOR_MATERIAL);
            this.getChildren().add(separator);
        }
        for (int i = 0; i < numOfTilesZ - 1; i++) {
            Box separator = new Box(width, FLOOR_HEIGHT + 4, SEPARATOR_THICKNESS);
            separator.setTranslateZ(-length / 2 + (i + 1) * tileLength);
            separator.setTranslateY(-FLOOR_HEIGHT);
            separator.setMaterial(SEPARATOR_MATERIAL);
            this.getChildren().add(separator);
        }
    }

    // Possibly TODO: A method for populating a tile with a Collectable or CollectableObject or something that will not create a new object, but use an existing one that is passed as a parameter?
    public GameObject populateRandomTile(PopulateOption option, Player player) {
        int i = Game.RANDOM.nextInt(tiles.length);
        int j = Game.RANDOM.nextInt(tiles[i].length);
        int cnt = 0;
//        while (tiles[i][j].getUserData() != null
//                || isTileActive(i, j)
//                || // potentially use !tiles[i][j].intersects(player.localToScene(player.getBody().getBoundsInParent())) or something alike in case the player position conditions don't work properly
//                ((Math.abs(tiles[i][j].getTranslateX() - player.getTranslateX()) < tiles[i][j].getWidth() / 2)
//                && (Math.abs(tiles[i][j].getTranslateZ() - player.getTranslateZ()) < tiles[i][j].getDepth() / 2))) { // select the next tile
        while (isTileOccupied(i, j, player)) {  // ?
            if (j == tiles[i].length - 1) {
                j = 0;
                if (i == tiles.length - 1) {
                    i = 0;
                } else {
                    i++;
                }
            } else {
                j++;
            }
            if (++cnt == tiles.length * tiles[i].length) {  // against errors (infinite loops) ?
                return null;
            }
        }
        GameObject object = null;
        switch (option) {
            case COIN: {
                object = new Coin(new Vector(tiles[i][j].getTranslateX(), POPULATING_OBJECT_START_Y - Coin.RADIUS, tiles[i][j].getTranslateZ()));
                break;
            }
            case CLOCK: {
                object = new Clock(new Vector(tiles[i][j].getTranslateX(), POPULATING_OBJECT_START_Y - Clock.RADIUS, tiles[i][j].getTranslateZ()));
                break;
            }
            case HEART: {
                object = new Heart(new Vector(tiles[i][j].getTranslateX(), POPULATING_OBJECT_START_Y - Heart.TOTAL_HEIGHT, tiles[i][j].getTranslateZ()));
                break;
            }
        }
        tiles[i][j].setUserData(object);
        return object;
    }

    protected boolean isTileOccupied(int i, int j, Player player) {
        boolean occupiedByObjects = tiles[i][j].getUserData() != null || isTileActive(i, j);
        if (player == null || occupiedByObjects == true) {
            return occupiedByObjects;
        } else {
            boolean occupiedByPlayer = (Math.abs(tiles[i][j].getTranslateX() - player.getTranslateX()) < tiles[i][j].getWidth() / 2)
                    && (Math.abs(tiles[i][j].getTranslateZ() - player.getTranslateZ()) < tiles[i][j].getDepth() / 2);
            // potentially use tiles[i][j].intersects(player.localToScene(player.getBody().getBoundsInParent())) or something alike in case the player position conditions don't work properly
            return occupiedByPlayer;
        }
    }

    protected boolean isTileActive(int i, int j) {
        return false;   // meant to be overriden
    }

    public boolean unpopulateTile(GameObject object) {
        for (Box[] tileRow : tiles) {
            for (Box tile : tileRow) {
                if (tile.getUserData() == object) {
                    tile.setUserData(null);
                    return true;
                }
            }
        }
        return false;
    }

    public Box[][] getTiles() {
        return tiles;
    }

}
