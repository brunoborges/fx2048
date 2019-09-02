package game2048;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Bruno Borges
 */
public class Game2048 extends Application {

    public static final String VERSION = "1.0.4";

    private GamePane root;

    private static Game2048 applicationInstance;

    public Game2048() {
        applicationInstance = this;
    }

    public synchronized static Game2048 getInstance() {
        if (applicationInstance == null) {
            while (applicationInstance == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return applicationInstance;
    }

    @Override
    public void start(Stage primaryStage) {
        root = new GamePane();

        var scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("game.css").toExternalForm());

        setGameBounds(primaryStage, scene);
        setEnhancedDeviceSettings(primaryStage, scene);
        setQuitListener(primaryStage);

        primaryStage.show();
        root.requestFocus();
    }

    private void setQuitListener(Stage primaryStage) {
        primaryStage.setOnCloseRequest(t -> {
            t.consume();
            root.getGameManager().quitGame();
        });
    }

    private void setEnhancedDeviceSettings(Stage primaryStage, Scene scene) {
        var isARM = System.getProperty("os.arch").toUpperCase().contains("ARM");
        if (isARM) {
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
        } else {
            root.setOnKeyPressed(ke -> {
                if (ke.getCode().equals(KeyCode.F)) {
                    primaryStage.setFullScreen(true);
                }
            });
        }

        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            scene.setCursor(Cursor.NONE);
        }
    }

    private void setGameBounds(Stage primaryStage, Scene scene) {
        var gameBounds = root.getGameManager().getLayoutBounds();
        int MARGIN = GamePane.getMargin();
        var visualBounds = Screen.getPrimary().getVisualBounds();
        double factor = Math.min(visualBounds.getWidth() / (gameBounds.getWidth() + MARGIN),
                visualBounds.getHeight() / (gameBounds.getHeight() + MARGIN));
        primaryStage.setTitle("2048FX");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(gameBounds.getWidth() / 2d);
        primaryStage.setMinHeight(gameBounds.getHeight() / 2d);
        primaryStage.setWidth(((gameBounds.getWidth() + MARGIN) * factor) / 1.5d);
        primaryStage.setHeight(((gameBounds.getHeight() + MARGIN) * factor) / 1.5d);
    }

    @Override
    public void stop() {
        root.getGameManager().saveRecord();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
