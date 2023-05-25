package io.fxgame.game2048;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.Objects;

import static io.fxgame.game2048.Direction.*;

/**
 * @author Bruno Borges
 */
public class GamePane extends StackPane {

    private final GameManager gameManager;
    private final Bounds gameBounds;

    static {
        // Downloaded from https://01.org/clear-sans/blogs
        // The font may be used and redistributed under the terms of the Apache License 2.0
        Font.loadFont(Objects.requireNonNull(Game2048.class.getResource("ClearSans-Bold.ttf")).toExternalForm(), 10.0);
    }

    public GamePane() {
        gameManager = new GameManager(UserSettings.LOCAL.getGridSize());
        gameBounds = gameManager.getLayoutBounds();

        getChildren().add(gameManager);

        getStyleClass().addAll("game-root");
        ChangeListener<Number> resize = (ov, v, v1) -> {
            double scale = Math.min((getWidth() - UserSettings.MARGIN) / gameBounds.getWidth(),
                    (getHeight() - UserSettings.MARGIN) / gameBounds.getHeight());
            gameManager.setScale(scale);
            gameManager.setLayoutX((getWidth() - gameBounds.getWidth()) / 2d);
            gameManager.setLayoutY((getHeight() - gameBounds.getHeight()) / 2d);
        };
        widthProperty().addListener(resize);
        heightProperty().addListener(resize);

        addKeyHandlers();
        addSwipeHandlers();
        setFocusTraversable(true);
        setOnMouseClicked(e -> requestFocus());
    }

    private final BooleanProperty cmdCtrlKeyPressed = new SimpleBooleanProperty(false);

    private void addKeyHandlers() {
        setOnKeyPressed(ke -> {
            var keyCode = ke.getCode();
            switch (keyCode) {
                case CONTROL, COMMAND -> cmdCtrlKeyPressed.set(true);
                case S -> gameManager.saveSession();
                case R -> gameManager.restoreSession();
                case P -> gameManager.pauseGame();
                case Q -> {
                    if (!cmdCtrlKeyPressed.get()) gameManager.quitGame();
                }
                case F -> {
                    var stage = ((Stage) getScene().getWindow());
                    stage.setFullScreen(!stage.isFullScreen());
                }
                default -> {
                    if (keyCode.isArrowKey()) move(Direction.valueFor(keyCode));
                }
            }
        });

        setOnKeyReleased(ke -> {
            var keyCode = ke.getCode();
            switch (keyCode) {
                case CONTROL, COMMAND -> cmdCtrlKeyPressed.set(false);
            }
        });
    }

    private void addSwipeHandlers() {
        setOnSwipeUp(e -> move(UP));
        setOnSwipeRight(e -> move(RIGHT));
        setOnSwipeLeft(e -> move(LEFT));
        setOnSwipeDown(e -> move(DOWN));
    }

    private void move(Direction direction) {
        gameManager.move(direction);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

}
