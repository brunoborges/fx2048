package game2048;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Random;

/**
 * @author bruno.borges@oracle.com
 */
@ToString
class Tile extends Label {

    private static final String GAME_LABEL_CLASS_NAME = "game-label";

    private static final String GAME_TILE_CLASS_NAME_PREFIX = "game-tile-";

    private static final int SQUARE_SPAN = 13;

    private static final double VALUE_THRESHOLD = 0.9;

    @Getter
    private int value;
    @Getter
    @Setter
    private Location location;
    private boolean merged;

    static Tile newRandomTile() {
        int value = new Random().nextDouble() < VALUE_THRESHOLD ? 2 : 4;
        return new Tile(value);
    }

    static Tile newTile(int value) {
        return new Tile(value);
    }

    private Tile(int value) {
        setValue(value);
        clearMerge();
        setTileExterior();
    }

    private void setTileExterior() {
        final int squareSize = Board.CELL_SIZE - SQUARE_SPAN;
        setMinSize(squareSize, squareSize);
        setMaxSize(squareSize, squareSize);
        setPrefSize(squareSize, squareSize);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll(GAME_LABEL_CLASS_NAME, GAME_TILE_CLASS_NAME_PREFIX + value);
    }

    void merge() {
        getStyleClass().remove(GAME_TILE_CLASS_NAME_PREFIX + value);
        setValue(this.value <<= 1);
        merged = true;
        getStyleClass().add(GAME_TILE_CLASS_NAME_PREFIX + value);
    }

    private void setValue(int value) {
        this.value = value;
        setText(String.valueOf(value));
    }

    boolean isMerged() {
        return merged;
    }

    void clearMerge() {
        merged = false;
    }

    boolean isMergeable(Tile other) {
        return other != null && other.getValue() == getValue();
    }
}
