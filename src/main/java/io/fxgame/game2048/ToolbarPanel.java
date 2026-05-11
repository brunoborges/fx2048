package io.fxgame.game2048;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

final class ToolbarPanel extends HBox {

    ToolbarPanel(Actions actions) {
        super(
                button("mSave", "Save Session", actions.saveSession()),
                button("mRestore", "Restore Session", actions.restoreSession()),
                button("mPause", "Pause Game", actions.pauseGame()),
                button("mReplay", "Try Again", actions.tryAgain()),
                button("mUndo", "Undo Move", actions.undoMove()),
                button("mSettings", "Settings", actions.settings()),
                button("mInfo", "About the Game", actions.about()),
                button("mQuit", "Quit Game", actions.quit()));

        setAlignment(Pos.CENTER);
        setPadding(new Insets(10.0));
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
        button.setPrefSize(40, 40);
        button.setId(id);
        button.setOnAction(_ -> action.run());
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }
}
