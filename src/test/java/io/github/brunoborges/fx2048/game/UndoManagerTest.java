package io.github.brunoborges.fx2048.game;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class UndoManagerTest {

    @Test
    void startsWithInitialUndoAllowanceAndCanConsume() {
        var manager = new UndoManager();

        assertEquals(UndoManager.INITIAL_UNDOS, manager.remainingUndos());
        assertTrue(manager.consumeUndo());
        assertEquals(UndoManager.INITIAL_UNDOS - 1, manager.remainingUndos());
    }

    @Test
    void cannotConsumeWhenNoUndosRemain() {
        var manager = new UndoManager();
        while (manager.canUndo()) {
            manager.consumeUndo();
        }

        assertFalse(manager.consumeUndo());
        assertEquals(0, manager.remainingUndos());
    }

    @Test
    void earnsUndosForMilestoneMergesOnlyOncePerMilestone() {
        var manager = new UndoManager();
        manager.consumeUndo();
        manager.consumeUndo();

        manager.awardEarnedUndos(List.of(GameModel.Movement.merge(new Location(0, 0), new Location(1, 0), 512)));
        manager.awardEarnedUndos(List.of(GameModel.Movement.merge(new Location(0, 1), new Location(1, 1), 512)));

        assertEquals(1, manager.remainingUndos());
    }

    @Test
    void earnedUndosAreCapped() {
        var manager = new UndoManager();

        manager.awardEarnedUndos(List.of(
                GameModel.Movement.merge(new Location(0, 0), new Location(1, 0), 512),
                GameModel.Movement.merge(new Location(0, 1), new Location(1, 1), 1024),
                GameModel.Movement.merge(new Location(0, 2), new Location(1, 2), 2048)));

        assertEquals(UndoManager.MAX_UNDOS, manager.remainingUndos());
    }

    @Test
    void restoreResetsAndReappliesMilestoneAwardsFromBoardState() {
        var manager = new UndoManager();
        manager.consumeUndo();
        manager.resetForRestoredBoard(Map.of(
                new Location(0, 0), 2048,
                new Location(1, 0), 0));

        assertEquals(UndoManager.MAX_UNDOS, manager.remainingUndos());
    }
}
