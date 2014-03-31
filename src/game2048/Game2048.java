package game2048;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * @author bruno.borges@oracle.com
 */
public class Game2048 extends Application {

    private GameManager gameManager;

    @Override
    public void start(Stage primaryStage) {
        gameManager = new GameManager();

        Scene scene = new Scene(gameManager, 512, 512);
        scene.getStylesheets().add("game2048/game.css");
        addKeyHandler(scene);

        primaryStage.setTitle("Fx2048");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void addKeyHandler(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, ke -> {
            KeyCode keyCode = ke.getCode();
            try {
                Direction direction = Direction.valueOf(keyCode.name());
                gameManager.move(direction);
            } catch (RuntimeException e) {/* keynotfound */ }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
