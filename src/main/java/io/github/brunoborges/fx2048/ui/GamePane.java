package io.github.brunoborges.fx2048.ui;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.Objects;

import static io.github.brunoborges.fx2048.game.Direction.*;
import io.github.brunoborges.fx2048.app.Game2048;
import io.github.brunoborges.fx2048.game.Direction;
import io.github.brunoborges.fx2048.persistence.UserSettings;

/**
 * @author Bruno Borges
 */
public class GamePane extends StackPane {

    private static final double STATUS_BAR_HEIGHT = 20;

    private GameManager gameManager;
    private Bounds gameBounds;
    private final SystemStatusBar statusBar = new SystemStatusBar();

    static {
        // Downloaded from https://01.org/clear-sans/blogs
        // The font may be used and redistributed under the terms of the Apache License 2.0
        Font.loadFont(Objects.requireNonNull(Game2048.class.getResource("/io/github/brunoborges/fx2048/ClearSans-Bold.ttf")).toExternalForm(), 10.0);
    }

    public GamePane() {
        createGameManager(UserSettings.LOCAL.getGridSize());
        gameManager.tryAutoRestoreSession();
        createStatusBar();

        getStyleClass().addAll("game-root");
        ChangeListener<Number> resize = (_, _, _) -> resizeGameManager();
        widthProperty().addListener(resize);
        heightProperty().addListener(resize);

        addKeyHandlers();
        addSwipeHandlers();
        setFocusTraversable(true);
        setOnMouseClicked(e -> requestFocus());
    }

    private void createGameManager(int gridSize) {
        if (gameManager != null) {
            gameManager.saveRecord();
            gameManager.dispose();
            getChildren().remove(gameManager);
        }

        gameManager = new GameManager(gridSize, this::changeGridSize);
        gameBounds = gameManager.getLayoutBounds();
        getChildren().add(gameManager);
        statusBar.toFront();
        resizeGameManager();
    }

    private void createStatusBar() {
        statusBar.setMinHeight(STATUS_BAR_HEIGHT);
        statusBar.setPrefHeight(STATUS_BAR_HEIGHT);
        statusBar.setMaxHeight(STATUS_BAR_HEIGHT);
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.prefWidthProperty().bind(widthProperty());
        StackPane.setAlignment(statusBar, Pos.BOTTOM_CENTER);
        getChildren().add(statusBar);
    }

    private void changeGridSize(int gridSize) {
        createGameManager(gridSize);
        requestFocus();
    }

    private void resizeGameManager() {
        if (gameBounds == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        var availableWidth = Math.max(1, getWidth() - UserSettings.MARGIN);
        var availableHeight = Math.max(1, getHeight() - UserSettings.MARGIN - STATUS_BAR_HEIGHT);
        double scale = calculateGameScale(
                availableWidth,
                availableHeight,
                gameBounds.getWidth(),
                gameBounds.getHeight());
        gameManager.setScale(scale);
        gameManager.setLayoutX((getWidth() - gameBounds.getWidth()) / 2d);
        gameManager.setLayoutY((getHeight() - STATUS_BAR_HEIGHT - gameBounds.getHeight()) / 2d);
    }

    static double calculateGameScale(
            double availableWidth,
            double availableHeight,
            double gameWidth,
            double gameHeight) {
        return Math.min(availableWidth / gameWidth, availableHeight / gameHeight);
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
                case U -> gameManager.undoMove();
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
                case CONTROL, COMMAND -> { cmdCtrlKeyPressed.set(false); }
                default -> {}
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

    public void dispose() {
        gameManager.saveRecord();
        gameManager.dispose();
        statusBar.stop();
    }

}
