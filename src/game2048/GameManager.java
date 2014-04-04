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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
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
    private static final int BORDER_WIDTH=(14+2)/2;
    // grid_width=4*cell_size + 2*cell_stroke/2d (14px css)+2*grid_stroke/2d (2 px css)
    private static final int GRID_WIDTH = CELL_SIZE*DEFAULT_GRID_SIZE+BORDER_WIDTH*2;
    private static final int TOP_HEIGHT = 92;
    
    private final int gridSize;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private boolean won;
    private volatile boolean playing = false;
    private final IntegerProperty score=new SimpleIntegerProperty(0);
    private final IntegerProperty points=new SimpleIntegerProperty(0);
    
    private final VBox vGame=new VBox(50);
    private final HBox hTop=new HBox(0);
    private final Label lblScore=new Label("0");
    private final Label lblPoints=new Label();
    private final HBox hBottom=new HBox();
    private final Group grid=new Group();
    
    public GameManager() {
        this(DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        this.gridSize = gridSize;

        createScore();
        
        createGrid();
        initializeGrid();
        
        this.setManaged(false);
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

        points.set(0);
        
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

                if (tileToBeMerged != null && tileToBeMerged.getValue().equals(tile.getValue()) && !tileToBeMerged.isMerged()) {
                    tileToBeMerged.merge(tile);

                    gameGrid.put(nextLocation, tileToBeMerged);
                    gameGrid.replace(tile.getLocation(), null);

                    parallelTransition.getChildren().add(animateTile(tile, tileToBeMerged.getLocation()));
                    parallelTransition.getChildren().add(hideTileToBeMerged(tile));
                    mergedToBeRemoved.add(tile);

                    moved[0] = true;
                    points.set(points.get()+tileToBeMerged.getValue());
                    score.set(score.get()+tileToBeMerged.getValue());
                    
                    if (tileToBeMerged.getValue() == FINAL_VALUE_TO_WIN) {
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

        if(points.get()>0){
            animateScore(points.getValue().toString()).play();
        }
        // parallelTransition.getChildren().add(animateRandomTileAdded());
        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                playing = false;
            }

            mergedToBeRemoved.forEach(grid.getChildren()::remove);
            if (moved[0]) {
                animateRandomTileAdded();
            }

            // reset merged after each movement
            gameGrid.values().stream().filter(t -> t != null).forEach(Tile::clearMerge);
            
            if (won) {
                animateWinner();
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

    private void createScore() {
        Label lblTitle=new Label("2048");
        lblTitle.getStyleClass().add("title");
        Label lblSubtitle=new Label("FX");
        lblSubtitle.getStyleClass().add("subtitle");
        HBox hFill=new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);
        VBox vScore=new VBox();
        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("vbox");
        Label lblTit=new Label("SCORE");
        lblTit.getStyleClass().add("titScore");
        lblScore.getStyleClass().add("score");
        lblScore.textProperty().bind(score.asString());
        vScore.getChildren().addAll(lblTit,lblScore);
        
        hTop.getChildren().addAll(lblTitle,lblSubtitle,hFill,vScore);
        hTop.setMinSize(GRID_WIDTH,TOP_HEIGHT);
        hTop.setPrefSize(GRID_WIDTH,TOP_HEIGHT);
        hTop.setMaxSize(GRID_WIDTH,TOP_HEIGHT);
        
        vGame.getChildren().add(hTop);
        getChildren().add(vGame);
        
        lblPoints.getStyleClass().add("points");
        getChildren().add(lblPoints);
        
    }
    
    private void createGrid() {
        IntStream.range(0, gridSize)
                .mapToObj(i -> IntStream.range(0, gridSize).mapToObj(j -> {
                    Location loc = new Location(i, j);
                    locations.add(loc);

                    Rectangle rect2 = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    rect2.setArcHeight(CELL_SIZE/6d);
                    rect2.setArcWidth(CELL_SIZE/6d);
                    rect2.getStyleClass().add("grid-cell");
                    return rect2;
                }))
                .flatMap(s -> s)
                .forEach(grid.getChildren()::add);
        
        grid.getStyleClass().add("grid");
        grid.setManaged(false);
        grid.setLayoutX(BORDER_WIDTH);
        grid.setLayoutY(BORDER_WIDTH);
        hBottom.getStyleClass().add("backGrid");
        hBottom.setMinSize(GRID_WIDTH,GRID_WIDTH);
        hBottom.setPrefSize(GRID_WIDTH,GRID_WIDTH);
        hBottom.setMaxSize(GRID_WIDTH,GRID_WIDTH);
        
        hBottom.getChildren().add(grid);
        vGame.getChildren().add(hBottom);
        
    }

    private void redrawTiles() {
        grid.getChildren().filtered(c -> c instanceof Tile).forEach(grid.getChildren()::remove);
        gameGrid.values().forEach(t -> {
            double layoutX = t.getLocation().getLayoutX(CELL_SIZE) - (t.getMinWidth() / 2);
            double layoutY = t.getLocation().getLayoutY(CELL_SIZE) - (t.getMinHeight() / 2);

            t.setLayoutX(layoutX);
            t.setLayoutY(layoutY);
            grid.getChildren().add(t);
        });
    }
    
    private Timeline animateScore(String v1) {
        final Timeline timeline = new Timeline();
        lblPoints.setText("+"+v1);
        lblPoints.setOpacity(1);
        lblPoints.setLayoutX(400);
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

    private void animateWinner() {
        Logger.getLogger(this.getClass().getName()).info("WINNER!");
        ParallelTransition pt = new ParallelTransition();
        gameGrid.values().stream().filter(t -> t != null).forEach(t -> {
            FadeTransition ft = new FadeTransition();
            ft.setDuration(Duration.millis(200));
            ft.setNode(t);
            ft.setFromValue(1.0);
            ft.setToValue(0.1);
            ft.setCycleCount(4);
            pt.getChildren().add(ft);
        });
        pt.play();
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

        grid.getChildren().add(tile);

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
