package io.fxgame.game2048;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

final class ToolbarPanel extends HBox {

    private static final double BUTTON_SIZE = 40.0;
    private static final double PADDING = 10.0;
    private static final double MAX_SPACING = 32.0;

    ToolbarPanel(Actions actions, ObservableIntegerValue undoCount, ObservableBooleanValue undoAvailable) {
        super();
        var undoControl = undoControl(actions.undoMove(), undoCount, undoAvailable);
        getChildren().addAll(
                button("mSave", "Save Session", actions.saveSession()),
                button("mRestore", "Restore Session", actions.restoreSession()),
                button("mPause", "Pause Game", actions.pauseGame()),
                button("mReplay", "Try Again", actions.tryAgain()),
                undoControl,
                button("mSettings", "Settings", actions.settings()),
                button("mInfo", "About the Game", actions.about()),
                button("mQuit", "Quit Game", actions.quit()));

        setAlignment(Pos.CENTER);
        setFillHeight(false);
        setPadding(new Insets(PADDING));
        setMinHeight(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
    }

    void bindSpacingTo(ObservableNumberValue width) {
        spacingProperty().bind(Bindings.createDoubleBinding(
                () -> calculateSpacing(width.doubleValue(), getChildren().size()),
                width));
    }

    record Actions(
            Runnable saveSession,
            Runnable restoreSession,
            Runnable pauseGame,
            Runnable tryAgain,
            Runnable undoMove,
            Runnable settings,
            Runnable about,
            Runnable quit) {}

    private static Button button(String id, String tooltip, Runnable action) {
        var button = new Button();
        button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setId(id);
        button.setOnAction(_ -> action.run());
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private static StackPane undoControl(Runnable action, ObservableIntegerValue undoCount, ObservableBooleanValue undoAvailable) {
        var undoButton = button("mUndo", "Undo Move", action);
        undoButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> isUndoDisabled(undoCount.get(), undoAvailable.get()),
                undoCount,
                undoAvailable));

        var undoBadge = new Label();
        undoBadge.getStyleClass().addAll("game-label", "game-undo-badge");
        undoBadge.textProperty().bind(Bindings.createStringBinding(
                () -> Integer.toString(undoCount.get()),
                undoCount));
        undoBadge.disableProperty().bind(undoButton.disableProperty());
        undoBadge.setMouseTransparent(true);

        var undoControl = new StackPane(undoButton, undoBadge);
        undoControl.getStyleClass().add("game-undo-control");
        undoControl.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        undoControl.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        undoControl.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        StackPane.setAlignment(undoBadge, Pos.TOP_RIGHT);
        return undoControl;
    }

    static boolean isUndoDisabled(int undoCount, boolean undoAvailable) {
        return undoCount <= 0 || !undoAvailable;
    }

    static double calculateSpacing(double width, int buttonCount) {
        if (buttonCount <= 1) {
            return 0;
        }

        var gaps = buttonCount - 1;
        var availableSpacing = width - PADDING * 2 - BUTTON_SIZE * buttonCount;
        return Math.max(0, Math.min(MAX_SPACING, availableSpacing / gaps));
    }
}
