package io.fxgame.game2048;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jose Pereda
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    public final String propertiesFilename;
    private final Properties props = new Properties();
    private final GridOperator gridOperator;

    protected record SessionData(Map<Location, Tile> gameGrid, int score, long time, int moveCount) {}

    public SessionManager(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        this.propertiesFilename = "game2048_" + gridOperator.getGridSize() + ".properties";
    }

    protected boolean saveSession(Map<Location, Tile> gameGrid, Integer score, Long time, Integer moveCount) {
        props.clear();
        gridOperator.traverseGrid((x, y) -> {
            var tile = gameGrid.get(new Location(x, y));
            props.setProperty("Location_" + x + "_" + y, tile != null ? tile.getValue().toString() : "0");
            return 0;
        });
        props.setProperty("score", score.toString());
        props.setProperty("time", time.toString());
        props.setProperty("moves", moveCount.toString());
        return UserSettings.LOCAL.store(props, propertiesFilename);
    }

    protected Optional<SessionData> restoreSession() {
        props.clear();
        if (!UserSettings.LOCAL.restore(props, propertiesFilename)) {
            return Optional.empty();
        }

        try {
            var restoredGrid = new HashMap<Location, Tile>();
            gridOperator.traverseGrid((x, y) -> {
                var val = requireProperty("Location_" + x + "_" + y);
                var tileValue = Integer.parseInt(val);
                var location = new Location(x, y);
                restoredGrid.put(location, null);
                if (tileValue != 0) {
                    validateTileValue(tileValue);
                    var tile = Tile.newTile(tileValue);
                    tile.setLocation(location);
                    restoredGrid.put(location, tile);
                }
                return 0;
            });

            var score = parseNonNegativeInt(requireProperty("score"), "score");
            var time = parseNonNegativeLong(requireProperty("time"), "time");
            var moveCount = parseNonNegativeInt(props.getProperty("moves", "0"), "moves");

            return Optional.of(new SessionData(restoredGrid, score, time, moveCount));
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid saved session: " + propertiesFilename, e);
            return Optional.empty();
        }
    }

    private String requireProperty(String key) {
        var value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing session property: " + key);
        }
        return value;
    }

    private int parseNonNegativeInt(String value, String propertyName) {
        var parsedValue = Integer.parseInt(value);
        if (parsedValue < 0) {
            throw new IllegalArgumentException(propertyName + " must not be negative");
        }
        return parsedValue;
    }

    private long parseNonNegativeLong(String value, String propertyName) {
        var parsedValue = Long.parseLong(value);
        if (parsedValue < 0) {
            throw new IllegalArgumentException(propertyName + " must not be negative");
        }
        return parsedValue;
    }

    private void validateTileValue(int value) {
        if (value < 2 || (value & (value - 1)) != 0) {
            throw new IllegalArgumentException("Tile value must be a positive power of 2: " + value);
        }
    }

}
