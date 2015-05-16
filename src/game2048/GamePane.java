package game2048;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * @author bruno.borges@oracle.com
 */
public class GamePane extends StackPane {

    private GameManager gameManager;
    private Bounds gameBounds;
    private final static int MARGIN = 36;

    static {
        // Downloaded from https://01.org/clear-sans/blogs
        // The font may be used and redistributed under the terms of the Apache License, Version 2.0.
        Font.loadFont(Game2048.class.getResource("ClearSans-Bold.ttf").toExternalForm(), 10.0);
    }

    public GamePane() {
        gameManager = new GameManager();
        gameManager.setToolBar(createToolBar());
        gameBounds = gameManager.getLayoutBounds();

        getChildren().add(gameManager);

        getStyleClass().addAll("game-root");
        ChangeListener<Number> resize = (ov, v, v1) -> {
            double scale = Math.min((getWidth() - MARGIN) / gameBounds.getWidth(), (getHeight() - MARGIN) / gameBounds.getHeight());
            gameManager.setScale(scale);
            gameManager.setLayoutX((getWidth() - gameBounds.getWidth()) / 2d);
            gameManager.setLayoutY((getHeight() - gameBounds.getHeight()) / 2d);
        };
        widthProperty().addListener(resize);
        heightProperty().addListener(resize);

        addKeyHandler(this);
        addSwipeHandlers(this);
        setFocusTraversable(true);
        this.setOnMouseClicked(e -> requestFocus());
    }

    private void addKeyHandler(Node node) {
        node.setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
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
            if (keyCode.equals(KeyCode.Q) || keyCode.equals(KeyCode.ESCAPE)) {
                gameManager.quitGame();
                return;
            }
            if (keyCode.isArrowKey()) {
                Direction direction = Direction.valueFor(keyCode);
                move(direction);
            }
        });
    }

    private void addSwipeHandlers(Node node) {
        node.setOnSwipeUp(e -> move(Direction.UP));
        node.setOnSwipeRight(e -> move(Direction.RIGHT));
        node.setOnSwipeLeft(e -> move(Direction.LEFT));
        node.setOnSwipeDown(e -> move(Direction.DOWN));
    }
    
    private void move(Direction direction){
        gameManager.move(direction);    
    }

    private HBox createToolBar(){
        HBox toolbar=new HBox();    
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setPadding(new Insets(10.0));
        Button btItem1 = createButtonItem("mSave", "Save Session", t->gameManager.saveSession());
        Button btItem2 = createButtonItem("mRestore", "Restore Session", t->gameManager.restoreSession());
        Button btItem3 = createButtonItem("mPause", "Pause Game", t->gameManager.pauseGame());
        Button btItem4 = createButtonItem("mReplay", "Try Again", t->gameManager.tryAgain());
        Button btItem5 = createButtonItem("mInfo", "About the Game", t->gameManager.aboutGame());
        toolbar.getChildren().setAll(btItem1, btItem2, btItem3, btItem4, btItem5);
        Button btItem6 = createButtonItem("mQuit", "Quit Game", t->gameManager.quitGame());
        toolbar.getChildren().add(btItem6);
        return toolbar;
    }
    
    private Button createButtonItem(String symbol, String text, EventHandler<ActionEvent> t){
        Button g=new Button();
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
