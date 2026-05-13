package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AutoSaveModeTest {

    @Test
    void displayNamesAreReadable() {
        assertEquals("Off", AutoSaveMode.OFF.toString());
        assertEquals("On Exit", AutoSaveMode.ON_EXIT.toString());
        assertEquals("After Every Move", AutoSaveMode.AFTER_EVERY_MOVE.toString());
    }

    @Test
    void fromStringReturnsMatchingMode() {
        assertEquals(AutoSaveMode.OFF, AutoSaveMode.fromString("OFF"));
        assertEquals(AutoSaveMode.ON_EXIT, AutoSaveMode.fromString("ON_EXIT"));
        assertEquals(AutoSaveMode.AFTER_EVERY_MOVE, AutoSaveMode.fromString("AFTER_EVERY_MOVE"));
    }

    @Test
    void fromStringIsCaseInsensitive() {
        assertEquals(AutoSaveMode.ON_EXIT, AutoSaveMode.fromString("on_exit"));
        assertEquals(AutoSaveMode.AFTER_EVERY_MOVE, AutoSaveMode.fromString("after_every_move"));
    }

    @Test
    void fromStringReturnsOffForUnknownValues() {
        assertEquals(AutoSaveMode.OFF, AutoSaveMode.fromString("unknown"));
        assertEquals(AutoSaveMode.OFF, AutoSaveMode.fromString(""));
        assertEquals(AutoSaveMode.OFF, AutoSaveMode.fromString(null));
    }
}
