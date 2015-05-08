package game2048;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author bruno.borges@oracle.com
 */
public class Game2048 extends Application {

    public static final String VERSION = "1.0.4";
    
    private GameManager gameManager;
    private Bounds gameBounds;
    private final static int MARGIN = 36;

    @Override
    public void init() {
        // Downloaded from https://01.org/clear-sans/blogs
        // The font may be used and redistributed under the terms of the Apache License, Version 2.0.
        Font.loadFont(Game2048.class.getResource("ClearSans-Bold.ttf").toExternalForm(), 10.0);
    }

    @Override
    public void start(Stage primaryStage) {
        gameManager = new GameManager();
        gameManager.setToolBar(createToolBar());
        gameBounds = gameManager.getLayoutBounds();
        gameManager.setHostServices(getHostServices());

        StackPane root = new StackPane(gameManager);
        root.getStyleClass().addAll("game-root");
        ChangeListener<Number> resize = (ov, v, v1) -> {
            double scale = Math.min((root.getWidth() - MARGIN) / gameBounds.getWidth(), (root.getHeight() - MARGIN) / gameBounds.getHeight());
            gameManager.setScale(scale);
            gameManager.setLayoutX((root.getWidth() - gameBounds.getWidth()) / 2d);
            gameManager.setLayoutY((root.getHeight() - gameBounds.getHeight()) / 2d);
        };
        root.widthProperty().addListener(resize);
        root.heightProperty().addListener(resize);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("game2048/game.css");
        addKeyHandler(scene);
        addSwipeHandlers(scene);

        if (isARMDevice()) {
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
        }

        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            scene.setCursor(Cursor.NONE);
        }

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double factor = Math.min(visualBounds.getWidth() / (gameBounds.getWidth() + MARGIN),
                visualBounds.getHeight() / (gameBounds.getHeight() + MARGIN));
        primaryStage.setTitle("2048FX");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(gameBounds.getWidth() / 2d);
        primaryStage.setMinHeight(gameBounds.getHeight() / 2d);
        primaryStage.setWidth((gameBounds.getWidth() + MARGIN) * factor);
        primaryStage.setHeight((gameBounds.getHeight() + MARGIN) * factor);
        
        primaryStage.setOnCloseRequest(t->{
            t.consume();
            gameManager.quitGame();
        });
        primaryStage.show();
    }

    private boolean isARMDevice() {
        return System.getProperty("os.arch").toUpperCase().contains("ARM");
    }

    private void addKeyHandler(Scene scene) {
        scene.setOnKeyPressed(ke -> {
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

    private void addSwipeHandlers(Scene scene) {
        scene.setOnSwipeUp(e -> move(Direction.UP));
        scene.setOnSwipeRight(e -> move(Direction.RIGHT));
        scene.setOnSwipeLeft(e -> move(Direction.LEFT));
        scene.setOnSwipeDown(e -> move(Direction.DOWN));
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
    
    @Override
    public void stop() {
        gameManager.saveRecord();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
