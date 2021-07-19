module fxgame {
    requires java.base;
    requires java.logging;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires com.almasb.fxgl.all;

    exports io.fxgame.game2048;
}
