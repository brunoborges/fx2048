package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class GameTimerTest {

    @Test
    void formatsElapsedTimeWithoutDayWraparound() {
        assertEquals("00:00:00", GameTimer.formatElapsed(0));
        assertEquals("01:01:01", GameTimer.formatElapsed(nanos(1, 1, 1)));
        assertEquals("25:00:00", GameTimer.formatElapsed(nanos(25, 0, 0)));
    }

    @Test
    void tracksPauseResumeAndRestoreUsingElapsedNanos() {
        var ticker = new AtomicLong();
        var timer = new GameTimer(ticker::get);

        timer.startNew();
        ticker.set(nanos(0, 0, 5));
        assertEquals(nanos(0, 0, 5), timer.elapsedNanos());

        timer.pause();
        ticker.set(nanos(0, 0, 9));
        assertEquals(nanos(0, 0, 5), timer.elapsedNanos());

        timer.resume();
        ticker.set(nanos(0, 0, 11));
        assertEquals(nanos(0, 0, 7), timer.elapsedNanos());

        timer.restore(nanos(0, 1, 0));
        ticker.set(nanos(0, 0, 12));
        assertEquals(nanos(0, 1, 1), timer.elapsedNanos());
    }

    private static long nanos(long hours, long minutes, long seconds) {
        return ((hours * 3_600) + (minutes * 60) + seconds) * 1_000_000_000L;
    }
}
