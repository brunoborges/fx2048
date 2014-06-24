package game2048;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author bruno
 */
public class GameManager extends Group {

    private static final int FINAL_VALUE_TO_WIN = 2048;
    public static final int CELL_SIZE = 128;
    private static final int DEFAULT_GRID_SIZE = 4;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    // grid_width=4*cell_size + 2*cell_stroke/2d (14px css)+2*grid_stroke/2d (2 px css)
    private static final int GRID_WIDTH = CELL_SIZE * DEFAULT_GRID_SIZE + BORDER_WIDTH * 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;

    private volatile boolean movingTiles = false;
    private final int gridSize;
    private final List<Integer> traversalX;
    private final List<Integer> traversalY;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final BooleanProperty gameWonProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePauseProperty = new SimpleBooleanProperty(false);
    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBestProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();
    private final ParallelTransition parallelTransition = new ParallelTransition();
    private final BooleanProperty layerOnProperty = new SimpleBooleanProperty(false);

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
    private final HBox hOvrLabel = new HBox();
    private final HBox hOvrButton = new HBox();
    private final Label lblTime=new Label();        
        
    public GameManager() {
        this(DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        this.gridSize = gridSize;
        this.traversalX = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        this.traversalY = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());

        createScore();
        createGrid();
        initGameProperties();
        restoreRecord();
        
        initializeGrid();
        time=LocalTime.now();
        timer.play();
        
        this.setManaged(false);
    }

    public void move(Direction direction) {
        if (layerOnProperty.get()) {
            return;
        }

        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        gameMovePoints.set(0);

        Collections.sort(traversalX, direction.getX() == 1 ? Collections.reverseOrder() : Integer::compareTo);
        Collections.sort(traversalY, direction.getY() == 1 ? Collections.reverseOrder() : Integer::compareTo);
        final int tilesWereMoved = traverseGrid((int x, int y) -> {
            Location thisloc = new Location(x, y);
            Tile tile = gameGrid.get(thisloc);
            if (tile == null) {
                return 0;
            }

            Location farthestLocation = findFarthestLocation(thisloc, direction); // farthest available location
            Location nextLocation = farthestLocation.offset(direction); // calculates to a possible merge
            Tile tileToBeMerged = nextLocation.isValidFor(gridSize) ? gameGrid.get(nextLocation) : null;

            if (tileToBeMerged != null && tileToBeMerged.getValue().equals(tile.getValue()) && !tileToBeMerged.isMerged()) {
                tileToBeMerged.merge(tile);
                tileToBeMerged.toFront();

                gameGrid.put(nextLocation, tileToBeMerged);
                gameGrid.replace(tile.getLocation(), null);

                parallelTransition.getChildren().add(animateExistingTile(tile, tileToBeMerged.getLocation()));
                parallelTransition.getChildren().add(animateMergedTile(tileToBeMerged));
                mergedToBeRemoved.add(tile);

                gameMovePoints.set(gameMovePoints.get() + tileToBeMerged.getValue());
                gameScoreProperty.set(gameScoreProperty.get() + tileToBeMerged.getValue());

                if (tileToBeMerged.getValue() == FINAL_VALUE_TO_WIN) {
                    gameWonProperty.set(true);
                }
                return 1;
            } else if (farthestLocation.equals(tile.getLocation()) == false) {
                parallelTransition.getChildren().add(animateExistingTile(tile, farthestLocation));

                gameGrid.put(farthestLocation, tile);
                gameGrid.replace(tile.getLocation(), null);

                tile.setLocation(farthestLocation);

                return 1;
            }

            return 0;
        });

        if (gameMovePoints.get() > 0) {
            animateScore(gameMovePoints.getValue().toString()).play();
        }

        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                movingTiles = false;
            }

            gridGroup.getChildren().removeAll(mergedToBeRemoved);

            // game is over if there is no more moves
            Location randomAvailableLocation = findRandomAvailableLocation();
            if (randomAvailableLocation == null && !mergeMovementsAvailable()) {
                gameOverProperty.set(true);
            } else if (randomAvailableLocation != null && tilesWereMoved > 0) {
                addAndAnimateRandomTile(randomAvailableLocation);
            }

            mergedToBeRemoved.clear();

            // reset merged after each movement
            gameGrid.values().stream().filter(Objects::nonNull).forEach(Tile::clearMerge);
        });

        synchronized (gameGrid) {
            movingTiles = true;
        }

        parallelTransition.play();
        parallelTransition.getChildren().clear();
    }

    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (location.isValidFor(gridSize) && gameGrid.get(location) == null);

        return farthest;
    }

    private int traverseGrid(IntBinaryOperator func) {
        AtomicInteger at = new AtomicInteger();
        traversalX.forEach(t_x -> {
            traversalY.forEach(t_y -> {
                at.addAndGet(func.applyAsInt(t_x, t_y));
            });
        });

        return at.get();
    }

    private boolean mergeMovementsAvailable() {
        final SimpleBooleanProperty foundMergeableTile = new SimpleBooleanProperty(false);

        Stream.of(Direction.UP, Direction.LEFT).parallel().forEach(direction -> {
            int mergeableFound = traverseGrid((x, y) -> {
                Location thisloc = new Location(x, y);
                Tile tile = gameGrid.get(thisloc);

                if (tile != null) {
                    Location nextLocation = thisloc.offset(direction); // calculates to a possible merge
                    if (nextLocation.isValidFor(gridSize)) {
                        Tile tileToBeMerged = gameGrid.get(nextLocation);
                        if (tile.isMergeable(tileToBeMerged)) {
                            return 1;
                        }
                    }
                }

                return 0;
            });

            if (mergeableFound > 0) {
                foundMergeableTile.set(true);
            }
        });

        return foundMergeableTile.getValue();
    }

    private void createScore() {
        gameScoreProperty.addListener((ov,i,i1)->{
            if(i1.intValue()>gameBestProperty.get()){
                gameBestProperty.set(i1.intValue());
            }
        });
        Label lblTitle = new Label("2048");
        lblTitle.getStyleClass().add("title");
        Label lblSubtitle = new Label("FX");
        lblSubtitle.getStyleClass().add("subtitle");
        HBox hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);
        
        VBox vScores = new VBox();
        HBox hScores=new HBox(5);
        
        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("vbox");
        Label lblTit = new Label("SCORE");
        lblTit.getStyleClass().add("titScore");
        lblScore.getStyleClass().add("score");
        lblScore.textProperty().bind(gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        VBox vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("vbox");
        Label lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().add("titScore");
        lblBest.getStyleClass().add("score");
        lblBest.textProperty().bind(gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore,vRecord);
        VBox vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores,vFill);
                
        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill,vScores);
        hTop.setMinSize(GRID_WIDTH, TOP_HEIGHT);
        hTop.setPrefSize(GRID_WIDTH, TOP_HEIGHT);
        hTop.setMaxSize(GRID_WIDTH, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        HBox hTime=new HBox();
        hTime.setMinSize(GRID_WIDTH, GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().add("time");
        lblTime.textProperty().bind(clock);
        timer=new Timeline(new KeyFrame(Duration.ZERO, e->{
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt));
        }),new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        hTime.getChildren().add(lblTime);
        
        vGame.getChildren().add(hTime);
        getChildren().add(vGame);
        
        lblPoints.getStyleClass().add("points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }

    private void createGrid() {
        final double arcSize = CELL_SIZE / 6d;

        IntStream.range(0, gridSize)
                .mapToObj(i -> IntStream.range(0, gridSize).mapToObj(j -> {
                    Location loc = new Location(i, j);
                    locations.add(loc);

                    Rectangle rect2 = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                    rect2.setArcHeight(arcSize);
                    rect2.setArcWidth(arcSize);
                    rect2.getStyleClass().add("grid-cell");
                    return rect2;
                }))
                .flatMap(s -> s)
                .forEach(gridGroup.getChildren()::add);

        gridGroup.getStyleClass().add("grid");
        gridGroup.setManaged(false);
        gridGroup.setLayoutX(BORDER_WIDTH);
        gridGroup.setLayoutY(BORDER_WIDTH);

        HBox hBottom = new HBox();
        hBottom.getStyleClass().add("backGrid");
        hBottom.setMinSize(GRID_WIDTH, GRID_WIDTH);
        hBottom.setPrefSize(GRID_WIDTH, GRID_WIDTH);
        hBottom.setMaxSize(GRID_WIDTH, GRID_WIDTH);

        hBottom.getChildren().add(gridGroup);

        vGame.getChildren().add(hBottom);
    }

    private void initGameProperties() {
        gameOverProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("over");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblOver = new Label("Game over!");
                lblOver.getStyleClass().add("lblOver");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblOver);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                Button bTry = new Button("Try again");
                bTry.getStyleClass().setAll("try");

                bTry.setOnTouchPressed(e -> resetGame());
                bTry.setOnAction(e -> resetGame());

                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });

        gameWonProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                Timeline timerPause=new Timeline(new KeyFrame(Duration.seconds(1), e->
                    time=time.plusNanos(1_000_000_000)));
                timerPause.setCycleCount(Animation.INDEFINITE);
                timerPause.play();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("won");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblWin = new Label("You win!");
                lblWin.getStyleClass().add("lblWon");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblWin);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                hOvrButton.setSpacing(10);
                Button bContinue = new Button("Keep going");
                bContinue.getStyleClass().add("try");
                bContinue.setOnTouchPressed(e -> {
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                bContinue.setOnAction(e -> {
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                Button bTry = new Button("Try again");
                bTry.getStyleClass().add("try");
                bTry.setOnTouchPressed(e -> {
                    timerPause.stop();
                    resetGame();
                });
                bTry.setOnAction(e -> {
                    timerPause.stop();
                    resetGame();
                });
                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bContinue, bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });
        
        gamePauseProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                Timeline timerPause=new Timeline(new KeyFrame(Duration.seconds(1), e->
                    time=time.plusNanos(1_000_000_000)));
                timerPause.setCycleCount(Animation.INDEFINITE);
                timerPause.play();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("pause");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblWin = new Label("Game Paused");
                lblWin.getStyleClass().add("lblPause");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblWin);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                hOvrButton.setSpacing(10);
                Button bContinue = new Button("Keep going");
                bContinue.getStyleClass().add("try");
                bContinue.setOnAction(e -> {
                    gamePauseProperty.set(false);
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                Button bTry = new Button("Try again");
                bTry.getStyleClass().add("try");
                bTry.setOnTouchPressed(e -> {
                    timerPause.stop();
                    resetGame();
                });
                bTry.setOnAction(e -> {
                    timerPause.stop();
                    resetGame();
                });
                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bContinue, bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });
    }

    private void clearGame() {
        List<Node> collect = gridGroup.getChildren().filtered(c -> c instanceof Tile).stream().collect(Collectors.toList());
        gridGroup.getChildren().removeAll(collect);
        gameGrid.clear();
        getChildren().removeAll(hOvrLabel, hOvrButton);

        layerOnProperty.set(false);
        gameScoreProperty.set(0);
        gameWonProperty.set(false);
        gameOverProperty.set(false);
        gamePauseProperty.set(false);
            
        initializeLocationsInGameGrid();
    }

    private void resetGame() {
        saveRecord();
        clearGame();
        initializeGrid();
        restoreRecord();
        time=LocalTime.now();
        timer.playFromStart();
    }

    /**
     * Clears the grid and redraws all tiles in the <code>gameGrid</code> object
     */
    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(t -> {
            double layoutX = t.getLocation().getLayoutX(CELL_SIZE) - (t.getMinWidth() / 2);
            double layoutY = t.getLocation().getLayoutY(CELL_SIZE) - (t.getMinHeight() / 2);

            t.setLayoutX(layoutX);
            t.setLayoutY(layoutY);
            gridGroup.getChildren().add(t);
        });
    }

    private Timeline animateScore(String v1) {
        final Timeline timeline = new Timeline();
        lblPoints.setText("+" + v1);
        lblPoints.setOpacity(1);
        double posX=vScore.localToScene(vScore.getWidth()/2d,0).getX()-lblPoints.getWidth()/2d-this.getLayoutX();
        lblPoints.setLayoutX(posX);
        lblPoints.setLayoutY(20);
        final KeyValue kvO = new KeyValue(lblPoints.opacityProperty(), 0);
        final KeyValue kvY = new KeyValue(lblPoints.layoutYProperty(), 100);

        Duration animationDuration = Duration.millis(600);
        final KeyFrame kfO = new KeyFrame(animationDuration, kvO);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kfO);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    interface AddTile {

        void add(int value, int x, int y);
    }

    /**
     * Initializes all cells in gameGrid map to null
     */
    private void initializeLocationsInGameGrid() {
        traverseGrid((x, y) -> {
            Location thisloc = new Location(x, y);
            gameGrid.put(thisloc, null);
            return 0;
        });
    }

    private void initializeGrid() {
        initializeLocationsInGameGrid();

        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocs = new ArrayList<>(locations);
        Collections.shuffle(randomLocs);
        Iterator<Location> locs = randomLocs.stream().limit(2).iterator();
        tile0.setLocation(locs.next());

        Tile tile1 = null;
        if (new Random().nextFloat() <= 0.8) { // gives 80% chance to add a second tile
            tile1 = Tile.newRandomTile();
            if (tile1.getValue() == 4 && tile0.getValue() == 4) {
                tile1 = Tile.newTile(2);
            }
            tile1.setLocation(locs.next());
        }

        Arrays.asList(tile0, tile1).forEach(t -> {
            if (t == null) {
                return;
            }
            gameGrid.put(t.getLocation(), t);
        });

        redrawTilesInGameGrid();
    }

    /**
     * Finds a random location or returns null if none exist
     *
     * @return a random location or <code>null</code> if there are no more
     * locations available
     */
    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null).collect(Collectors.toList());

        if (availableLocations.isEmpty()) {
            return null;
        }

        Collections.shuffle(availableLocations);
        Location randomLocation = availableLocations.get(new Random().nextInt(availableLocations.size()));
        return randomLocation;
    }

    private void addAndAnimateRandomTile(Location randomLocation) {
        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);

        gameGrid.put(tile.getLocation(), tile);
        gridGroup.getChildren().add(tile);
        
        animateNewlyAddedTile(tile).play();
    }

    private static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);

    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(tile.layoutXProperty(), 
                newLocation.getLayoutX(CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(), 
                newLocation.getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        KeyFrame kfX = new KeyFrame(ANIMATION_EXISTING_TILE, kvX);
        KeyFrame kfY = new KeyFrame(ANIMATION_EXISTING_TILE, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    // after last movement on full grid, check if there are movements available
    private final EventHandler<ActionEvent> onFinishNewlyAddedTile = e -> {
        if (this.gameGrid.values().parallelStream().noneMatch(Objects::isNull) && !mergeMovementsAvailable()) {
            this.gameOverProperty.set(true);
        }
    };

    private static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);

    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scale=new ScaleTransition(ANIMATION_NEWLY_ADDED_TILE, tile);
        scale.setToX(1.0); scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.setOnFinished(onFinishNewlyAddedTile);
        return scale;
    }
    
    private static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);

    // pop effect: increase tile scale to 120% at the middle, then go back to 100%
    private SequentialTransition animateMergedTile(Tile tile) {
        final ScaleTransition scale0=new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale0.setToX(1.2); scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);
        
        final ScaleTransition scale1=new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale1.setToX(1.0); scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);
        
        return new SequentialTransition(scale0,scale1);
    }

    public void pauseGame(){
        if(!gamePauseProperty.get()){
            gamePauseProperty.set(true);
        }
    }
    
    public void saveSession() {
        SessionManager sessionManager = new SessionManager(DEFAULT_GRID_SIZE);
        sessionManager.saveSession(gameGrid, gameScoreProperty.getValue(), LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
    }

    public void restoreSession() {
        SessionManager sessionManager = new SessionManager(DEFAULT_GRID_SIZE);

        clearGame();
        timer.stop();
        StringProperty sTime=new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime);
        if (score >= 0) {
            gameScoreProperty.set(score);
            if(!sTime.get().isEmpty()){
                time = LocalTime.now().minusNanos(new Long(sTime.get()));
            }
            timer.play();
            redrawTilesInGameGrid();
        } else {
            // not session found, restart again
            resetGame();
        }
    }
    
    public void saveRecord() {
        RecordManager recordManager = new RecordManager(DEFAULT_GRID_SIZE);
        recordManager.saveRecord(gameScoreProperty.getValue());
    }
    
    private void restoreRecord() {
        RecordManager recordManager = new RecordManager(DEFAULT_GRID_SIZE);
        gameBestProperty.set(recordManager.restoreRecord());
    }
    
}
