package io.github.brunoborges.fx2048.ui;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class OverlayPanel extends Group {

    private final HBox overlay = new HBox();
    private final VBox content = new VBox(10);
    private final Label messageText = new Label();
    private final Label warningText = new Label();
    private final HBox buttons = new HBox();

    OverlayPanel(int gridDimension, int topHeight, int gapHeight) {
        overlay.setMinSize(gridDimension, gridDimension);
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(topHeight + gapHeight);
        overlay.getChildren().setAll(content);

        content.setAlignment(Pos.CENTER);

        buttons.setAlignment(Pos.CENTER);
        buttons.setTranslateY(topHeight + gapHeight + (double) gridDimension / 2);
        buttons.setMinSize(gridDimension, (double) gridDimension / 2);
        buttons.setPickOnBounds(false);
        buttons.setSpacing(10);

        getChildren().setAll(overlay, buttons);
    }

    void showMessage(String overlayStyle, String message, String warning, String messageStyle,
            Button leftButton, Button rightButton) {
        prepare(overlayStyle);
        messageText.setText(message);
        messageText.getStyleClass().setAll("game-label", messageStyle);
        warningText.setText(warning);
        warningText.getStyleClass().setAll("game-label", "game-lblWarning");
        content.getChildren().setAll(messageText, warningText);
        setButtons(leftButton, rightButton);
    }

    void showContent(String overlayStyle, Button leftButton, Button rightButton, Node... nodes) {
        prepare(overlayStyle);
        content.getChildren().setAll(nodes);
        setButtons(leftButton, rightButton);
    }

    void focusFirstButton() {
        buttons.getChildren().getFirst().requestFocus();
    }

    void setDefaultButton(Button defaultButton) {
        buttons.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> button.setDefaultButton(button == defaultButton));
    }

    private void prepare(String overlayStyle) {
        overlay.getStyleClass().setAll("game-overlay", overlayStyle);
    }

    private void setButtons(Button leftButton, Button rightButton) {
        buttons.getChildren().setAll(leftButton);
        if (rightButton != null) {
            buttons.getChildren().add(rightButton);
        }
    }
}
