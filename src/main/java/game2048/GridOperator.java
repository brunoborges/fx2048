package game2048;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jpereda
 */
class GridOperator {

    public static final int GRID_SIZE = 4;

    private final List<Integer> traversalX;
    private final List<Integer> traversalY;

    GridOperator() {
        this.traversalX = IntStream.range(0, GRID_SIZE).boxed().collect(Collectors.toList());
        this.traversalY = IntStream.range(0, GRID_SIZE).boxed().collect(Collectors.toList());
    }

    void sortGrid(Direction direction) {
        traversalX.sort(direction.equals(Direction.RIGHT) ? Collections.reverseOrder() : Integer::compareTo);
        traversalY.sort(direction.equals(Direction.DOWN) ? Collections.reverseOrder() : Integer::compareTo);
    }

    int traverseGrid(IntBinaryOperator function) {
        AtomicInteger at = new AtomicInteger();
        traversalX.forEach(left -> traversalY.forEach(right -> at.addAndGet(function.applyAsInt(left, right))));
        return at.get();
    }

    void iterateGrid(BiConsumer<Integer, Integer> function) {
        traversalX.forEach(left -> traversalY.forEach(right -> function.accept(left, right)));
    }

    boolean isValidLocation(Location location) {
        return location.getX() >= 0 && location.getX() < GRID_SIZE
                && location.getY() >= 0 && location.getY() < GRID_SIZE;
    }

}
