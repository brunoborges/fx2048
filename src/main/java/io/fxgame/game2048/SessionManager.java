package io.fxgame.game2048;

import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.Properties;

/**
 * @author Jose Pereda
 */
public class SessionManager {

    public final String propertiesFilename;
    private final Properties props = new Properties();
    private final GridOperator gridOperator;

    public SessionManager(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        this.propertiesFilename = "game2048_" + gridOperator.getGridSize() + ".properties";
    }

    protected void saveSession(Map<Location, Tile> gameGrid, Integer score, Long time) {
        gridOperator.traverseGrid((x, y) -> {
            var tile = gameGrid.get(new Location(x, y));
            props.setProperty("Location_" + x + "_" + y, tile != null ? tile.getValue().toString() : "0");
            return 0;
        });
        props.setProperty("score", score.toString());
        props.setProperty("time", time.toString());
        UserSettings.LOCAL.store(props, propertiesFilename);
    }

    protected int restoreSession(Map<Location, Tile> gameGrid, StringProperty time) {
        UserSettings.LOCAL.restore(props, propertiesFilename);

        gridOperator.traverseGrid((x, y) -> {
            var val = props.getProperty("Location_" + x + "_" + y);
            if (!val.equals("0")) {
                var tile = Tile.newTile(Integer.parseInt(val));
                var location = new Location(x, y);
                tile.setLocation(location);
                gameGrid.put(location, tile);
            }
            return 0;
        });

        time.set(props.getProperty("time"));

        var score = props.getProperty("score");
        if (score != null) {
            return Integer.parseInt(score);
        }
        return 0;
    }

}
