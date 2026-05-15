package io.github.brunoborges.fx2048.ui;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToolbarPanelTest {

    @Test
    void capsSpacingOnWideToolbars() {
        assertEquals(32.0, ToolbarPanel.calculateSpacing(784, 9));
    }

    @Test
    void shrinksSpacingToFitNarrowToolbars() {
        assertEquals(18.5, ToolbarPanel.calculateSpacing(528, 9), 0.001);
    }

    @Test
    void neverUsesNegativeSpacing() {
        assertEquals(0.0, ToolbarPanel.calculateSpacing(300, 9));
    }

    @Test
    void disablesUndoUntilAnUndoIsActuallyAvailable() {
        assertTrue(ToolbarPanel.isUndoDisabled(UndoManager.INITIAL_UNDOS, false));
        assertFalse(ToolbarPanel.isUndoDisabled(UndoManager.INITIAL_UNDOS, true));
        assertTrue(ToolbarPanel.isUndoDisabled(0, true));
    }
}
