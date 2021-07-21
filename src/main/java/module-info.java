module fx2048 {
    requires java.base;
    requires java.logging;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.almasb.fxgl.all;

    exports io.fxgame.game2048;
}
