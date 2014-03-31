package game2048;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * @author bruno.borges@oracle.com
 */
public class Game2048 extends Application {

    private GameManager gameManager;

    @Override
    public void start(Stage primaryStage) {
        gameManager = new GameManager();

        AnchorPane root=new AnchorPane();
        
        root.getChildren().add(gameManager);
        root.setTranslateX(4.5d);
        root.setTranslateY(4.5d);
        Scene scene = new Scene(root, 512, 512);
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
            if (keyCode.isArrowKey() == false) {
                return;
            }
            Direction direction = Direction.valueOf(keyCode.name());
            gameManager.move(direction);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
