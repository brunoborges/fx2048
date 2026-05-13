package io.fxgame.game2048;

import java.util.List;
import java.util.Optional;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

final class SettingsPanel extends VBox {

    private final List<Item> items;

    SettingsPanel(List<Item> items) {
        super(10);
        this.items = List.copyOf(items);

        var title = new Label("Settings");
        title.getStyleClass().setAll("game-label", "game-lblPause");

        var settingsList = new GridPane();
        settingsList.setAlignment(Pos.CENTER);
        settingsList.setHgap(15);
        settingsList.setVgap(8);
        settingsList.getStyleClass().add("game-settings-list");
        settingsList.getColumnConstraints().setAll(labelColumn(), controlColumn());
        addSettingRows(settingsList);

        setAlignment(Pos.CENTER);
        getStyleClass().add("game-settings-panel");
        getChildren().setAll(title, settingsList);
    }

    void refresh() {
        items.forEach(Item::refresh);
    }

    boolean apply() {
        var startsNewGame = false;
        for (var item : items) {
            if (item.apply()) {
                startsNewGame = true;
            }
        }
        return startsNewGame;
    }

    private void addSettingRows(GridPane settingsList) {
        var rowIndex = 0;
        for (var item : items) {
            var warning = item.warning();
            if (warning.isPresent()) {
                var warningLabel = new Label(warning.get());
                warningLabel.getStyleClass().setAll("game-label", "game-lblWarning");
                GridPane.setColumnSpan(warningLabel, 2);
                GridPane.setHalignment(warningLabel, HPos.CENTER);
                settingsList.add(warningLabel, 0, rowIndex++);
            }

            var label = new Label(item.label());
            label.getStyleClass().setAll("game-label", "game-settings-label");
            settingsList.add(label, 0, rowIndex);
            settingsList.add(item.control(), 1, rowIndex++);
        }
    }

    private ColumnConstraints labelColumn() {
        var column = new ColumnConstraints();
        column.setHalignment(HPos.RIGHT);
        return column;
    }

    private ColumnConstraints controlColumn() {
        var column = new ColumnConstraints();
        column.setHalignment(HPos.LEFT);
        return column;
    }

    interface Item {
        String label();

        Node control();

        default Optional<String> warning() {
            return Optional.empty();
        }

        void refresh();

        boolean apply();
    }
}
