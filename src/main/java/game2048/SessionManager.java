package game2048;

import javafx.beans.property.StringProperty;
import lombok.SneakyThrows;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * @author José Pereda
 */
class SessionManager {

    private static final String FILE_NAME = "game2048.properties";

    private static final String RECORD_PROPERTY = "record";
    private static final String LOCATION_PROPERTY_TEMPLATE = "location_%d_%d";
    private static final String SCORE_PROPERTY = "score";
    private static final String TIME_PROPERTY = "time";


    private final Properties props = new Properties();
    private final GridOperator gridOperator = new GridOperator();
    private Map<Location, Tile> gameGrid;

    @SneakyThrows
    void saveRecord(Integer score) {
        int oldRecord = restoreRecord();

        props.setProperty(RECORD_PROPERTY, Integer.toString(Math.max(oldRecord, score)));
        props.store(new FileWriter(FILE_NAME), FILE_NAME);
    }

    @SneakyThrows
    int restoreRecord() {
        if (Files.exists(Paths.get(FILE_NAME))) {
            Reader reader = new FileReader(FILE_NAME);
            props.load(reader);

            String score = props.getProperty(RECORD_PROPERTY);
            return score != null ? Integer.valueOf(score) : 0;
        } else {
            return 0;
        }
    }

    @SneakyThrows
    void saveSession(Map<Location, Tile> gameGrid, Integer score, Long time) {
        this.gameGrid = gameGrid;
        gridOperator.iterateGrid(this::saveTile);
        props.setProperty(SCORE_PROPERTY, score.toString());
        props.setProperty(TIME_PROPERTY, time.toString());
        props.store(new FileWriter(FILE_NAME), FILE_NAME);
    }

    private void saveTile(int x, int y) {
        Tile tile = gameGrid.get(new Location(x, y));
        props.setProperty(String.format(LOCATION_PROPERTY_TEMPLATE, x, y),
                tile != null ? String.valueOf(tile.getValue()) : "0");
    }

    @SneakyThrows
    int restoreSession(Map<Location, Tile> gameGrid, StringProperty time) {
        Reader reader = new FileReader(FILE_NAME);
        props.load(reader);

        this.gameGrid = gameGrid;
        gridOperator.iterateGrid(this::restoreTile);

        time.set(props.getProperty(TIME_PROPERTY));

        String score = props.getProperty(SCORE_PROPERTY);
        return score != null ? Integer.valueOf(score) : 0;
    }

    private void restoreTile(int x, int y) {
        String val = props.getProperty(String.format(LOCATION_PROPERTY_TEMPLATE, x, y));
        if (val != null && !val.equals("0")) {
            Tile t = Tile.newTile(Integer.valueOf(val));
            Location l = new Location(x, y);
            t.setLocation(l);
            gameGrid.put(l, t);
        }
    }

}
