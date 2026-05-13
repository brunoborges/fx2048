package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class RecordManagerTest {

    @Test
    void restoresBestScoresAcrossGridSizesAndHandlesMissingRecords() {
        var settings = UserSettings.LOCAL;
        var originalRecords = backupRecords(settings);

        try {
            clearAllRecords();
            new RecordManager(4).saveRecord(4096);
            new RecordManager(6).saveRecord(8192);

            var bestScores = RecordManager.restoreBestScores();

            assertEquals(GridOperator.MAX_GRID_SIZE - GridOperator.MIN_GRID_SIZE + 1, bestScores.size());
            assertEquals("4x4", bestScores.getFirst().gridLabel());
            assertEquals("4096", bestScores.getFirst().scoreLabel());
            assertEquals("6x6", bestScores.get(2).gridLabel());
            assertEquals("8192", bestScores.get(2).scoreLabel());
            assertEquals("No record", bestScores.get(1).scoreLabel());
            assertEquals("No record", bestScores.getLast().scoreLabel());
        } finally {
            restoreRecords(settings, originalRecords);
        }
    }

    private Map<String, Properties> backupRecords(UserSettings settings) {
        var originalRecords = new HashMap<String, Properties>();
        for (var gridSize = GridOperator.MIN_GRID_SIZE; gridSize <= GridOperator.MAX_GRID_SIZE; gridSize++) {
            var filename = new RecordManager(gridSize).propertiesFilename;
            var properties = new Properties();
            if (settings.restore(properties, filename)) {
                originalRecords.put(filename, properties);
            }
        }
        return originalRecords;
    }

    private void restoreRecords(UserSettings settings, Map<String, Properties> originalRecords) {
        IntStream.rangeClosed(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE)
                .mapToObj(gridSize -> new RecordManager(gridSize).propertiesFilename)
                .forEach(filename -> {
                    var originalProperties = originalRecords.get(filename);
                    if (originalProperties != null) {
                        settings.store(originalProperties, filename);
                        return;
                    }
                    deleteRecordFile(filename);
                });
    }

    private void clearAllRecords() {
        IntStream.rangeClosed(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE)
                .mapToObj(gridSize -> new RecordManager(gridSize).propertiesFilename)
                .forEach(this::deleteRecordFile);
    }

    private void deleteRecordFile(String filename) {
        try {
            Files.deleteIfExists(Path.of(System.getProperty("user.home"), ".fx2048", filename));
        } catch (IOException ignored) {
        }
    }
}
