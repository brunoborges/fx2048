package io.github.brunoborges.fx2048.persistence;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import io.github.brunoborges.fx2048.settings.AnimationSpeed;

class UserSettingsTest {

    private static final String SETTINGS_FILENAME = "settings.properties";
    private static final String ANIMATION_SPEED_KEY = "animationSpeed";

    @Test
    void animationSpeedIsSavedAndRestored() {
        var settings = UserSettings.LOCAL;
        var originalProperties = new Properties();
        var hadOriginalSettings = settings.restore(originalProperties, SETTINGS_FILENAME);

        try {
            settings.setAnimationSpeed(AnimationSpeed.FAST);
            assertEquals(AnimationSpeed.FAST, settings.getAnimationSpeed());
        } finally {
            restoreOriginalSettings(settings, originalProperties, hadOriginalSettings);
        }
    }

    @Test
    void invalidAnimationSpeedFallsBackToDefault() {
        var settings = UserSettings.LOCAL;
        var originalProperties = new Properties();
        var hadOriginalSettings = settings.restore(originalProperties, SETTINGS_FILENAME);
        var invalidProperties = new Properties();
        invalidProperties.putAll(originalProperties);
        invalidProperties.setProperty(ANIMATION_SPEED_KEY, "invalid");

        try {
            settings.store(invalidProperties, SETTINGS_FILENAME);
            try (var logs = CapturingLogHandler.capture(Logger.getLogger(UserSettings.class.getName()))) {
                assertEquals(AnimationSpeed.DEFAULT, settings.getAnimationSpeed());
                assertTrue(logs.records().stream().anyMatch(record -> record.getLevel() == Level.WARNING));
            }
        } finally {
            restoreOriginalSettings(settings, originalProperties, hadOriginalSettings);
        }
    }

    @Test
    void missingAnimationSpeedFallsBackToDefault() {
        var settings = UserSettings.LOCAL;
        var originalProperties = new Properties();
        var hadOriginalSettings = settings.restore(originalProperties, SETTINGS_FILENAME);
        var propertiesWithoutAnimationSpeed = new Properties();
        propertiesWithoutAnimationSpeed.putAll(originalProperties);
        propertiesWithoutAnimationSpeed.remove(ANIMATION_SPEED_KEY);

        try {
            settings.store(propertiesWithoutAnimationSpeed, SETTINGS_FILENAME);
            assertEquals(AnimationSpeed.DEFAULT, settings.getAnimationSpeed());
        } finally {
            restoreOriginalSettings(settings, originalProperties, hadOriginalSettings);
        }
    }

    private void restoreOriginalSettings(UserSettings settings, Properties originalProperties, boolean hadOriginalSettings) {
        if (hadOriginalSettings) {
            settings.store(originalProperties, SETTINGS_FILENAME);
            return;
        }

        try {
            Files.deleteIfExists(Path.of(System.getProperty("user.home"), ".fx2048", SETTINGS_FILENAME));
        } catch (IOException ignored) {
        }
    }

    private static final class CapturingLogHandler extends Handler implements AutoCloseable {
        private final Logger logger;
        private final Handler[] previousHandlers;
        private final Level previousLevel;
        private final boolean previousUseParentHandlers;
        private final List<LogRecord> records = new ArrayList<>();

        private CapturingLogHandler(Logger logger) {
            this.logger = logger;
            previousHandlers = logger.getHandlers();
            previousLevel = logger.getLevel();
            previousUseParentHandlers = logger.getUseParentHandlers();
            Arrays.stream(previousHandlers).forEach(logger::removeHandler);
            setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
            logger.addHandler(this);
        }

        static CapturingLogHandler capture(Logger logger) {
            return new CapturingLogHandler(logger);
        }

        List<LogRecord> records() {
            return records;
        }

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            logger.removeHandler(this);
            Arrays.stream(previousHandlers).forEach(logger::addHandler);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParentHandlers);
        }
    }
}
