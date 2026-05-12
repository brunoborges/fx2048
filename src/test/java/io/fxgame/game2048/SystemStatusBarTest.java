package io.fxgame.game2048;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SystemStatusBarTest {

    @Test
    void formatsUnavailableCpuLoad() {
        assertEquals("CPU --", SystemStatusBar.formatCpuLoad(-1));
        assertEquals("CPU --", SystemStatusBar.formatCpuLoad(Double.NaN));
    }

    @Test
    void formatsCpuLoadAsPercent() {
        assertEquals("CPU 0%", SystemStatusBar.formatCpuLoad(0));
        assertEquals("CPU 13%", SystemStatusBar.formatCpuLoad(0.125));
        assertEquals("CPU 100%", SystemStatusBar.formatCpuLoad(1));
    }

    @Test
    void formatsMemoryUsageInMegabytes() {
        assertEquals("Memory 128 MB / 512 MB",
                SystemStatusBar.formatMemoryUsage(128L * 1024L * 1024L, 512L * 1024L * 1024L));
    }

    @Test
    void formatsGarbageCollectionCount() {
        assertEquals("GC --", SystemStatusBar.formatGarbageCollectionCount(-1));
        assertEquals("GC 0", SystemStatusBar.formatGarbageCollectionCount(0));
        assertEquals("GC 42", SystemStatusBar.formatGarbageCollectionCount(42));
    }
}
