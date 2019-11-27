package game2048;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * @author Jose Pereda
 * @author Bruno Borges
 */
public class Board extends Pane {

    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBestProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final BooleanProperty gameWonProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameAboutProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePauseProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameTryAgainProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameSaveProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameRestoreProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameQuitProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOnProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final BooleanProperty clearGame = new SimpleBooleanProperty(false);
    private final BooleanProperty restoreGame = new SimpleBooleanProperty(false);
    private final BooleanProperty saveGame = new SimpleBooleanProperty(false);

    private LocalTime time;
    private Timeline timer;
    private final StringProperty clock = new SimpleStringProperty("00:00:00");

    // User Interface controls
    private final VBox vGame = new VBox(0);
    private final Group gridGroup = new Group();

    private final HBox hTop = new HBox(0);
    private final VBox vScore = new VBox(-5);
    private final Label lblScore = new Label("0");
    private final Label lblBest = new Label("0");
    private final Label lblPoints = new Label();

    private final BoardOverlay overlay;

    // Overlay Buttons
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bSave = new Button("Save");
    private final Button bRestore = new Button("Restore");
    private final Button bQuit = new Button("Quit");

    private final ChangeListener<Boolean> wonListener;

    private final HBox hToolbar = new HBox();

    private final Label lblTime = new Label();

    private final int gridWidth;
    private final GridOperator gridOperator;
    private final SessionManager sessionManager;

    public Board(GridOperator grid) {
        this.gridOperator = grid;
        gridWidth = Constants.CELL_SIZE * grid.getGridSize() + Constants.BORDER_WIDTH * 2;
        sessionManager = new SessionManager(gridOperator);

        createScore();
        createGrid();
        createToolBar();
        initGameProperties();
    }

    public int getGridWidth() {
        return gridWidth;
    }

    private void createScore() {
        var lblTitle = new Label("2048");
        lblTitle.getStyleClass().addAll("game-label", "game-title");

        var lblSubtitle = new Label("FX");
        lblSubtitle.getStyleClass().addAll("game-label", "game-subtitle");

        var hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);

        var vScores = new VBox();
        var hScores = new HBox(5);

        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("game-vbox");

        var lblTit = new Label("SCORE");
        lblTit.getStyleClass().addAll("game-label", "game-titScore");

        lblScore.getStyleClass().addAll("game-label", "game-score");
        lblScore.textProperty().bind(gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        var vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("game-vbox");

        var lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().addAll("game-label", "game-titScore");
        lblBest.getStyleClass().addAll("game-label", "game-score");
        lblBest.textProperty().bind(gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore, vRecord);

        var vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores, vFill);

        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill, vScores);
        hTop.setMinSize(gridWidth, Constants.TOP_HEIGHT);
        hTop.setPrefSize(gridWidth, Constants.TOP_HEIGHT);
        hTop.setMaxSize(gridWidth, Constants.TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        var hTime = new HBox();
        hTime.setMinSize(gridWidth, Constants.GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().addAll("game-label", "game-time");
        lblTime.textProperty().bind(clock);
        timer = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(Constants.TIMER_FORMATTER));
        }), new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        hTime.getChildren().add(lblTime);

        vGame.getChildren().add(hTime);
        getChildren().add(vGame);

        lblPoints.getStyleClass().addAll("game-label", "game-points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = Constants.CELL_SIZE / 6d;
        var cell = new Rectangle(i * Constants.CELL_SIZE, j * Constants.CELL_SIZE, Constants.CELL_SIZE,
                Constants.CELL_SIZE);
        // provide default style in case css are not loaded
        cell.setFill(Color.WHITE);
        cell.setStroke(Color.GREY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        cell.getStyleClass().add("game-grid-cell");
        return cell;
    }

    private void createGrid() {
        gridOperator.traverseGrid((i, j) -> {
            gridGroup.getChildren().add(createCell(i, j));
            return 0;
        });

        gridGroup.getStyleClass().add("game-grid");
        gridGroup.setManaged(false);
        gridGroup.setLayoutX(Constants.BORDER_WIDTH);
        gridGroup.setLayoutY(Constants.BORDER_WIDTH);

        var hBottom = new HBox();
        hBottom.getStyleClass().add("game-backGrid");
        hBottom.setMinSize(gridWidth, gridWidth);
        hBottom.setPrefSize(gridWidth, gridWidth);
        hBottom.setMaxSize(gridWidth, gridWidth);

        // Clip hBottom to keep the dropshadow effects within the hBottom
        var rect = new Rectangle(gridWidth, gridWidth);
        hBottom.setClip(rect);
        hBottom.getChildren().add(gridGroup);

        vGame.getChildren().add(hBottom);
    }

    private void createToolBar() {
        // toolbar
        var hPadding = new HBox();
        hPadding.setMinSize(gridWidth, Constants.TOOLBAR_HEIGHT);
        hPadding.setPrefSize(gridWidth, Constants.TOOLBAR_HEIGHT);
        hPadding.setMaxSize(gridWidth, Constants.TOOLBAR_HEIGHT);

        hToolbar.setAlignment(Pos.CENTER);
        hToolbar.getStyleClass().add("game-backGrid");
        hToolbar.setMinSize(gridWidth, Constants.TOOLBAR_HEIGHT);
        hToolbar.setPrefSize(gridWidth, Constants.TOOLBAR_HEIGHT);
        hToolbar.setMaxSize(gridWidth, Constants.TOOLBAR_HEIGHT);

        vGame.getChildren().add(hPadding);
        vGame.getChildren().add(hToolbar);
    }

    protected void setToolBar(HBox toolbar) {
        toolbar.disableProperty().bind(layerOnProperty);
        toolbar.spacingProperty().bind(Bindings.divide(vGame.widthProperty(), 10));
        hToolbar.getChildren().add(toolbar);
    }

    protected void tryAgain() {
        if (!gameTryAgainProperty.get()) {
            gameTryAgainProperty.set(true);
        }
    }

    private void btnTryAgain() {
        layerOnProperty.set(false);
        doResetGame();
    }

    private void keepGoing() {
        layerOnProperty.set(false);
        gamePauseProperty.set(false);
        gameTryAgainProperty.set(false);
        gameSaveProperty.set(false);
        gameRestoreProperty.set(false);
        gameAboutProperty.set(false);
        gameQuitProperty.set(false);
        timer.play();
    }

    private void exitGame() {
        Platform.exit();
    }

    private void initGameProperties() {
        overlay = new BoardOverlay(timer, layerOnProperty, this);

        bTry.getStyleClass().add("game-button");
        bTry.setOnAction(e -> btnTryAgain());

        bContinue.getStyleClass().add("game-button");
        bContinue.setOnAction(e -> keepGoing());

        bSave.getStyleClass().add("game-button");
        bSave.setOnAction(e -> saveGame.set(true));

        bRestore.getStyleClass().add("game-button");
        bRestore.setOnAction(e -> restoreGame.set(true));

        bQuit.getStyleClass().add("game-button");
        bQuit.setOnAction(e -> exitGame());

        // Game Won
        wonListener = overlay.createChangeListener("You win!", "", bContinue, bTry, "game-overlay-won", "game-lblWon");
        gameWonProperty.addListener(wonListener);

        // Game Over
        gameOverProperty.addListener(
                overlay.createChangeListener("Game over!", "", bTry, null, "game-overlay-over", "game-lblOver"));

        // Game Pause
        gamePauseProperty.addListener(overlay.createChangeListener("Game Paused", "", bContinue, null,
                "game-overlay-pause", "game-lblPause"));

        // Game Try Again
        gameTryAgainProperty.addListener(overlay.createChangeListener("Try Again?", "Current game will be deleted",
                bTry, bContinue, "game-overlay-pause", "game-lblPause"));

        // Game Save
        gameSaveProperty.addListener(overlay.createChangeListener("Save?", "Previous saved data will be overwritten",
                bSave, bContinue, "game-overlay-pause", "game-lblPause"));

        // Game Restore
        gameRestoreProperty.addListener(overlay.createChangeListener("Restore?", "Current game will be deleted",
                bRestore, bContinue, "game-overlay-pause", "game-lblPause"));

        // Game Quit
        gameQuitProperty.addListener(overlay.createChangeListener("Quit Game?", "Non saved data will be lost", bQuit,
                bContinue, "game-overlay-quit", "game-lblQuit"));

        // About Overlay
        gameAboutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                overlay.displayAbout();
            }
        });

        restoreRecord();

        gameScoreProperty.addListener((ov, i, i1) -> {
            if (i1.intValue() > gameBestProperty.get()) {
                gameBestProperty.set(i1.intValue());
            }
        });

        layerOnProperty.addListener((ov, b, b1) -> {
            if (!b1) {
                getChildren().removeAll(overlay, buttonsOverlay);
                // Keep the focus on the game when the layer is removed:
                getParent().requestFocus();
            } else if (b1) {
                // Set focus on the first button
                buttonsOverlay.getChildren().get(0).requestFocus();
            }
        });

    }

    private void doClearGame() {
        saveRecord();
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().removeAll(overlay, buttonsOverlay);

        Arrays.asList(clearGame, resetGame, restoreGame, saveGame, layerOnProperty, gameWonProperty, gameOverProperty,
                gameAboutProperty, gamePauseProperty, gameTryAgainProperty, gameSaveProperty, gameRestoreProperty,
                gameQuitProperty).forEach(a -> ((WritableBooleanValue) a).set(false));

        gameScoreProperty.set(0);

        clearGame.set(true);
    }

    private void doResetGame() {
        doClearGame();
        resetGame.set(true);
    }

    public void animateScore() {
        if (gameMovePoints.get() == 0) {
            return;
        }

        final var timeline = new Timeline();
        lblPoints.setText("+" + gameMovePoints.getValue().toString());
        lblPoints.setOpacity(1);

        double posX = vScore.localToScene(vScore.getWidth() / 2d, 0).getX();
        lblPoints.setTranslateX(0);
        lblPoints.setTranslateX(lblPoints.sceneToLocal(posX, 0).getX() - lblPoints.getWidth() / 2d);
        lblPoints.setLayoutY(20);

        final var kvO = new KeyValue(lblPoints.opacityProperty(), 0);
        final var kvY = new KeyValue(lblPoints.layoutYProperty(), 100);

        var animationDuration = Duration.millis(600);
        final KeyFrame kfO = new KeyFrame(animationDuration, kvO);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kfO);
        timeline.getKeyFrames().add(kfY);

        timeline.play();
    }

    public void addTile(Tile tile) {
        double layoutX = tile.getLocation().getLayoutX(Constants.CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(Constants.CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        gridGroup.getChildren().add(tile);
    }

    public Tile addRandomTile(Location randomLocation) {
        var tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(Constants.CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(Constants.CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);

        gridGroup.getChildren().add(tile);

        return tile;
    }

    public void startGame() {
        restoreRecord();

        time = LocalTime.now();
        timer.playFromStart();
    }

    public void setPoints(int points) {
        gameMovePoints.set(points);
    }

    public int getPoints() {
        return gameMovePoints.get();
    }

    public void addPoints(int points) {
        gameMovePoints.set(gameMovePoints.get() + points);
        gameScoreProperty.set(gameScoreProperty.get() + points);
    }

    public void gameOver() {
        gameOverProperty.set(true);
    }

    public void gameWon() {
        if (!gameWonProperty.get()) {
            gameWonProperty.set(true);
        }
    }

    public void pauseGame() {
        if (!gamePauseProperty.get()) {
            gamePauseProperty.set(true);
        }
    }

    public void aboutGame() {
        if (!gameAboutProperty.get()) {
            gameAboutProperty.set(true);
        }
    }

    public void quitGame() {
        if (gameQuitProperty.get()) {
            exitGame();
        } else {
            gameQuitProperty.set(true);
        }
    }

    protected BooleanProperty isLayerOn() {
        return layerOnProperty;
    }

    protected BooleanProperty resetGameProperty() {
        return resetGame;
    }

    protected BooleanProperty clearGameProperty() {
        return clearGame;
    }

    protected BooleanProperty saveGameProperty() {
        return saveGame;
    }

    protected BooleanProperty restoreGameProperty() {
        return restoreGame;
    }

    public boolean saveSession() {
        if (!gameSaveProperty.get()) {
            gameSaveProperty.set(true);
        }
        return true;
    }

    /*
     * Once we have confirmation
     */
    public void saveSession(Map<Location, Tile> gameGrid) {
        saveGame.set(false);
        sessionManager.saveSession(gameGrid, gameScoreProperty.getValue(),
                LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
        keepGoing();
    }

    public boolean restoreSession() {
        if (!gameRestoreProperty.get()) {
            gameRestoreProperty.set(true);
        }
        return true;
    }

    /*
     * Once we have confirmation
     */
    public boolean restoreSession(Map<Location, Tile> gameGrid) {
        restoreGame.set(false);
        doClearGame();
        timer.stop();
        var sTime = new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime);
        if (score >= 0) {
            gameScoreProperty.set(score);
            // check tiles>=2048
            gameWonProperty.set(false);
            gameGrid.forEach((l, t) -> {
                if (t != null && t.getValue() >= Constants.FINAL_VALUE_TO_WIN) {
                    gameWonProperty.removeListener(wonListener);
                    gameWonProperty.set(true);
                    gameWonProperty.addListener(wonListener);
                }
            });
            if (!sTime.get().isEmpty()) {
                time = LocalTime.now().minusNanos(Long.parseLong(sTime.get()));
            }
            timer.play();
            return true;
        }
        // not session found, restart again
        doResetGame();
        return false;
    }

    public void saveRecord() {
        var recordManager = new RecordManager(gridOperator.getGridSize());
        recordManager.saveRecord(gameScoreProperty.getValue());
    }

    private void restoreRecord() {
        var recordManager = new RecordManager(gridOperator.getGridSize());
        gameBestProperty.set(recordManager.restoreRecord());
    }

    public void removeTiles(Set<Tile> mergedToBeRemoved) {
        gridGroup.getChildren().removeAll(mergedToBeRemoved);
    }

}
