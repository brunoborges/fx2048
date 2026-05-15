package io.github.brunoborges.fx2048.persistence;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.brunoborges.fx2048.game.GridOperator;
import io.github.brunoborges.fx2048.game.Location;
import io.github.brunoborges.fx2048.game.Tile;

/**
 * @author Jose Pereda
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    public final String propertiesFilename;
    private final Properties props = new Properties();
    private final GridOperator gridOperator;
    public record SessionData(Map<Location, Integer> gridValues, int score, long time, int moveCount) {}

    public SessionManager(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        this.propertiesFilename = "game2048_" + gridOperator.getGridSize() + ".properties";
    }
    public boolean saveSession(Map<Location, Integer> gridValues, Integer score, Long time, Integer moveCount) {
        props.clear();
        gridOperator.traverseGrid((x, y) -> {
            var tileValue = gridValues.get(new Location(x, y));
            props.setProperty("Location_" + x + "_" + y, Integer.toString(tileValue == null ? 0 : tileValue));
            return 0;
        });
        props.setProperty("score", score.toString());
        props.setProperty("time", time.toString());
        props.setProperty("moves", moveCount.toString());
        return UserSettings.LOCAL.store(props, propertiesFilename);
    }
    public Optional<SessionData> restoreSession() {
        props.clear();
        if (!UserSettings.LOCAL.restore(props, propertiesFilename)) {
            return Optional.empty();
        }

        try {
            var restoredValues = new HashMap<Location, Integer>();
            gridOperator.traverseGrid((x, y) -> {
                var val = requireProperty("Location_" + x + "_" + y);
                var tileValue = Integer.parseInt(val);
                var location = new Location(x, y);
                restoredValues.put(location, 0);
                if (tileValue != 0) {
                    validateTileValue(tileValue);
                    restoredValues.put(location, tileValue);
                }
                return 0;
            });

            var score = parseNonNegativeInt(requireProperty("score"), "score");
            var time = parseNonNegativeLong(requireProperty("time"), "time");
            var moveCount = parseNonNegativeInt(props.getProperty("moves", "0"), "moves");

            return Optional.of(new SessionData(restoredValues, score, time, moveCount));
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
