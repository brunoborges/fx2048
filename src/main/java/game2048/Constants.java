package game2048;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javafx.util.Duration;

/**
 * Constants
 */
final class Constants {

    protected static final String VERSION = "1.1.0";
    protected static final String APP_TITLE = "2048FX";

    protected static final int DEFAULT_GRID_SIZE = 4;

    protected static final int CELL_SIZE = 128;
    protected static final int BORDER_WIDTH = (14 + 2) / 2;
    protected static final int TOP_HEIGHT = 92;
    protected static final int GAP_HEIGHT = 50;
    protected static final int TOOLBAR_HEIGHT = 80;

    protected static final int FINAL_VALUE_TO_WIN = 2048;

    protected static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);
    protected static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);
    protected static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);

    protected static final DateTimeFormatter TIMER_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private Constants() {
    }

}
