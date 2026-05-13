package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToolbarPanelTest {

    @Test
    void capsSpacingOnWideToolbars() {
        assertEquals(32.0, ToolbarPanel.calculateSpacing(784, 8));
    }

    @Test
    void shrinksSpacingToFitNarrowToolbars() {
        assertEquals(26.857, ToolbarPanel.calculateSpacing(528, 8), 0.001);
    }

    @Test
    void neverUsesNegativeSpacing() {
        assertEquals(0.0, ToolbarPanel.calculateSpacing(300, 8));
    }

    @Test
    void disablesUndoUntilAnUndoIsActuallyAvailable() {
        assertTrue(ToolbarPanel.isUndoDisabled(UndoManager.INITIAL_UNDOS, false));
        assertFalse(ToolbarPanel.isUndoDisabled(UndoManager.INITIAL_UNDOS, true));
        assertTrue(ToolbarPanel.isUndoDisabled(0, true));
    }
}
