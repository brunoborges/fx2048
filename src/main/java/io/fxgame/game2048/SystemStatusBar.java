package io.fxgame.game2048;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import com.sun.management.OperatingSystemMXBean;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

final class SystemStatusBar extends HBox {

    private static final long BYTES_PER_MEBIBYTE = 1024L * 1024L;
    private static final OperatingSystemMXBean OPERATING_SYSTEM =
            ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private static final List<GarbageCollectorMXBean> GARBAGE_COLLECTORS =
            ManagementFactory.getGarbageCollectorMXBeans();

    private final Runtime runtime;
    private final Label cpu = new Label();
    private final Label memory = new Label();
    private final Label gc = new Label();
    private final Timeline updater;

    SystemStatusBar() {
        this(Runtime.getRuntime(), true);
    }

    SystemStatusBar(Runtime runtime, boolean startUpdater) {
        this.runtime = runtime;

        getStyleClass().add("game-status-bar");
        setAlignment(Pos.CENTER_RIGHT);

        cpu.getStyleClass().addAll("game-label", "game-status-text");
        memory.getStyleClass().addAll("game-label", "game-status-text");
        gc.getStyleClass().addAll("game-label", "game-status-text");

        var spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(spacer, cpu, memory, gc);

        updater = new Timeline(new KeyFrame(Duration.seconds(2), _ -> update()));
        updater.setCycleCount(Animation.INDEFINITE);
        update();
        if (startUpdater) {
            updater.playFromStart();
        }
    }

    void stop() {
        updater.stop();
    }

    private void update() {
        cpu.setText(formatCpuLoad(processCpuLoad()));
        memory.setText(formatMemoryUsage(runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory()));
        gc.setText(formatGarbageCollectionCount(garbageCollectionCount()));
    }

    private double processCpuLoad() {
        return OPERATING_SYSTEM == null ? -1 : OPERATING_SYSTEM.getProcessCpuLoad();
    }

    private long garbageCollectionCount() {
        long total = 0;
        boolean hasCount = false;
        for (var garbageCollector : GARBAGE_COLLECTORS) {
            var count = garbageCollector.getCollectionCount();
            if (count >= 0) {
                total += count;
                hasCount = true;
            }
        }
        return hasCount ? total : -1;
    }

    static String formatCpuLoad(double cpuLoad) {
        if (Double.isNaN(cpuLoad) || cpuLoad < 0) {
            return "CPU --";
        }
        return "CPU %d%%".formatted(Math.round(cpuLoad * 100));
    }

    static String formatMemoryUsage(long usedBytes, long maxBytes) {
        return "Memory %d MB / %d MB".formatted(toMebibytes(usedBytes), toMebibytes(maxBytes));
    }

    static String formatGarbageCollectionCount(long collectionCount) {
        if (collectionCount < 0) {
            return "GC --";
        }
        return "GC %,d".formatted(collectionCount);
    }

    private static long toMebibytes(long bytes) {
        return Math.max(0, Math.round((double) bytes / BYTES_PER_MEBIBYTE));
    }
}
