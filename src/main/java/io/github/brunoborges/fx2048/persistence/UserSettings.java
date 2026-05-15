package io.github.brunoborges.fx2048.persistence;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
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
    private static final String ANIMATION_SPEED_KEY = "animationSpeed";
    private static final String AUTO_SAVE_KEY = "autoSave";
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

    public AnimationSpeed getAnimationSpeed() {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);

        var animationSpeed = settings.getProperty(ANIMATION_SPEED_KEY);
        if (animationSpeed == null) {
            return AnimationSpeed.DEFAULT;
        }

        try {
            return AnimationSpeed.valueOf(animationSpeed.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.WARNING,
                    "Invalid animation speed setting ''{0}''. Using default animation speed.", animationSpeed);
            return AnimationSpeed.DEFAULT;
        }
    }

    public void setAnimationSpeed(AnimationSpeed animationSpeed) {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);
        settings.setProperty(ANIMATION_SPEED_KEY, validateAnimationSpeed(animationSpeed).name());
        store(settings, SETTINGS_FILENAME);
    }

    public AutoSaveMode getAutoSave() {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);
        return AutoSaveMode.fromString(settings.getProperty(AUTO_SAVE_KEY, AutoSaveMode.OFF.name()));
    }

    public void setAutoSave(AutoSaveMode mode) {
        var settings = new Properties();
        restore(settings, SETTINGS_FILENAME);
        settings.setProperty(AUTO_SAVE_KEY, mode.name());
        store(settings, SETTINGS_FILENAME);
    }

    private int validateGridSize(int gridSize) {
        if (gridSize < GridOperator.MIN_GRID_SIZE || gridSize > GridOperator.MAX_GRID_SIZE) {
            throw new IllegalArgumentException("Grid size must be of range %s and %s."
                    .formatted(GridOperator.MIN_GRID_SIZE, GridOperator.MAX_GRID_SIZE));
        }
        return gridSize;
    }

    private AnimationSpeed validateAnimationSpeed(AnimationSpeed animationSpeed) {
        return Objects.requireNonNull(animationSpeed, "Animation speed cannot be null.");
    }

}
