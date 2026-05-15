package io.github.brunoborges.fx2048.settings;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

public final class AutoSaveSetting implements SettingsPanel.Item {

    private final ChoiceBox<AutoSaveMode> autoSaveChoice = new ChoiceBox<>();
    public AutoSaveSetting() {
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
