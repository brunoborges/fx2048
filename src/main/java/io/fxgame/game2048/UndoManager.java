package io.fxgame.game2048;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class UndoManager {

    static final int INITIAL_UNDOS = 2;
    static final int MAX_UNDOS = 3;
    private static final List<Integer> EARN_MILESTONES = List.of(512, 1024, 2048);

    private final Set<Integer> earnedMilestones = new HashSet<>();
    private int remainingUndos;

    UndoManager() {
        resetForNewGame();
    }

    int remainingUndos() {
        return remainingUndos;
    }

    boolean canUndo() {
        return remainingUndos > 0;
    }

    void resetForNewGame() {
        earnedMilestones.clear();
        remainingUndos = INITIAL_UNDOS;
    }

    void resetForRestoredBoard(Map<Location, Integer> snapshot) {
        resetForNewGame();
        var reachedMilestones = EARN_MILESTONES.stream()
                .filter(milestone -> snapshot.values().stream().anyMatch(value -> value != null && value >= milestone))
                .toList();
        reachedMilestones.forEach(earnedMilestones::add);
        remainingUndos = Math.min(MAX_UNDOS, remainingUndos + reachedMilestones.size());
    }

    boolean consumeUndo() {
        if (!canUndo()) {
            return false;
        }
        remainingUndos--;
        return true;
    }

    void awardEarnedUndos(List<GameModel.Movement> movements) {
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
