package io.github.brunoborges.fx2048.settings;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

/**
 * Auto-save modes for the game session.
 */
public enum AutoSaveMode {

    OFF("Off"),
    ON_EXIT("On Exit"),
    AFTER_EVERY_MOVE("After Every Move");

    private final String displayName;

    AutoSaveMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Returns the {@code AutoSaveMode} whose {@link #name()} matches {@code value} (case-insensitive).
     * Returns {@link #OFF} when {@code value} is {@code null}, empty, or does not match any mode.
     */
    public static AutoSaveMode fromString(String value) {
        for (var mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return OFF;
    }
}
