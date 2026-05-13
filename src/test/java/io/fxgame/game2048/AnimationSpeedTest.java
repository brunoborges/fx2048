package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
