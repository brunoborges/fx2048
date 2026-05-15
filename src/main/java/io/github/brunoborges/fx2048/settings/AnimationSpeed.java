package io.github.brunoborges.fx2048.settings;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import javafx.util.Duration;

public enum AnimationSpeed {
    SLOW("Slow", 1.5),
    NORMAL("Normal", 1.0),
    FAST("Fast", 0.5),
    OFF("Off / instant", 0.0);
    public static final AnimationSpeed DEFAULT = NORMAL;

    private final String label;
    private final double multiplier;

    AnimationSpeed(String label, double multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }
    public Duration scale(Duration duration) {
        return multiplier == 0.0 ? Duration.ZERO : duration.multiply(multiplier);
    }
    public boolean isInstant() {
        return multiplier == 0.0;
    }

    @Override
    public String toString() {
        return label;
    }
}
