package game2048;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * @author bruno.borges@oracle.com
 */
public class Tile extends Label {

    private Integer value;
    private Location location;

    public static Tile newRandomTile() {
        int value = new Random().nextBoolean() ? 2 : 4;
        return new Tile(value);
    }

    public static Tile newTile(int value) {
        return new Tile(value);
    }

    private final Set<Tile> merged = new HashSet<>();

    private Tile(Integer value) {
        // TODO adjust size to be more... err... responsive? :)
        setMinSize(128 - 13, 128 - 13);
        setMaxSize(128 - 13, 128 - 13);
        setPrefSize(128 - 13, 128 - 13);
        setAlignment(Pos.CENTER);

        this.value = value;
        setText(value.toString());
        getStyleClass().add("tile-" + value);
    }

    public void merge(Tile another) {
        getStyleClass().remove("tile-" + value);
        this.value += another.getValue();
        setText(value.toString());
        merged.add(another);
        getStyleClass().add("tile-" + value);
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

    boolean mergedWith(Tile tile) {
        return merged.contains(tile);
    }

}
