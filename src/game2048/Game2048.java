package game2048;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author bruno.borges@oracle.com
 */
public class Game2048 extends Application {

    public static final String VERSION = "1.0.4";
    
    private GamePane root;

    @Override
    public void start(Stage primaryStage) {
        root = new GamePane();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("game2048/game.css");

        if (isARMDevice()) {
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
        }

        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            scene.setCursor(Cursor.NONE);
        }

        Bounds gameBounds = root.getGameManager().getLayoutBounds();
        int MARGIN = GamePane.getMargin();
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
            root.getGameManager().quitGame();
        });
        primaryStage.show();
    }

    private boolean isARMDevice() {
        return System.getProperty("os.arch").toUpperCase().contains("ARM");
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
