package io.github.brunoborges.fx2048.app;


import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;
import io.github.brunoborges.fx2048.persistence.UserSettings;
import io.github.brunoborges.fx2048.ui.GamePane;

/**
 * @author Bruno Borges
 */
public class Game2048 extends Application {

    public static final String VERSION = AppMetadata.VERSION;
    private static final double MIN_WINDOW_WIDTH = 360;
    private static final double MIN_WINDOW_HEIGHT = 480;
    private static Game2048 applicationInstance;
    private GamePane gamePane;

    @Override
    public void stop() {
        gamePane.dispose();
    }

    @Override
    public void start(Stage primaryStage) {
        gamePane = new GamePane();

        var scene = new Scene(gamePane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/io/github/brunoborges/fx2048/game.css")).toExternalForm());

        setGameBounds(primaryStage, scene);
        setEnhancedDeviceSettings(primaryStage, scene);
        setQuitListener(primaryStage);

        primaryStage.show();
        gamePane.requestFocus();
    }

    private void setQuitListener(Stage primaryStage) {
        primaryStage.setOnCloseRequest(t -> {
            t.consume();
            gamePane.getGameManager().quitGame();
        });
    }

    private void setEnhancedDeviceSettings(Stage primaryStage, Scene scene) {
        var isARM = System.getProperty("os.arch").toUpperCase().contains("ARM");
        if (isARM) {
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
        }

        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            scene.setCursor(Cursor.NONE);
        }
    }

    private void setGameBounds(Stage primaryStage, Scene scene) {
        var margin = UserSettings.MARGIN;
        var gameBounds = gamePane.getGameManager().getLayoutBounds();
        var visualBounds = Screen.getPrimary().getVisualBounds();
        double factor = Math.min(visualBounds.getWidth() / (gameBounds.getWidth() + margin),
                visualBounds.getHeight() / (gameBounds.getHeight() + margin));
        primaryStage.setTitle("2048FX " + VERSION);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setWidth(((gameBounds.getWidth() + margin) * factor) / 1.5d);
        primaryStage.setHeight(((gameBounds.getHeight() + margin) * factor) / 1.5d);
    }

    public interface URLOpener {
        void open(String url);
    }

    public static URLOpener urlOpener() {
        return (url) -> getInstance().getHostServices().showDocument(url);
    }

    private synchronized static Game2048 getInstance() {
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

    public Game2048() {
        applicationInstance = this;
    }

}
