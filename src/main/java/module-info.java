module fxgame {
    requires java.logging;
    requires java.management;
    requires jdk.management;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    exports io.fxgame.game2048;
}
