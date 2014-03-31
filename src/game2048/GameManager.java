package game2048;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author bruno
 */
public class GameManager extends Group {

    private static final int CELL_SIZE = 128;
    private static final int DEFAULT_GRID_SIZE = 4;

    private final int gridSize;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private boolean won;
    private volatile boolean playing = false;

    public GameManager() {
        this(DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        this.gridSize = gridSize;

        createGrid();
        initializeGrid();
    }

    public void move(Direction direction) {
        synchronized (gameGrid) {
            if (playing) {
                return;
            }
        }

        ParallelTransition parallelTransition = new ParallelTransition();

        List<Integer> traversalX = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        List<Integer> traversalY = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());

        if (direction.getX() == 1) {
            Collections.sort(traversalX, Collections.reverseOrder());
        }
        if (direction.getY() == 1) {
            Collections.sort(traversalY, Collections.reverseOrder());
        }

        final Set<Tile> mergedToBeRemoved = new HashSet<>();

        final boolean[] moved = new boolean[]{false};

        traversalX.stream().forEachOrdered(x -> {
            traversalY.stream().forEachOrdered(y -> {
                Location thisloc = new Location(x, y);
                Tile tile = gameGrid.get(thisloc);
                if (tile == null) {
                    return;
                }

                Location farthestLocation = findFarthestLocation(thisloc, direction); // farthest available location
                Location nextLocation = farthestLocation.offset(direction); // calculates to a possible merge
                Tile tileToBeMerged = nextLocation.isValidFor(gridSize) ? gameGrid.get(nextLocation) : null;

                if (tileToBeMerged != null && tileToBeMerged.getValue().equals(tile.getValue()) && !tileToBeMerged.mergedWith(tile)) {
                    tileToBeMerged.merge(tile);

                    gameGrid.put(nextLocation, tileToBeMerged);
                    gameGrid.replace(tile.getLocation(), null);

                    parallelTransition.getChildren().add(animateTile(tile, tileToBeMerged.getLocation()));
                    parallelTransition.getChildren().add(hideTileToBeMerged(tile));
                    mergedToBeRemoved.add(tile);

                    moved[0] = true;

                    if (tileToBeMerged.getValue() == 2048) {
                        won = true;
                    }
                } else if (farthestLocation.equals(tile.getLocation()) == false) {
                    parallelTransition.getChildren().add(animateTile(tile, farthestLocation));

                    gameGrid.put(farthestLocation, tile);
                    gameGrid.replace(tile.getLocation(), null);
                    tile.setLocation(farthestLocation);

                    moved[0] = true;
                }
            });
        });

        // parallelTransition.getChildren().add(animateRandomTileAdded());
        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                playing = false;
            }

            mergedToBeRemoved.forEach(getChildren()::remove);
            if (moved[0]) {
                animateRandomTileAdded();
            }
        });
        playing = true;
        parallelTransition.play();
    }

    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (location.isValidFor(gridSize) && gameGrid.get(location) == null);

        return farthest;
    }

    private void createGrid() {
        IntStream.range(0, gridSize)
                .mapToObj(i -> IntStream.range(0, gridSize).mapToObj(j -> {
                    Location loc = new Location(i, j);
                    locations.add(loc);

                    Rectangle rect2 = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    rect2.getStyleClass().add("grid-cell");
                    return rect2;
                }))
                .flatMap(s -> s)
                .forEach(getChildren()::add);
    }

    private void redrawTiles() {
        getChildren().filtered(c -> c instanceof Tile).forEach(getChildren()::remove);
        gameGrid.values().forEach(t -> {
            double layoutX = t.getLocation().getLayoutX(CELL_SIZE) - (t.getMinWidth() / 2);
            double layoutY = t.getLocation().getLayoutY(CELL_SIZE) - (t.getMinHeight() / 2);

            t.setLayoutX(layoutX);
            t.setLayoutY(layoutY);
            getChildren().add(t);
        });
    }

    interface AddTile {

        void add(int value, int x, int y);
    }

    /**
     * method to use for debugging
     */
    private void __initializeGrid() {
        AddTile addTile = (int value, int x, int y) -> {
            Tile tile = Tile.newTile(value);
            tile.setLocation(new Location(x, y));
            gameGrid.put(tile.getLocation(), tile);
        };

        addTile.add(128, 0, 0);
        addTile.add(512, 0, 1);
        addTile.add(1024, 1, 0);
        addTile.add(2048, 1, 1);

        redrawTiles();
    }

    private void initializeGrid() {
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

        redrawTiles();
    }

    private void animateRandomTileAdded() {
        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null).collect(Collectors.toList());
        Collections.shuffle(availableLocations);
        Location randomLocation = availableLocations.get(new Random().nextInt(availableLocations.size()));

        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);
        gameGrid.put(tile.getLocation(), tile);

        getChildren().add(tile);
        
        animateNewTile(tile).play();
    }

    private Timeline animateTile(Tile tile, Location newLocation) {
        final Timeline timeline = new Timeline();
        final KeyValue kvX = new KeyValue(tile.layoutXProperty(), newLocation.getLayoutX(CELL_SIZE) - (tile.getMinHeight() / 2));
        final KeyValue kvY = new KeyValue(tile.layoutYProperty(), newLocation.getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2));

        Duration animationDuration = Duration.millis(200);
        final KeyFrame kfX = new KeyFrame(animationDuration, kvX);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    private Timeline animateNewTile(Tile tile) {
        final Timeline timeline = new Timeline();
        final KeyValue kvX = new KeyValue(tile.scaleXProperty(), 1);
        final KeyValue kvY = new KeyValue(tile.scaleYProperty(), 1);

        Duration animationDuration = Duration.millis(150);
        final KeyFrame kfX = new KeyFrame(animationDuration, kvX);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    private Timeline hideTileToBeMerged(Tile tile) {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(tile.opacityProperty(), 0);

        Duration animationDuration = Duration.millis(150);
        final KeyFrame kf = new KeyFrame(animationDuration, kv);

        timeline.getKeyFrames().add(kf);

        return timeline;
    }
}
