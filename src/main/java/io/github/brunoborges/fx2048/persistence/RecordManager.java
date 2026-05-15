package io.github.brunoborges.fx2048.persistence;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
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

    public static List<BestScore> restoreBestScores() {
        return IntStream.rangeClosed(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE)
                .mapToObj(gridSize -> new BestScore(gridSize, new RecordManager(gridSize).restoreRecord()))
                .toList();
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

    public record BestScore(int gridSize, int score) {

        public String gridLabel() {
            return gridSize + "x" + gridSize;
        }

        public String scoreLabel() {
            return score > 0 ? Integer.toString(score) : "No record";
        }
    }

}
