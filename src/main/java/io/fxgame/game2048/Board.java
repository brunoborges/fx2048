package io.fxgame.game2048;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;
    private static final int TOOLBAR_HEIGHT = 80;

    private final GameState state = new GameState();

    private LocalTime time;
    private Timeline timer;
    private final StringProperty clock = new SimpleStringProperty("00:00:00");
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    // User Interface controls
    private final VBox vGame = new VBox(0);
    private final Group gridGroup = new Group();

    private final HBox hTop = new HBox(0);
    private final VBox vScore = new VBox(-5);
    private final Label lblScore = new Label("0");
    private final Label lblBest = new Label("0");
    private final Label lblPoints = new Label();

    private final HBox overlay = new HBox();
    private final VBox txtOverlay = new VBox(10);
    private final Label lOvrText = new Label();
    private final Label lOvrSubText = new Label();
    private final HBox buttonsOverlay = new HBox();

    // Overlay Buttons
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bContinueNo = new Button("No, keep going");
    private final Button bSave = new Button("Save");
    private final Button bRestore = new Button("Restore");
    private final Button bQuit = new Button("Quit");

    private final HBox hToolbar = new HBox();

    private final Label lblTime = new Label();

    private final int gridDimension;
    private final GridOperator gridOperator;
    private final SessionManager sessionManager;

    public Board(GridOperator grid) {
        this.gridOperator = grid;
        gridDimension = CELL_SIZE * grid.getGridSize() + BORDER_WIDTH * 2;
        sessionManager = new SessionManager(gridOperator);

        createScore();
        createGrid();
        createToolBar();
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
        hScores.getChildren().addAll(vScore, vRecord);

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
        lblTime.textProperty().bind(clock);
        timer = new Timeline(new KeyFrame(Duration.ZERO, e -> clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt))), new KeyFrame(Duration.seconds(1)));
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

    protected void setToolBar(HBox toolbar) {
        toolbar.disableProperty().bind(state.layerOnProperty);
        toolbar.spacingProperty().bind(Bindings.divide(vGame.widthProperty(), 10));
        hToolbar.getChildren().add(toolbar);
    }

    protected void tryAgain() {
        if (!state.gameTryAgainProperty.get()) {
            state.gameTryAgainProperty.set(true);
        }
    }

    private void btnTryAgain() {
        state.layerOnProperty.set(false);
        doResetGame();
    }

    private void keepGoing() {
        state.keepGoing();
        timer.play();
    }

    private void exitGame() {
        Platform.exit();
    }

    private final Overlay wonListener = new Overlay("You win!", "", bContinue, bTry, "game-overlay-won", "game-lblWon");

    private class Overlay implements ChangeListener<Boolean> {

        private final Button btn1, btn2;
        private final String message, warning;
        private final String style1, style2;

        public Overlay(String message, String warning, Button btn1, Button btn2, String style1, String style2) {
            this.message = message;
            this.warning = warning;
            this.btn1 = btn1; // left
            this.btn2 = btn2; // right
            this.style1 = style1;
            this.style2 = style2;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                return;
            }

            timer.stop();

            overlay.getStyleClass().setAll("game-overlay", style1);
            lOvrText.setText(message);
            lOvrText.getStyleClass().setAll("game-label", style2);
            lOvrSubText.setText(warning);
            lOvrSubText.getStyleClass().setAll("game-label", "game-lblWarning");
            txtOverlay.getChildren().setAll(lOvrText, lOvrSubText);
            buttonsOverlay.getChildren().setAll(btn1);

            if (btn2 != null) {
                buttonsOverlay.getChildren().add(btn2);
            }

            if (!state.layerOnProperty.get()) {
                var defaultBtn = btn2 == null ? btn1 : btn2;
                defaultBtn.requestFocus();
                defaultBtn.setDefaultButton(true);

                Board.this.getChildren().addAll(overlay, buttonsOverlay);
                state.layerOnProperty.set(true);
            }
        }
    }

    private void initGameProperties() {
        overlay.setMinSize(gridDimension, gridDimension);
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);

        overlay.getChildren().setAll(txtOverlay);
        txtOverlay.setAlignment(Pos.CENTER);

        buttonsOverlay.setAlignment(Pos.CENTER);
        buttonsOverlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + gridDimension / 2);
        buttonsOverlay.setMinSize(gridDimension, gridDimension / 2);
        buttonsOverlay.setSpacing(10);

        bTry.getStyleClass().add("game-button");
        bTry.setOnAction(e -> btnTryAgain());

        bContinue.getStyleClass().add("game-button");
        bContinue.setOnAction(e -> keepGoing());

        bContinueNo.getStyleClass().add("game-button");
        bContinueNo.setOnAction(e -> keepGoing());

        bSave.getStyleClass().add("game-button");
        bSave.setOnAction(e -> state.saveGame.set(true));

        bRestore.getStyleClass().add("game-button");
        bRestore.setOnAction(e -> state.restoreGame.set(true));

        bQuit.getStyleClass().add("game-button");
        bQuit.setOnAction(e -> exitGame());

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
        state.gameAboutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                overlay.getStyleClass().setAll("game-overlay", "game-overlay-quit");

                TextFlow flow = new TextFlow();
                flow.setTextAlignment(TextAlignment.CENTER);
                flow.setPadding(new Insets(10, 0, 0, 0));
                flow.setMinSize(gridDimension, gridDimension);
                flow.setPrefSize(gridDimension, gridDimension);
                flow.setMaxSize(gridDimension, gridDimension);
                flow.setPrefSize(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

                var t00 = new Text("2048");
                t00.getStyleClass().setAll("game-label", "game-lblAbout");

                var t01 = new Text("FX");
                t01.getStyleClass().setAll("game-label", "game-lblAbout2");

                var t02 = new Text(" Game\n");
                t02.getStyleClass().setAll("game-label", "game-lblAbout");

                var t1 = new Text("JavaFX game - Desktop version\n\n");
                t1.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var t20 = new Text("Powered by ");
                t20.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var link1 = new Hyperlink();
                link1.setText("OpenJFX");
                link1.setOnAction(e -> Game2048.urlOpener().open("https://openjfx.io/"));
                link1.getStyleClass().setAll("game-label", "game-lblAboutSub2");

                var t21 = new Text(" Project \n\n");
                t21.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var t23 = new Text("© ");
                t23.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var link2 = new Hyperlink();
                link2.setText("@JPeredaDnr");
                link2.setOnAction(e -> Game2048.urlOpener().open("https://twitter.com/JPeredaDnr"));
                link2.getStyleClass().setAll("game-label", "game-lblAboutSub2");

                var t22 = new Text(" & ");
                t22.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var link3 = new Hyperlink();
                link3.setText("@brunoborges");
                link3.setOnAction(e -> Game2048.urlOpener().open("https://twitter.com/brunoborges"));

                var t32 = new Text(" & ");
                t32.getStyleClass().setAll("game-label", "game-lblAboutSub");
                link3.getStyleClass().setAll("game-label", "game-lblAboutSub2");

                var t24 = new Text("\n\n");
                t24.getStyleClass().setAll("game-label", "game-lblAboutSub");

                var t31 = new Text(" Version " + Game2048.VERSION + " - 2015\n\n");
                t31.getStyleClass().setAll("game-label", "game-lblAboutSub");

                flow.getChildren().setAll(t00, t01, t02, t1, t20, link1, t21, t23, link2, t22, link3);
                flow.getChildren().addAll(t24, t31);
                txtOverlay.getChildren().setAll(flow);
                buttonsOverlay.getChildren().setAll(bContinue);
                this.getChildren().removeAll(overlay, buttonsOverlay);
                this.getChildren().addAll(overlay, buttonsOverlay);
                state.layerOnProperty.set(true);
            }
        });
        state.gameQuitProperty.addListener(new Overlay("Quit Game?", "Non saved data will be lost", bQuit, bContinueNo,
                "game-overlay-quit", "game-lblQuit"));

        restoreRecord();

        state.gameScoreProperty.addListener((ov, i, i1) -> {
            if (i1.intValue() > state.gameBestProperty.get()) {
                state.gameBestProperty.set(i1.intValue());
            }
        });

        state.layerOnProperty.addListener((ov, b, b1) -> {
            if (!b1) {
                getChildren().removeAll(overlay, buttonsOverlay);
                // Keep the focus on the game when the layer is removed:
                getParent().requestFocus();
            } else {
                // Set focus on the first button
                buttonsOverlay.getChildren().get(0).requestFocus();
            }
        });

    }

    private void doClearGame() {
        saveRecord();
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().removeAll(overlay, buttonsOverlay);

        state.clearState();
    }

    private void doResetGame() {
        doClearGame();
        state.resetGame();
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
        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        gridGroup.getChildren().add(tile);
    }

    public Tile addRandomTile(Location randomLocation) {
        var tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

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
        state.gameMovePoints.set(points);
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
    public void saveSession(Map<Location, Tile> gameGrid) {
        state.saveGame.set(false);
        sessionManager.saveSession(gameGrid, state.gameScoreProperty.getValue(),
                LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
        keepGoing();
    }

    public void restoreSession() {
        if (!state.gameRestoreProperty.get()) {
            state.gameRestoreProperty.set(true);
        }
    }

    /*
     * Once we have confirmation
     */
    public boolean restoreSession(Map<Location, Tile> gameGrid) {
        state.restoreGame.set(false);
        doClearGame();
        timer.stop();
        var sTime = new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime);
        if (score >= 0) {
            state.gameScoreProperty.set(score);
            // check tiles>=2048
            state.gameWonProperty.set(false);
            gameGrid.forEach((l, t) -> {
                if (t != null && t.getValue() >= GameManager.FINAL_VALUE_TO_WIN) {
                    state.gameWonProperty.removeListener(wonListener);
                    state.gameWonProperty.set(true);
                    state.gameWonProperty.addListener(wonListener);
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
        recordManager.saveRecord(state.gameScoreProperty.getValue());
    }

    private void restoreRecord() {
        var recordManager = new RecordManager(gridOperator.getGridSize());
        state.gameBestProperty.set(recordManager.restoreRecord());
    }

    public void removeTiles(Set<Tile> mergedToBeRemoved) {
        gridGroup.getChildren().removeAll(mergedToBeRemoved);
    }

}
