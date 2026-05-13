package io.fxgame.game2048;

import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

final class GridSizeSetting implements SettingsPanel.Item {

    private final GridOperator gridOperator;
    private final IntConsumer gridSizeChangeHandler;
    private final ChoiceBox<Integer> gridSizeChoice = new ChoiceBox<>();

    GridSizeSetting(GridOperator gridOperator, IntConsumer gridSizeChangeHandler) {
        this.gridOperator = gridOperator;
        this.gridSizeChangeHandler = gridSizeChangeHandler;

        gridSizeChoice.getItems().setAll(IntStream
                .rangeClosed(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE)
                .boxed()
                .toList());
        gridSizeChoice.getStyleClass().add("game-settings-choice");
    }

    @Override
    public String label() {
        return "Grid size";
    }

    @Override
    public Node control() {
        return gridSizeChoice;
    }

    @Override
    public Optional<String> warning() {
        return Optional.of("Changing grid size starts a new game");
    }

    @Override
    public void refresh() {
        gridSizeChoice.setValue(gridOperator.getGridSize());
    }

    @Override
    public boolean apply() {
        var selectedGridSize = gridSizeChoice.getValue();
        UserSettings.LOCAL.setGridSize(selectedGridSize);

        if (selectedGridSize != gridOperator.getGridSize()) {
            gridSizeChangeHandler.accept(selectedGridSize);
            return true;
        }
        return false;
    }
}
