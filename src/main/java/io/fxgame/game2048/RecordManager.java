package io.fxgame.game2048;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jose Pereda
 */
public class RecordManager {

    private static final Logger LOGGER = Logger.getLogger(RecordManager.class.getName());

    public final String propertiesFilename;
    private final Properties props = new Properties();

    public RecordManager(int grid_size) {
        this.propertiesFilename = "game2048_" + grid_size + "_record.properties";
    }

    public void saveRecord(Integer score) {
        int oldRecord = restoreRecord();
        props.clear();
        props.setProperty("record", Integer.toString(Math.max(oldRecord, score)));
        UserSettings.LOCAL.store(props, propertiesFilename);
    }

    public int restoreRecord() {
        props.clear();
        UserSettings.LOCAL.restore(props, propertiesFilename);

        String score = props.getProperty("record");
        if (score != null) {
            try {
                return parseRecord(score);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid record value in " + propertiesFilename, e);
            }
        }
        return 0;
    }

    private int parseRecord(String score) {
        var record = Integer.parseInt(score);
        if (record < 0) {
            throw new IllegalArgumentException("Record must not be negative");
        }
        return record;
    }

}
