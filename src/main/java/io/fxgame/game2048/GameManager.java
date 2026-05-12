package io.fxgame.game2048;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * @author Bruno Borges
 * @author Jose Pereda
 */
public class GameManager extends Group {

    public static final int FINAL_VALUE_TO_WIN = GameModel.FINAL_VALUE_TO_WIN;

    private static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);
    private static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);
    private static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);

    private volatile boolean movingTiles = false;
    private final Map<Location, Tile> gameGrid = new HashMap<>();
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    private final Board board;
    private final GameModel model;
    private Animation shakingAnimation;
    private MoveSnapshot undoSnapshot;
    private boolean shakingAnimationPlaying = false;
    private boolean shakingXYState = false;

    private record MoveSnapshot(Map<Location, Integer> tiles, int score, int moveCount) {}

    public GameManager() {
        this(UserSettings.LOCAL.getGridSize());
    }

    public GameManager(int gridSize) {
        this(gridSize, _ -> {
        });
    }

    /**
     * GameManager is a Group containing a Board that holds a grid and the score.
     * It delegates the game rules to GameModel and keeps JavaFX animation concerns
     * here.
     *
     * @param gridSize defines the size of the grid, default 6x6
     */
    public GameManager(int gridSize, IntConsumer gridSizeChangeHandler) {
        var gridOperator = new GridOperator(gridSize);
        model = new GameModel(gridOperator);
        board = new Board(gridOperator, gridSizeChangeHandler);
        board.setToolBar(createToolbarPanel());
        getChildren().add(board);

        board.clearGameProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                undoSnapshot = null;
                initializeGameGrid();
            }
        });
        board.resetGameProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                startGame();
            }
        });
        board.restoreGameProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                doRestoreSession();
            }
        });
        board.saveGameProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                doSaveSession();
            }
        });

        startGame();
    }

    private void initializeGameGrid() {
        model.initialize();
        syncTileMapFromModel();
    }

    /**
     * Starts the game by adding 1 or 2 tiles at random locations.
     */
    private void startGame() {
        model.startGame();
        syncTileMapFromModel();
        redrawTilesInGameGrid();
        board.startGame();
    }

    /**
     * Redraws all tiles in the <code>gameGrid</code> object.
     */
    private void redrawTilesInGameGrid() {
        board.clearTiles();
        gameGrid.values().stream().filter(Objects::nonNull).forEach(board::addTile);
    }

    private void syncTileMapFromModel() {
        gameGrid.clear();
        model.snapshot().forEach((location, value) -> gameGrid.put(location, value == 0 ? null : createTile(location, value)));
    }

    private Tile createTile(Location location, int value) {
        var tile = Tile.newTile(value);
        tile.setLocation(location);
        return tile;
    }

    /**
     * Moves the tiles according to given direction. At any move, takes care of merge
     * tiles, add a new one and perform the required animations. It updates the score
     * and checks if the user won the game or if the game is over.
     *
     * @param direction is the selected direction to move the tiles
     */
    private void moveTiles(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        board.setPoints(0);
        mergedToBeRemoved.clear();
        var previousSnapshot = createMoveSnapshot();
        var moveResult = model.move(direction);
        var parallelTransition = new ParallelTransition();
        applyMovements(moveResult, parallelTransition);

        if (moveResult.tilesMoved()) {
            undoSnapshot = previousSnapshot;
            board.incrementMoveCount();
        }

        if (moveResult.points() > 0) {
            board.addPoints(moveResult.points());
        }
        if (moveResult.won()) {
            board.setGameWin(true);
        }

        board.animateScore();
        if (!parallelTransition.getChildren().isEmpty()) {
            parallelTransition.setOnFinished(_ -> {
                board.removeTiles(mergedToBeRemoved);
                gameGrid.values().stream().filter(Objects::nonNull).forEach(Tile::clearMerge);
                synchronized (gameGrid) {
                    movingTiles = false;
                }

                var addedTile = model.addRandomTile();
                if (addedTile.isPresent()) {
                    addAndAnimateRandomTile(addedTile.get());
                } else if (!model.hasMergeMovements()) {
                    board.setGameOver(true);
                }
            });

            synchronized (gameGrid) {
                movingTiles = true;
            }

            parallelTransition.play();
        }

        if (!moveResult.tilesMoved()) {
            shakeGamePane();
        }
    }

    private void applyMovements(GameModel.MoveResult moveResult, ParallelTransition parallelTransition) {
        moveResult.movements().forEach(movement -> {
            var tile = requireTile(movement.source());
            if (movement.merge()) {
                var targetTile = requireTile(movement.destination());
                targetTile.merge(tile);
                targetTile.toFront();
                gameGrid.put(movement.destination(), targetTile);
                gameGrid.replace(movement.source(), null);

                parallelTransition.getChildren().add(animateExistingTile(tile, targetTile.getLocation()));
                parallelTransition.getChildren().add(animateMergedTile(targetTile));
                mergedToBeRemoved.add(tile);
            } else {
                parallelTransition.getChildren().add(animateExistingTile(tile, movement.destination()));
                gameGrid.put(movement.destination(), tile);
                gameGrid.replace(movement.source(), null);
                tile.setLocation(movement.destination());
            }
        });
    }

    private Tile requireTile(Location location) {
        var tile = gameGrid.get(location);
        if (tile == null) {
            throw new IllegalStateException("Expected tile at " + location);
        }
        return tile;
    }

    private void shakeGamePane() {
        if (shakingAnimation == null) {
            shakingAnimation = createShakeGamePaneAnimation();
        }

        if (!shakingAnimationPlaying) {
            shakingAnimation.play();
            shakingAnimationPlaying = true;
        }
    }

    private MoveSnapshot createMoveSnapshot() {
        return new MoveSnapshot(model.snapshot(), board.getScore(), board.getMoveCount());
    }

    private void restoreMoveSnapshot(MoveSnapshot snapshot) {
        model.restoreSnapshot(snapshot.tiles());
        syncTileMapFromModel();
        board.setPoints(0);
        board.setScore(snapshot.score());
        board.setMoveCount(snapshot.moveCount());
        redrawTilesInGameGrid();
        undoSnapshot = null;
    }

    private void addAndAnimateRandomTile(GameModel.TileState tileState) {
        var tile = board.addAnimatedTile(tileState.location(), tileState.value());
        gameGrid.put(tile.getLocation(), tile);

        animateNewlyAddedTile(tile).play();
    }

    /**
     * Animation that creates a fade in effect when a tile is added to the game by
     * increasing the tile scale from 0 to 100%.
     *
     * @param tile to be animated
     * @return a scale transition
     */
    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final var scaleTransition = new ScaleTransition(ANIMATION_NEWLY_ADDED_TILE, tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setOnFinished(_ -> {
            if (model.isFull() && !model.hasMergeMovements()) {
                board.setGameOver(true);
            }
        });
        return scaleTransition;
    }

    private Animation createShakeGamePaneAnimation() {
        var shakingAnimation = new Timeline(new KeyFrame(Duration.seconds(0.05), _ -> {
            var parent = getParent();

            if (shakingXYState) {
                parent.setLayoutX(parent.getLayoutX() + 5);
                parent.setLayoutY(parent.getLayoutY() + 5);
            } else {
                parent.setLayoutX(parent.getLayoutX() - 5);
                parent.setLayoutY(parent.getLayoutY() - 5);
            }

            shakingXYState = !shakingXYState;
        }));

        shakingAnimation.setCycleCount(6);
        shakingAnimation.setAutoReverse(false);
        shakingAnimation.setOnFinished(_ -> {
            shakingXYState = false;
            shakingAnimationPlaying = false;
        });

        return shakingAnimation;
    }

    /**
     * Animation that moves the tile from its previous location to a new location.
     *
     * @param tile        to be animated
     * @param newLocation new location of the tile
     * @return a timeline
     */
    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        var timeline = new Timeline();
        var kvX = new KeyValue(tile.layoutXProperty(),
                newLocation.getLayoutX(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        var kvY = new KeyValue(tile.layoutYProperty(),
                newLocation.getLayoutY(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        var kfX = new KeyFrame(ANIMATION_EXISTING_TILE, kvX);
        var kfY = new KeyFrame(ANIMATION_EXISTING_TILE, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    /**
     * Animation that creates a pop effect when two tiles merge by increasing the
     * tile scale to 120% at the middle, and then going back to 100%.
     *
     * @param tile to be animated
     * @return a sequential transition
     */
    private SequentialTransition animateMergedTile(Tile tile) {
        final var scale0 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final var scale1 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        return new SequentialTransition(scale0, scale1);
    }

    /**
     * Move the tiles according user input if overlay is not on.
     */
    public void move(Direction direction) {
        if (!board.isLayerOn().get()) {
            moveTiles(direction);
        }
    }

    public void undoMove() {
        synchronized (gameGrid) {
            if (movingTiles || undoSnapshot == null) {
                return;
            }
        }

        if (!board.isLayerOn().get()) {
            restoreMoveSnapshot(undoSnapshot);
        }
    }

    /**
     * Set gameManager scale to adjust overall game size.
     */
    public void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    /**
     * Pauses the game time, covers the grid.
     */
    public void pauseGame() {
        board.pauseGame();
    }

    /**
     * Quit the game with confirmation.
     */
    public void quitGame() {
        board.quitGame();
    }

    /**
     * Ask to save the game from a properties file with confirmation.
     */
    public void saveSession() {
        board.saveSession();
    }

    /**
     * Save the game to a properties file, without confirmation.
     */
    private void doSaveSession() {
        board.saveSession(model.snapshot());
    }

    /**
     * Ask to restore the game from a properties file with confirmation.
     */
    public void restoreSession() {
        board.restoreSession();
    }

    /**
     * Restore the game from a properties file, without confirmation.
     */
    private void doRestoreSession() {
        var restoredValues = new HashMap<Location, Integer>();
        if (board.restoreSession(restoredValues)) {
            model.restoreSnapshot(restoredValues);
            syncTileMapFromModel();
            redrawTilesInGameGrid();
            undoSnapshot = null;
        }
    }

    /**
     * Save actual record to a properties file.
     */
    public void saveRecord() {
        board.saveRecord();
    }

    private ToolbarPanel createToolbarPanel() {
        return new ToolbarPanel(new ToolbarPanel.Actions(
                this::saveSession,
                this::restoreSession,
                board::pauseGame,
                board::showTryAgainOverlay,
                this::undoMove,
                board::settingsGame,
                board::aboutGame,
                this::quitGame));
    }
}
