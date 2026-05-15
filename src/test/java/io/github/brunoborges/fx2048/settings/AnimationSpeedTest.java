package io.github.brunoborges.fx2048.settings;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.util.Duration;

class AnimationSpeedTest {

    @Test
    void normalKeepsOriginalDuration() {
        assertEquals(Duration.millis(100), AnimationSpeed.NORMAL.scale(Duration.millis(100)));
    }

    @Test
    void fastUsesShorterDuration() {
        assertEquals(Duration.millis(50), AnimationSpeed.FAST.scale(Duration.millis(100)));
    }

    @Test
    void slowUsesLongerDuration() {
        assertEquals(Duration.millis(150), AnimationSpeed.SLOW.scale(Duration.millis(100)));
    }

    @Test
    void offUsesInstantDuration() {
        assertEquals(Duration.ZERO, AnimationSpeed.OFF.scale(Duration.millis(100)));
    }

    @Test
    void onlyOffDisablesAnimations() {
        assertFalse(AnimationSpeed.SLOW.isInstant());
        assertFalse(AnimationSpeed.NORMAL.isInstant());
        assertFalse(AnimationSpeed.FAST.isInstant());
        assertTrue(AnimationSpeed.OFF.isInstant());
    }
}
