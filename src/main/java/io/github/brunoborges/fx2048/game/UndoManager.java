package io.github.brunoborges.fx2048.game;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class UndoManager {
    public static final int INITIAL_UNDOS = 1;
    public static final int MAX_UNDOS = 4;
    private static final List<Integer> EARN_MILESTONES = List.of(512, 1024, 2048);

    private final Set<Integer> earnedMilestones = new HashSet<>();
    private int remainingUndos;
    public UndoManager() {
        resetForNewGame();
    }
    public int remainingUndos() {
        return remainingUndos;
    }
    public boolean canUndo() {
        return remainingUndos > 0;
    }
    public void resetForNewGame() {
        earnedMilestones.clear();
        remainingUndos = INITIAL_UNDOS;
    }
    public void resetForRestoredBoard(Map<Location, Integer> snapshot) {
        resetForNewGame();
        var reachedMilestones = EARN_MILESTONES.stream()
                .filter(milestone -> snapshot.values().stream().anyMatch(value -> value != null && value >= milestone))
                .toList();
        reachedMilestones.forEach(earnedMilestones::add);
        remainingUndos = Math.min(MAX_UNDOS, remainingUndos + reachedMilestones.size());
    }
    public boolean consumeUndo() {
        if (!canUndo()) {
            return false;
        }
        remainingUndos--;
        return true;
    }
    public void awardEarnedUndos(List<GameModel.Movement> movements) {
        var newlyEarned = movements.stream()
                .filter(GameModel.Movement::merge)
                .flatMap(movement -> milestoneFor(movement.mergedValue()))
                .filter(earnedMilestones::add)
                .count();

        if (newlyEarned > 0) {
            remainingUndos = (int) Math.min(MAX_UNDOS, remainingUndos + newlyEarned);
        }
    }

    private Stream<Integer> milestoneFor(int mergedValue) {
        return EARN_MILESTONES.stream().filter(milestone -> milestone == mergedValue);
    }
}
