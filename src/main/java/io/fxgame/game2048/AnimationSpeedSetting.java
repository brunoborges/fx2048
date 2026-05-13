package io.fxgame.game2048;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

final class AnimationSpeedSetting implements SettingsPanel.Item {

    private final Consumer<AnimationSpeed> animationSpeedChangeHandler;
    private final ChoiceBox<AnimationSpeed> animationSpeedChoice = new ChoiceBox<>();

    AnimationSpeedSetting(Consumer<AnimationSpeed> animationSpeedChangeHandler) {
        this.animationSpeedChangeHandler = animationSpeedChangeHandler;
        animationSpeedChoice.getItems().setAll(AnimationSpeed.values());
        animationSpeedChoice.getStyleClass().add("game-settings-choice");
    }

    @Override
    public String label() {
        return "Animation speed";
    }

    @Override
    public Node control() {
        return animationSpeedChoice;
    }

    @Override
    public void refresh() {
        animationSpeedChoice.setValue(UserSettings.LOCAL.getAnimationSpeed());
    }

    @Override
    public boolean apply() {
        var selectedAnimationSpeed = animationSpeedChoice.getValue();
        UserSettings.LOCAL.setAnimationSpeed(selectedAnimationSpeed);
        animationSpeedChangeHandler.accept(selectedAnimationSpeed);
        return false;
    }
}
