package io.fxgame.game2048;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserSettings
 * 
 * @author Bruno Borges
 */
public enum UserSettings {

    LOCAL;

    public final static int MARGIN = 36;
    private static final String SETTINGS_FILENAME = "settings.properties";
    private static final String GRID_SIZE_KEY = "gridSize";
    private final File userGameFolder;

    UserSettings() {
        var userHome = System.getProperty("user.home");
        var gamePath = Path.of(userHome, ".fx2048");
        try {
            Files.createDirectories(gamePath);
        } catch (IOException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, "Unable to create settings directory.", e);
        }
        userGameFolder = gamePath.toFile();

        try {
            var isWindows = System.getProperty("os.name").toUpperCase().contains("WINDOWS");
            if (isWindows) {
                Files.setAttribute(gamePath, "dos:hidden", true);
            }
        } catch (IOException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.WARNING, "Unable to hide settings directory.", e);
        }
    }

    public boolean store(Properties data, String fileName) {
        try (var writer = new FileWriter(new File(userGameFolder, fileName))) {
            data.store(writer, fileName);
            return true;
        } catch (IOException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, "Unable to store settings.", e);
            return false;
        }
    }

    public boolean restore(Properties props, String fileName) {
        try (var reader = new FileReader(new File(userGameFolder, fileName))) {
            props.load(reader);
            return true;
        } catch (FileNotFoundException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.INFO, "Properties file not found: {0}", fileName);
            return false;
        } catch (IllegalArgumentException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.WARNING, "Invalid properties file: {0}", fileName);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int getGridSize() {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);

        var gridSize = settings.getProperty(GRID_SIZE_KEY);
        if (gridSize == null) {
            return GridOperator.DEFAULT_GRID_SIZE;
        }

        try {
            return validateGridSize(Integer.parseInt(gridSize));
        } catch (IllegalArgumentException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.WARNING,
                    "Invalid grid size setting. Using default grid size.", e);
            return GridOperator.DEFAULT_GRID_SIZE;
        }
    }

    public void setGridSize(int gridSize) {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);
        settings.setProperty(GRID_SIZE_KEY, Integer.toString(validateGridSize(gridSize)));
        store(settings, SETTINGS_FILENAME);
    }

    private int validateGridSize(int gridSize) {
        if (gridSize < GridOperator.MIN_GRID_SIZE || gridSize > GridOperator.MAX_GRID_SIZE) {
            throw new IllegalArgumentException("Grid size must be of range %s and %s."
                    .formatted(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE));
        }
        return gridSize;
    }

}
