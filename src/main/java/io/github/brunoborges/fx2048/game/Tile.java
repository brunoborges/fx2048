package io.github.brunoborges.fx2048.game;


import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import java.util.Optional;
import io.github.brunoborges.fx2048.ui.Board;

/**
 * @author Bruno Borges
 */
public class Tile extends Label {

    private Integer value;
    private Location location;
    private Boolean merged;

    public static Tile newTile(int value) {
        if (value % 2 != 0) {
            throw new IllegalArgumentException("Tile value must be multiple of 2");
        }

        return new Tile(value);
    }

    private Tile(Integer value) {
        final int squareSize = Board.CELL_SIZE - 13;
        setMinSize(squareSize, squareSize);
        setMaxSize(squareSize, squareSize);
        setPrefSize(squareSize, squareSize);
        setAlignment(Pos.CENTER);

        this.value = value;
        this.merged = false;
        setText(value.toString());
        getStyleClass().addAll("game-label", "game-tile-" + value);

        // Cache the rendered tile as a bitmap so JavaFX reuses it during
        // translate/scale animations instead of re-rasterizing the rounded-rect
        // background and drop-shadow effect every frame (Marlin hotspot). The
        // cache is invalidated automatically when the tile's value/style change.
        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void merge(Tile another) {
        getStyleClass().remove("game-tile-" + value);
        this.value += another.getValue();
        setText(value.toString());
        merged = true;
        getStyleClass().add("game-tile-" + value);
    }

    public Integer getValue() {
        return value;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Tile{" + "value=" + value + ", location=" + location + '}';
    }

    public boolean isMerged() {
        return merged;
    }

    public void clearMerge() {
        merged = false;
    }

    public boolean isMergeable(Optional<Tile> anotherTile) {
        return anotherTile.filter(t -> t.getValue().equals(getValue())).isPresent();
    }
}
