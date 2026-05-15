package io.github.brunoborges.fx2048.ui;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

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
                styledText("JavaFX game - Desktop version\n\n", "game-lblAboutSub"),
                styledText("Creator and maintainer: ", "game-lblAboutSub"),
                aboutLink("@brunoborges", "https://github.com/brunoborges"),
                styledText("\nContributor: ", "game-lblAboutSub"),
                aboutLink("JPereda", "https://github.com/JPeredaDnr"),
                styledText("\nProject site: ", "game-lblAboutSub"),
                aboutLink("brunoborges.github.io/fx2048", "https://brunoborges.github.io/fx2048/"),
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
