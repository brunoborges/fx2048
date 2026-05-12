package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

class GameModelTest {

    @Test
    void moveLeftMergesAdjacentTilesAndTracksScore() {
        var model = modelWith(
                row(2, 2, 2, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0));

        var result = model.move(Direction.LEFT);

        assertTrue(result.tilesMoved());
        assertEquals(4, result.points());
        assertFalse(result.won());
        assertRow(model, 0, 4, 2, 0, 0);
    }

    @Test
    void moveLeftOnlyMergesEachDestinationOnce() {
        var model = modelWith(
                row(2, 2, 2, 2),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0));

        var result = model.move(Direction.LEFT);

        assertTrue(result.tilesMoved());
        assertEquals(8, result.points());
        assertRow(model, 0, 4, 4, 0, 0);
    }

    @Test
    void moveRightSlidesTilesToFarthestAvailableLocation() {
        var model = modelWith(
                row(2, 0, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0));

        var result = model.move(Direction.RIGHT);

        assertTrue(result.tilesMoved());
        assertEquals(0, result.points());
        assertRow(model, 0, 0, 0, 0, 2);
    }

    @Test
    void moveReportsWinningMerge() {
        var model = modelWith(
                row(1024, 1024, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0),
                row(0, 0, 0, 0));

        var result = model.move(Direction.LEFT);

        assertTrue(result.won());
        assertEquals(2048, result.points());
        assertRow(model, 0, 2048, 0, 0, 0);
    }

    @Test
    void fullBoardWithoutPairsHasNoAvailableMergeMovements() {
        var model = modelWith(
                row(2, 4, 2, 4),
                row(4, 2, 4, 2),
                row(2, 4, 2, 4),
                row(4, 2, 4, 2));

        var result = model.move(Direction.LEFT);

        assertFalse(result.tilesMoved());
        assertTrue(model.isFull());
        assertFalse(model.hasMergeMovements());
    }

    @Test
    void fullBoardWithAdjacentPairHasMergeMovements() {
        var model = modelWith(
                row(2, 2, 4, 8),
                row(16, 32, 64, 128),
                row(256, 512, 1024, 2048),
                row(4096, 8192, 16384, 32768));

        assertTrue(model.isFull());
        assertTrue(model.hasMergeMovements());
    }

    @Test
    void addRandomTileFillsAnAvailableLocation() {
        var model = modelWith(
                row(2, 4, 2, 4),
                row(4, 2, 4, 2),
                row(2, 4, 2, 4),
                row(4, 2, 4, 0));

        var addedTile = model.addRandomTile();

        assertTrue(addedTile.isPresent());
        assertEquals(new Location(3, 3), addedTile.get().location());
        assertTrue(addedTile.get().value() == 2 || addedTile.get().value() == 4);
        assertTrue(model.isFull());
    }

    private GameModel modelWith(int[]... rows) {
        var model = new GameModel(new GridOperator(4), new Random(0));
        model.restoreSnapshot(snapshot(rows));
        return model;
    }

    private Map<Location, Integer> snapshot(int[]... rows) {
        var snapshot = new HashMap<Location, Integer>();
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length; x++) {
                snapshot.put(new Location(x, y), rows[y][x]);
            }
        }
        return snapshot;
    }

    private int[] row(int value0, int value1, int value2, int value3) {
        return new int[] { value0, value1, value2, value3 };
    }

    private void assertRow(GameModel model, int y, int value0, int value1, int value2, int value3) {
        var snapshot = model.snapshot();
        assertEquals(value0, snapshot.get(new Location(0, y)));
        assertEquals(value1, snapshot.get(new Location(1, y)));
        assertEquals(value2, snapshot.get(new Location(2, y)));
        assertEquals(value3, snapshot.get(new Location(3, y)));
    }
}
