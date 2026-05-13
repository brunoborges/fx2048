package io.fxgame.game2048;

/**
 * Auto-save modes for the game session.
 */
enum AutoSaveMode {

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
    static AutoSaveMode fromString(String value) {
        for (var mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return OFF;
    }
}
