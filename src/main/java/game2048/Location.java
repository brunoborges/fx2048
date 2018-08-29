package game2048;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author bruno.borges@oracle.com
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Location {

    public static final int CELL_SIZE = 128;

    private final int x;
    private final int y;

    Location offset(Direction direction) {
        return new Location(x + direction.getX(), y + direction.getY());
    }

    double getLayoutY() {
        return (y * CELL_SIZE) + CELL_SIZE / 2.;
    }

    double getLayoutX() {
        return (x * CELL_SIZE) + CELL_SIZE / 2.;
    }

}
