package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GamePaneTest {

    @Test
    void smallerGridsDoNotScaleGameChromeAboveDefaultGridScale() {
        var availableWidth = 1200.0;
        var availableHeight = 900.0;
        var fourByFourWidth = Board.layoutWidthForGridSize(4);
        var fourByFourHeight = Board.layoutHeightForGridSize(4);
        var defaultWidth = Board.layoutWidthForGridSize(GridOperator.DEFAULT_GRID_SIZE);
        var defaultHeight = Board.layoutHeightForGridSize(GridOperator.DEFAULT_GRID_SIZE);

        var scale = GamePane.calculateGameScale(
                availableWidth,
                availableHeight,
                fourByFourWidth,
                fourByFourHeight,
                defaultWidth,
                defaultHeight);

        assertEquals(Math.min(availableWidth / defaultWidth, availableHeight / defaultHeight), scale);
    }

    @Test
    void largerGridsStillScaleDownToFit() {
        var availableWidth = 1200.0;
        var availableHeight = 900.0;
        var largeGridWidth = Board.layoutWidthForGridSize(8);
        var largeGridHeight = Board.layoutHeightForGridSize(8);
        var defaultWidth = Board.layoutWidthForGridSize(GridOperator.DEFAULT_GRID_SIZE);
        var defaultHeight = Board.layoutHeightForGridSize(GridOperator.DEFAULT_GRID_SIZE);

        var scale = GamePane.calculateGameScale(
                availableWidth,
                availableHeight,
                largeGridWidth,
                largeGridHeight,
                defaultWidth,
                defaultHeight);

        assertEquals(Math.min(availableWidth / largeGridWidth, availableHeight / largeGridHeight), scale);
    }
}
