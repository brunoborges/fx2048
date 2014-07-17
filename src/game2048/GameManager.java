package game2048;

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
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.util.Duration;

/**
 *
 * @author bruno
 */
public class GameManager extends Group {

    private static final int FINAL_VALUE_TO_WIN = 2048;
    public static final int CELL_SIZE = 128;
    public static final int DEFAULT_GRID_SIZE = 4;

    private volatile boolean movingTiles = false;
    private final int gridSize;
    private final List<Integer> traversalX;
    private final List<Integer> traversalY;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();
    private final ParallelTransition parallelTransition = new ParallelTransition();
    
    private Board board;
    
    public GameManager() {
        this(DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        this.gridSize = gridSize;
        this.traversalX = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        this.traversalY = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());

        board=new Board(gridSize);
        this.getChildren().add(board);
        
        board.clearGameProperty().addListener((ov,b,b1)->{
            if(b1){
                initializeGameGrid();
            }
        });
        board.resetGameProperty().addListener((ov,b,b1)->{
            if(b1){
                startGame();
            }
        });
        
        initializeGameGrid();
        startGame();
    }
    
    public void move(Direction direction) {
        if (board.isLayerOn()) {
            return;
        }

        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        board.setPoints(0);

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

                board.addPoints(tileToBeMerged.getValue());
                
                if (tileToBeMerged.getValue() == FINAL_VALUE_TO_WIN) {
                    board.setGameWin(true);
                }
                return 1;
            } else if (!farthestLocation.equals(tile.getLocation())) {
                parallelTransition.getChildren().add(animateExistingTile(tile, farthestLocation));

                gameGrid.put(farthestLocation, tile);
                gameGrid.replace(tile.getLocation(), null);

                tile.setLocation(farthestLocation);

                return 1;
            }

            return 0;
        });

        board.animateScore();

        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                movingTiles = false;
            }

            board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);

            // game is over if there is no more moves
            Location randomAvailableLocation = findRandomAvailableLocation();
            if (randomAvailableLocation == null && !mergeMovementsAvailable()) {
                board.setGameOver(true);
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

    /**
     * Redraws all tiles in the <code>gameGrid</code> object
     */
    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(t -> {
            double layoutX = t.getLocation().getLayoutX(CELL_SIZE) - (t.getMinWidth() / 2);
            double layoutY = t.getLocation().getLayoutY(CELL_SIZE) - (t.getMinHeight() / 2);

            t.setLayoutX(layoutX);
            t.setLayoutY(layoutY);
            board.getGridGroup().getChildren().add(t);
        });
    }

    /**
     * Initializes all cells in gameGrid map to null
     */
    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        traverseGrid((x, y) -> {
            Location thisloc = new Location(x, y);
            locations.add(thisloc);
            gameGrid.put(thisloc, null);
            return 0;
        });
    }

    private void startGame() {
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

        Arrays.asList(tile0, tile1).stream().filter(Objects::nonNull)
                .forEach(t -> gameGrid.put(t.getLocation(), t));

        redrawTilesInGameGrid();
        
        board.startGame();
    }

    /**
     * Finds a random location or returns null if none exist
     *
     * @return a random location or <code>null</code> if there are no more
     * locations available
     */
    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null)
                .collect(Collectors.toList());

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
        board.getGridGroup().getChildren().add(tile);
        
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
            board.setGameOver(true);
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

    public void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    public void pauseGame(){
        board.pauseGame();
    }
    
    public void saveSession() {
        board.saveSession(gameGrid);               
    }

    public void restoreSession() {
        board.restoreSession(gameGrid);
        redrawTilesInGameGrid();
    }
    
    public void saveRecord(){
        board.saveRecord();
    }
    
}
