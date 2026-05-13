package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GamePaneTest {

    @Test
    void calculatesScaleFromAvailableSpaceAndStableGameBounds() {
        var availableWidth = 1200.0;
        var availableHeight = 900.0;

        var scale = GamePane.calculateGameScale(
                availableWidth,
                availableHeight,
                Board.layoutWidth(),
                Board.layoutHeight());

        assertEquals(Math.min(availableWidth / Board.layoutWidth(), availableHeight / Board.layoutHeight()), scale);
    }

    @Test
    void boardDimensionsStayStableAcrossGridSizes() {
        assertEquals(784, Board.layoutWidth());
        assertEquals(1086, Board.layoutHeight());
        assertEquals(1.5, Board.calculateGridScale(4));
        assertEquals(1.0, Board.calculateGridScale(GridOperator.DEFAULT_GRID_SIZE));
        assertEquals(0.375, Board.calculateGridScale(16));
    }
}
