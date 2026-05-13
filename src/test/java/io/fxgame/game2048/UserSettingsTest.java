package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

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
            assertEquals(AnimationSpeed.DEFAULT, settings.getAnimationSpeed());
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
}
