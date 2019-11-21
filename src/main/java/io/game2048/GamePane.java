package io.game2048;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * @author Bruno Borges
 */
public class GamePane extends StackPane {

    private GameManager gameManager;
    private Bounds gameBounds;
    private final static int MARGIN = 36;

    static {
        // Downloaded from https://01.org/clear-sans/blogs
        // The font may be used and redistributed under the terms of the Apache License, Version
        // 2.0.
        Font.loadFont(Game2048.class.getResource("ClearSans-Bold.ttf").toExternalForm(), 10.0);
    }

    public GamePane() {
        gameManager = new GameManager();
        gameManager.setToolBar(createToolBar());
        gameBounds = gameManager.getLayoutBounds();

        getChildren().add(gameManager);

        getStyleClass().addAll("game-root");
        ChangeListener<Number> resize = (ov, v, v1) -> {
            double scale = Math.min((getWidth() - MARGIN) / gameBounds.getWidth(),
                    (getHeight() - MARGIN) / gameBounds.getHeight());
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

    private BooleanProperty cmdCtrlKeyPressed = new SimpleBooleanProperty(false);

    private void addKeyHandlers() {
        setOnKeyPressed(ke -> {
            var keyCode = ke.getCode();

            if (keyCode.equals(KeyCode.CONTROL) || keyCode.equals(KeyCode.COMMAND)) {
                cmdCtrlKeyPressed.set(true);
                return;
            }

            if (keyCode.equals(KeyCode.S)) {
                gameManager.saveSession();
                return;
            }

            if (keyCode.equals(KeyCode.R)) {
                gameManager.restoreSession();
                return;
            }

            if (keyCode.equals(KeyCode.P)) {
                gameManager.pauseGame();
                return;
            }

            if (cmdCtrlKeyPressed.get() == false && keyCode.equals(KeyCode.Q)) {
                gameManager.quitGame();
                return;
            }

            if (ke.getCode().equals(KeyCode.F)) {
                var stage = ((Stage) getScene().getWindow());
                stage.setFullScreen(!stage.isFullScreen());
                return;
            }

            if (keyCode.isArrowKey()) {
                var direction = Direction.valueFor(keyCode);
                move(direction);
                return;
            }
        });

        setOnKeyReleased(ke -> {
            var keyCode = ke.getCode();

            if (keyCode.equals(KeyCode.CONTROL) || keyCode.equals(KeyCode.COMMAND)) {
                cmdCtrlKeyPressed.set(false);
                return;
            }
        });

    }

    private void addSwipeHandlers() {
        setOnSwipeUp(e -> move(Direction.UP));
        setOnSwipeRight(e -> move(Direction.RIGHT));
        setOnSwipeLeft(e -> move(Direction.LEFT));
        setOnSwipeDown(e -> move(Direction.DOWN));
    }

    private void move(Direction direction) {
        gameManager.move(direction);
    }

    private HBox createToolBar() {
        var btItem1 = createButtonItem("mSave", "Save Session", t -> gameManager.saveSession());
        var btItem2 = createButtonItem("mRestore", "Restore Session", t -> gameManager.restoreSession());
        var btItem3 = createButtonItem("mPause", "Pause Game", t -> gameManager.pauseGame());
        var btItem4 = createButtonItem("mReplay", "Try Again", t -> gameManager.tryAgain());
        var btItem5 = createButtonItem("mInfo", "About the Game", t -> gameManager.aboutGame());
        var btItem6 = createButtonItem("mQuit", "Quit Game", t -> gameManager.quitGame());

        var toolbar = new HBox(btItem1, btItem2, btItem3, btItem4, btItem5, btItem6);
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setPadding(new Insets(10.0));
        return toolbar;
    }

    private Button createButtonItem(String symbol, String text, EventHandler<ActionEvent> t) {
        var g = new Button();
        g.setPrefSize(40, 40);
        g.setId(symbol);
        g.setOnAction(t);
        g.setTooltip(new Tooltip(text));
        return g;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static int getMargin() {
        return MARGIN;
    }

}
