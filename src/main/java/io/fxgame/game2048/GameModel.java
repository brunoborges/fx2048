package io.fxgame.game2048;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class GameModel {

    static final int FINAL_VALUE_TO_WIN = 2048;

    private final GridOperator gridOperator;
    private final Random random;
    private final Map<Location, Integer> values = new HashMap<>();
    private final List<Location> locations = new ArrayList<>();

    record TileState(Location location, int value) {}

    record Movement(Location source, Location destination, boolean merge, int mergedValue) {
        static Movement move(Location source, Location destination) {
            return new Movement(source, destination, false, 0);
        }

        static Movement merge(Location source, Location destination, int mergedValue) {
            return new Movement(source, destination, true, mergedValue);
        }
    }

    record MoveResult(List<Movement> movements, int points, boolean won) {
        boolean tilesMoved() {
            return !movements.isEmpty();
        }
    }

    GameModel(GridOperator gridOperator) {
        this(gridOperator, new Random());
    }

    GameModel(GridOperator gridOperator, Random random) {
        this.gridOperator = gridOperator;
        this.random = random;
        initialize();
    }

    void initialize() {
        values.clear();
        locations.clear();
        gridOperator.traverseGrid((x, y) -> {
            var location = new Location(x, y);
            locations.add(location);
            values.put(location, null);
            return 0;
        });
    }

    void startGame() {
        initialize();

        var randomLocations = new ArrayList<>(locations);
        Collections.shuffle(randomLocations, random);

        var firstTile = new TileState(randomLocations.get(0), randomTileValue());
        values.put(firstTile.location(), firstTile.value());

        if (random.nextFloat() <= 0.8f) {
            var secondValue = randomTileValue();
            if (firstTile.value() == 4 && secondValue == 4) {
                secondValue = 2;
            }

            var secondTile = new TileState(randomLocations.get(1), secondValue);
            values.put(secondTile.location(), secondTile.value());
        }
    }

    Map<Location, Integer> snapshot() {
        var snapshot = new HashMap<Location, Integer>();
        values.forEach((location, value) -> snapshot.put(location, value == null ? 0 : value));
        return snapshot;
    }

    void restoreSnapshot(Map<Location, Integer> snapshot) {
        initialize();
        snapshot.forEach((location, value) -> {
            if (gridOperator.isValidLocation(location)) {
                values.put(location, value == null || value == 0 ? null : value);
            }
        });
    }

    MoveResult move(Direction direction) {
        var movements = new ArrayList<Movement>();
        var mergedLocations = new HashSet<Location>();
        var points = new AtomicInteger();
        var won = new AtomicBoolean();

        gridOperator.sortGrid(direction);
        gridOperator.traverseGrid((x, y) -> {
            var currentLocation = new Location(x, y);
            var currentValue = values.get(currentLocation);
            if (currentValue == null) {
                return 0;
            }

            var farthestLocation = findFarthestLocation(currentLocation, direction);
            var nextLocation = farthestLocation.offset(direction);
            var nextValue = values.get(nextLocation);

            if (Objects.equals(nextValue, currentValue) && !mergedLocations.contains(nextLocation)) {
                var mergedValue = currentValue + nextValue;
                values.put(nextLocation, mergedValue);
                values.put(currentLocation, null);
                mergedLocations.add(nextLocation);
                movements.add(Movement.merge(currentLocation, nextLocation, mergedValue));
                points.addAndGet(mergedValue);
                won.compareAndSet(false, mergedValue == FINAL_VALUE_TO_WIN);
                return 1;
            }

            if (!farthestLocation.equals(currentLocation)) {
                values.put(farthestLocation, currentValue);
                values.put(currentLocation, null);
                movements.add(Movement.move(currentLocation, farthestLocation));
                return 1;
            }

            return 0;
        });

        return new MoveResult(List.copyOf(movements), points.get(), won.get());
    }

    Optional<TileState> addRandomTile() {
        var randomLocation = findRandomAvailableLocation();
        if (randomLocation.isEmpty()) {
            return Optional.empty();
        }

        var tile = new TileState(randomLocation.get(), randomTileValue());
        values.put(tile.location(), tile.value());
        return Optional.of(tile);
    }

    boolean isFull() {
        return values.values().stream().noneMatch(Objects::isNull);
    }

    boolean hasMergeMovements() {
        for (var direction : List.of(Direction.UP, Direction.LEFT)) {
            if (gridOperator.traverseGrid((x, y) -> {
                var location = new Location(x, y);
                var value = values.get(location);
                return value != null && Objects.equals(value, values.get(location.offset(direction))) ? 1 : 0;
            }) > 0) {
                return true;
            }
        }

        return false;
    }

    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (gridOperator.isValidLocation(location) && values.get(location) == null);

        return farthest;
    }

    private Optional<Location> findRandomAvailableLocation() {
        var availableLocations = locations.stream()
                .filter(location -> values.get(location) == null)
                .toList();

        if (availableLocations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(availableLocations.get(random.nextInt(availableLocations.size())));
    }

    private int randomTileValue() {
        return random.nextDouble() < 0.9 ? 2 : 4;
    }
}
