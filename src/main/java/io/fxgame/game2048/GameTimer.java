package io.fxgame.game2048;

import java.util.function.LongSupplier;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

final class GameTimer {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR = 3_600L;

    private final LongSupplier nanoTime;
    private final StringProperty clock = new SimpleStringProperty(formatElapsed(0));
    private final Timeline timeline;
    private long elapsedNanos;
    private long startedAtNanos;
    private boolean running;

    GameTimer() {
        this(System::nanoTime, true);
    }

    GameTimer(LongSupplier nanoTime) {
        this(nanoTime, false);
    }

    private GameTimer(LongSupplier nanoTime, boolean useTimeline) {
        this.nanoTime = nanoTime;
        if (useTimeline) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> updateClock()));
            timeline.setCycleCount(Animation.INDEFINITE);
        } else {
            timeline = null;
        }
    }

    ReadOnlyStringProperty clockProperty() {
        return clock;
    }

    void startNew() {
        elapsedNanos = 0;
        running = false;
        resume();
    }

    void resume() {
        if (!running) {
            startedAtNanos = nanoTime.getAsLong();
            running = true;
        }
        updateClock();
        if (timeline != null) {
            timeline.playFromStart();
        }
    }

    void pause() {
        if (running) {
            elapsedNanos = elapsedNanos();
            running = false;
        }
        if (timeline != null) {
            timeline.stop();
        }
        updateClock();
    }

    void restore(long elapsedNanos) {
        if (elapsedNanos < 0) {
            throw new IllegalArgumentException("Elapsed time must not be negative");
        }
        this.elapsedNanos = elapsedNanos;
        running = false;
        resume();
    }

    long elapsedNanos() {
        if (!running) {
            return elapsedNanos;
        }
        return elapsedNanos + nanoTime.getAsLong() - startedAtNanos;
    }

    private void updateClock() {
        clock.set(formatElapsed(elapsedNanos()));
    }

    static String formatElapsed(long elapsedNanos) {
        var elapsedSeconds = Math.max(0, elapsedNanos / NANOS_PER_SECOND);
        var hours = elapsedSeconds / SECONDS_PER_HOUR;
        var minutes = (elapsedSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        var seconds = elapsedSeconds % SECONDS_PER_MINUTE;
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }
}
