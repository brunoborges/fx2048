package io.fxgame.game2048;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

final class AutoSaveSetting implements SettingsPanel.Item {

    private final ChoiceBox<AutoSaveMode> autoSaveChoice = new ChoiceBox<>();

    AutoSaveSetting() {
        autoSaveChoice.getItems().setAll(AutoSaveMode.values());
        autoSaveChoice.getStyleClass().add("game-settings-choice");
    }

    @Override
    public String label() {
        return "Auto-save";
    }

    @Override
    public Node control() {
        return autoSaveChoice;
    }

    @Override
    public Optional<String> warning() {
        return Optional.empty();
    }

    @Override
    public void refresh() {
        autoSaveChoice.setValue(UserSettings.LOCAL.getAutoSave());
    }

    @Override
    public boolean apply() {
        UserSettings.LOCAL.setAutoSave(autoSaveChoice.getValue());
        return false;
    }
}
