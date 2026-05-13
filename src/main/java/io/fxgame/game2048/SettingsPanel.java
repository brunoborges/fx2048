package io.fxgame.game2048;

import java.util.List;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class SettingsPanel extends VBox {

    private final List<Item> items;

    SettingsPanel(List<Item> items) {
        super(10);
        this.items = List.copyOf(items);

        var title = new Label("Settings");
        title.getStyleClass().setAll("game-label", "game-lblPause");

        var settingsList = new VBox(8);
        settingsList.setAlignment(Pos.CENTER);
        settingsList.getStyleClass().add("game-settings-list");
        settingsList.getChildren().setAll(this.items.stream()
                .map(this::createSettingNode)
                .toList());

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

    private Node createSettingNode(Item item) {
        var label = new Label(item.label());
        label.getStyleClass().setAll("game-label", "game-settings-label");

        var row = new HBox(15, label, item.control());
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("game-settings-row");

        var warning = item.warning();
        if (warning.isEmpty()) {
            return row;
        }

        var warningLabel = new Label(warning.get());
        warningLabel.getStyleClass().setAll("game-label", "game-lblWarning");

        var setting = new VBox(4, warningLabel, row);
        setting.setAlignment(Pos.CENTER);
        return setting;
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
