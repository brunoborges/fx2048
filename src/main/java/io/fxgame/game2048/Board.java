package io.fxgame.game2048;

import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * @author Jose Pereda
 * @author Bruno Borges
 */
public class Board extends Pane {

    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;
    private static final int TOOLBAR_HEIGHT = 80;
    private static final int STATUS_BAR_HEIGHT = 32;

    private final GameState state = new GameState();

    private final GameTimer gameTimer = new GameTimer();

    // User Interface controls
    private final VBox vGame = new VBox(0);
    private final Group gridGroup = new Group();

    private final HBox hTop = new HBox(0);
    private final VBox vScore = new VBox(-5);
    private final Label lblScore = new Label("0");
    private final Label lblBest = new Label("0");
    private final Label lblMoves = new Label("0");
    private final Label lblPoints = new Label();
    private final OverlayPanel overlayPanel;

    // Overlay Buttons
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bContinueNo = new Button("No, keep going");
    private final Button bSave = new Button("Save");
    private final Button bRestore = new Button("Restore");
    private final Button bApplySettings = new Button("Apply");
    private final Button bQuit = new Button("Quit");
    private final ChoiceBox<Integer> gridSizeChoice = new ChoiceBox<>();

    private final HBox hToolbar = new HBox();
    private final SystemStatusBar statusBar = new SystemStatusBar();

    private final Label lblTime = new Label();

    private final int gridDimension;
    private final GridOperator gridOperator;
    private final SessionManager sessionManager;
    private final IntConsumer gridSizeChangeHandler;

    public Board(GridOperator grid) {
        this(grid, _ -> {
        });
    }

    public Board(GridOperator grid, IntConsumer gridSizeChangeHandler) {
        this.gridOperator = grid;
        this.gridSizeChangeHandler = gridSizeChangeHandler;
        gridDimension = CELL_SIZE * grid.getGridSize() + BORDER_WIDTH * 2;
        overlayPanel = new OverlayPanel(gridDimension, TOP_HEIGHT, GAP_HEIGHT);
        sessionManager = new SessionManager(gridOperator);

        createScore();
        createGrid();
        createToolBar();
        createStatusBar();
        initGameProperties();
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
        lblScore.textProperty().bind(state.gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        var vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("game-vbox");

        var lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().addAll("game-label", "game-titScore");
        lblBest.getStyleClass().addAll("game-label", "game-score");
        lblBest.textProperty().bind(state.gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);

        var vMoves = new VBox(-5);
        vMoves.setAlignment(Pos.CENTER);
        vMoves.getStyleClass().add("game-vbox");

        var lblTitMoves = new Label("MOVES");
        lblTitMoves.getStyleClass().addAll("game-label", "game-titScore");
        lblMoves.getStyleClass().addAll("game-label", "game-score");
        lblMoves.textProperty().bind(state.gameMoveCountProperty.asString());
        vMoves.getChildren().addAll(lblTitMoves, lblMoves);

        hScores.getChildren().addAll(vScore, vRecord, vMoves);

        var vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores, vFill);

        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill, vScores);
        hTop.setMinSize(gridDimension, TOP_HEIGHT);
        hTop.setPrefSize(gridDimension, TOP_HEIGHT);
        hTop.setMaxSize(gridDimension, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        var hTime = new HBox();
        hTime.setMinSize(gridDimension, GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().addAll("game-label", "game-time");
        lblTime.textProperty().bind(gameTimer.clockProperty());
        hTime.getChildren().add(lblTime);

        vGame.getChildren().add(hTime);
        getChildren().add(vGame);

        lblPoints.getStyleClass().addAll("game-label", "game-points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = CELL_SIZE / 6d;
        var cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
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
        gridGroup.setLayoutX(BORDER_WIDTH);
        gridGroup.setLayoutY(BORDER_WIDTH);

        var hBottom = new HBox();
        hBottom.getStyleClass().add("game-backGrid");
        hBottom.setMinSize(gridDimension, gridDimension);
        hBottom.setPrefSize(gridDimension, gridDimension);
        hBottom.setMaxSize(gridDimension, gridDimension);

        // Clip hBottom to keep the drop-shadow effects within the hBottom
        var rect = new Rectangle(gridDimension, gridDimension);
        hBottom.setClip(rect);
        hBottom.getChildren().add(gridGroup);

        vGame.getChildren().add(hBottom);
    }

    private void createToolBar() {
        // toolbar
        var hPadding = new HBox();
        hPadding.setMinSize(gridDimension, TOOLBAR_HEIGHT);
        hPadding.setPrefSize(gridDimension, TOOLBAR_HEIGHT);
        hPadding.setMaxSize(gridDimension, TOOLBAR_HEIGHT);

        hToolbar.setAlignment(Pos.CENTER);
        hToolbar.getStyleClass().add("game-backGrid");
        hToolbar.setMinSize(gridDimension, TOOLBAR_HEIGHT);
        hToolbar.setPrefSize(gridDimension, TOOLBAR_HEIGHT);
        hToolbar.setMaxSize(gridDimension, TOOLBAR_HEIGHT);

        vGame.getChildren().add(hPadding);
        vGame.getChildren().add(hToolbar);
    }

    private void createStatusBar() {
        statusBar.setMinSize(gridDimension, STATUS_BAR_HEIGHT);
        statusBar.setPrefSize(gridDimension, STATUS_BAR_HEIGHT);
        statusBar.setMaxSize(gridDimension, STATUS_BAR_HEIGHT);
        vGame.getChildren().add(statusBar);
    }

    protected void setToolBar(HBox toolbar) {
        toolbar.disableProperty().bind(state.layerOnProperty);
        toolbar.spacingProperty().bind(Bindings.divide(vGame.widthProperty(), 16));
        hToolbar.getChildren().add(toolbar);
    }

    protected void showTryAgainOverlay() {
        if (!state.gameTryAgainProperty.get()) {
            state.gameTryAgainProperty.set(true);
        }
    }

    private void tryAgain() {
        state.layerOnProperty.set(false);
        doResetGame();
    }

    private void keepGoing() {
        state.keepGoing();
        gameTimer.resume();
    }

    private void exitGame() {
        Platform.exit();
    }

    private void applySettings() {
        var selectedGridSize = gridSizeChoice.getValue();
        UserSettings.LOCAL.setGridSize(selectedGridSize);

        if (selectedGridSize != gridOperator.getGridSize()) {
            gridSizeChangeHandler.accept(selectedGridSize);
        } else {
            keepGoing();
        }
    }

    private void showMessageOverlay(String message, String warning) {
        showMessageOverlay(message, warning, "game-overlay-pause", "game-lblPause", bContinue, null);
    }

    private final Overlay wonListener = new Overlay("You win!", "", bContinue, bTry, "game-overlay-won", "game-lblWon");

    private class Overlay implements ChangeListener<Boolean> {

        private final Button leftButton, rightButton;
        private final String message, warning;
        private final String style1, style2;

        public Overlay(String message, String warning, Button leftButton, Button rightButton, String style1, String style2) {
            this.message = message;
            this.warning = warning;
            this.leftButton = leftButton;
            this.rightButton = rightButton;
            this.style1 = style1;
            this.style2 = style2;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                return;
            }

            gameTimer.pause();
            overlayPanel.showMessage(style1, message, warning, style2, leftButton, rightButton);

            if (!state.layerOnProperty.get()) {
                var defaultBtn = rightButton == null ? leftButton : rightButton;
                showOverlay(defaultBtn);
            }
        }
    }

    private void showMessageOverlay(String message, String warning, String overlayStyle, String messageStyle,
            Button leftButton, Button rightButton) {
        gameTimer.pause();
        overlayPanel.showMessage(overlayStyle, message, warning, messageStyle, leftButton, rightButton);
        showOverlay(leftButton);
    }

    private void showOverlay(Button defaultButton) {
        getChildren().remove(overlayPanel);
        getChildren().add(overlayPanel);
        overlayPanel.setDefaultButton(defaultButton);
        state.layerOnProperty.set(true);
    }

    private void initGameProperties() {
        bTry.getStyleClass().add("game-button");
        bTry.setOnAction(_ -> tryAgain());

        bContinue.getStyleClass().add("game-button");
        bContinue.setOnAction(_ -> keepGoing());

        bContinueNo.getStyleClass().add("game-button");
        bContinueNo.setOnAction(_ -> keepGoing());

        bSave.getStyleClass().add("game-button");
        bSave.setOnAction(_ -> state.saveGame.set(true));

        bRestore.getStyleClass().add("game-button");
        bRestore.setOnAction(_ -> state.restoreGame.set(true));

        bApplySettings.getStyleClass().add("game-button");
        bApplySettings.setOnAction(_ -> applySettings());

        bQuit.getStyleClass().add("game-button");
        bQuit.setOnAction(_ -> exitGame());

        gridSizeChoice.getItems().setAll(IntStream
                .rangeClosed(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE)
                .boxed()
                .toList());
        gridSizeChoice.setValue(gridOperator.getGridSize());
        gridSizeChoice.getStyleClass().add("game-settings-choice");

        state.gameWonProperty.addListener(wonListener);
        state.gameOverProperty
                .addListener(new Overlay("Game over!", "", bTry, null, "game-overlay-over", "game-lblOver"));
        state.gamePauseProperty.addListener(
                new Overlay("Game Paused", "", bContinue, null, "game-overlay-pause", "game-lblPause"));
        state.gameTryAgainProperty.addListener(new Overlay("Try Again?", "Current game will be deleted", bTry, bContinueNo,
                "game-overlay-pause", "game-lblPause"));
        state.gameSaveProperty.addListener(new Overlay("Save?", "Previous saved data will be overwritten", bSave, bContinueNo,
                "game-overlay-pause", "game-lblPause"));
        state.gameRestoreProperty.addListener(new Overlay("Restore?", "Current game will be deleted", bRestore, bContinueNo,
                "game-overlay-pause", "game-lblPause"));
        state.gameSettingsProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                showSettingsOverlay();
            }
        });
        state.gameAboutProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                showAboutOverlay();
            }
        });
        state.gameQuitProperty.addListener(new Overlay("Quit Game?", "Non saved data will be lost", bQuit, bContinueNo,
                "game-overlay-quit", "game-lblQuit"));

        restoreRecord();

        state.gameScoreProperty.addListener((_, _, i1) -> {
            if (i1.intValue() > state.gameBestProperty.get()) {
                state.gameBestProperty.set(i1.intValue());
            }
        });

        state.layerOnProperty.addListener((_, _, b1) -> {
            if (!b1) {
                getChildren().remove(overlayPanel);
                // Keep the focus on the game when the layer is removed:
                getParent().requestFocus();
            } else {
                // Set focus on the first button
                overlayPanel.focusFirstButton();
            }
        });

    }

    private void doResetGame() {
        doClearGame();
        state.resetGame();
    }

    private void doClearGame() {
        saveRecord();
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().remove(overlayPanel);

        state.clearState();
    }

    private void showAboutOverlay() {
        gameTimer.pause();
        overlayPanel.showContent("game-overlay-quit", bContinue, null, new AboutContent(gridDimension));
        showOverlay(bContinue);
    }

    public void animateScore() {
        if (state.gameMovePoints.get() == 0) {
            return;
        }

        final var timeline = new Timeline();
        lblPoints.setText("+" + state.gameMovePoints.getValue().toString());
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
        positionTile(tile);
        gridGroup.getChildren().add(tile);
    }

    public void clearTiles() {
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
    }

    public Tile addAnimatedTile(Location location, int value) {
        var tile = Tile.newTile(value);
        tile.setLocation(location);
        positionTile(tile);
        tile.setScaleX(0);
        tile.setScaleY(0);

        gridGroup.getChildren().add(tile);

        return tile;
    }

    public void startGame() {
        restoreRecord();
        gameTimer.startNew();
    }

    public void setPoints(int points) {
        state.gameMovePoints.set(points);
    }

    public int getScore() {
        return state.gameScoreProperty.get();
    }

    public void setScore(int score) {
        state.gameScoreProperty.set(score);
    }

    public int getMoveCount() {
        return state.gameMoveCountProperty.get();
    }

    public void setMoveCount(int moveCount) {
        state.gameMoveCountProperty.set(moveCount);
    }

    public void incrementMoveCount() {
        state.gameMoveCountProperty.set(state.gameMoveCountProperty.get() + 1);
    }

    private void positionTile(Tile tile) {
        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
    }

    public void addPoints(int points) {
        state.gameMovePoints.set(state.gameMovePoints.get() + points);
        state.gameScoreProperty.set(state.gameScoreProperty.get() + points);
    }

    public void setGameOver(boolean gameOver) {
        state.gameOverProperty.set(gameOver);
    }

    public void setGameWin(boolean won) {
        if (!state.gameWonProperty.get()) {
            state.gameWonProperty.set(won);
        }
    }

    public void pauseGame() {
        if (!state.gamePauseProperty.get()) {
            state.gamePauseProperty.set(true);
        }
    }

    public void aboutGame() {
        if (!state.gameAboutProperty.get()) {
            state.gameAboutProperty.set(true);
        }
    }

    public void settingsGame() {
        if (!state.gameSettingsProperty.get()) {
            state.gameSettingsProperty.set(true);
        }
    }

    private void showSettingsOverlay() {
        gameTimer.pause();

        var title = new Label("Settings");
        title.getStyleClass().setAll("game-label", "game-lblPause");

        var warning = new Label("Changing grid size starts a new game");
        warning.getStyleClass().setAll("game-label", "game-lblWarning");

        var gridSizeLabel = new Label("Grid size");
        gridSizeLabel.getStyleClass().setAll("game-label", "game-settings-label");
        gridSizeChoice.setValue(gridOperator.getGridSize());

        var gridSizeRow = new HBox(15, gridSizeLabel, gridSizeChoice);
        gridSizeRow.setAlignment(Pos.CENTER);
        gridSizeRow.getStyleClass().add("game-settings-row");

        overlayPanel.showContent("game-overlay-pause", bApplySettings, bContinueNo, title, warning, gridSizeRow);
        showOverlay(bApplySettings);
    }

    public void quitGame() {
        if (state.gameQuitProperty.get()) {
            exitGame();
        } else {
            state.gameQuitProperty.set(true);
        }
    }

    protected BooleanProperty isLayerOn() {
        return state.layerOnProperty;
    }

    protected BooleanProperty resetGameProperty() {
        return state.resetGame;
    }

    protected BooleanProperty clearGameProperty() {
        return state.clearGame;
    }

    protected BooleanProperty saveGameProperty() {
        return state.saveGame;
    }

    protected BooleanProperty restoreGameProperty() {
        return state.restoreGame;
    }

    public void saveSession() {
        if (!state.gameSaveProperty.get()) {
            state.gameSaveProperty.set(true);
        }
    }

    /*
     * Once we have confirmation
     */
    public void saveSession(Map<Location, Integer> gridValues) {
        state.saveGame.set(false);
        if (sessionManager.saveSession(gridValues, state.gameScoreProperty.getValue(),
                gameTimer.elapsedNanos(), state.gameMoveCountProperty.getValue())) {
            keepGoing();
        } else {
            showMessageOverlay("Save failed", "Session could not be written");
        }
    }

    public void restoreSession() {
        if (!state.gameRestoreProperty.get()) {
            state.gameRestoreProperty.set(true);
        }
    }

    /*
     * Once we have confirmation
     */
    public boolean restoreSession(Map<Location, Integer> gridValues) {
        state.restoreGame.set(false);
        var restoredSession = sessionManager.restoreSession();
        if (restoredSession.isEmpty()) {
            showMessageOverlay("Restore failed", "No valid saved game found");
            return false;
        }

        doClearGame();
        gameTimer.pause();
        gridValues.clear();
        gridValues.putAll(restoredSession.get().gridValues());
        state.gameScoreProperty.set(restoredSession.get().score());
        state.gameMoveCountProperty.set(restoredSession.get().moveCount());
        // check tiles>=2048
        state.gameWonProperty.set(false);
        gridValues.forEach((_, value) -> {
            if (value >= GameManager.FINAL_VALUE_TO_WIN) {
                state.gameWonProperty.removeListener(wonListener);
                state.gameWonProperty.set(true);
                state.gameWonProperty.addListener(wonListener);
            }
        });
        gameTimer.restore(restoredSession.get().time());
        return true;
    }

    public void saveRecord() {
        var recordManager = new RecordManager(gridOperator.getGridSize());
        recordManager.saveRecord(state.gameScoreProperty.getValue());
    }

    private void restoreRecord() {
        var recordManager = new RecordManager(gridOperator.getGridSize());
        state.gameBestProperty.set(recordManager.restoreRecord());
    }

    public void removeTiles(Set<Tile> mergedToBeRemoved) {
        gridGroup.getChildren().removeAll(mergedToBeRemoved);
    }

    public void dispose() {
        gameTimer.pause();
        statusBar.stop();
    }

}
