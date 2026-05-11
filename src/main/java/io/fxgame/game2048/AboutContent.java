package io.fxgame.game2048;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

final class AboutContent extends TextFlow {

    AboutContent(int gridDimension) {
        setTextAlignment(TextAlignment.CENTER);
        setPadding(new Insets(10, 0, 0, 0));
        setMinSize(gridDimension, gridDimension);
        setPrefSize(gridDimension, gridDimension);
        setMaxSize(gridDimension, gridDimension);
        setPrefSize(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        getChildren().setAll(
                styledText("2048", "game-lblAbout"),
                styledText("FX", "game-lblAbout2"),
                styledText(" Game\n", "game-lblAbout"),
                styledText("JavaFX game - Desktop version\n\n", "game-lblAboutSub"),
                styledText("Powered by ", "game-lblAboutSub"),
                aboutLink("OpenJFX", "https://openjfx.io/"),
                styledText(" Project \n\n", "game-lblAboutSub"),
                styledText("© ", "game-lblAboutSub"),
                aboutLink("@JPeredaDnr", "https://twitter.com/JPeredaDnr"),
                styledText(" & ", "game-lblAboutSub"),
                aboutLink("@brunoborges", "https://twitter.com/brunoborges"),
                styledText("\n\n", "game-lblAboutSub"),
                styledText("Version " + Game2048.VERSION + "\n\n", "game-lblAboutSub"));
    }

    private Text styledText(String text, String styleClass) {
        var styledText = new Text(text);
        styledText.getStyleClass().setAll("game-label", styleClass);
        return styledText;
    }

    private Hyperlink aboutLink(String text, String url) {
        var link = new Hyperlink(text);
        link.setOnAction(_ -> Game2048.urlOpener().open(url));
        link.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        return link;
    }
}
