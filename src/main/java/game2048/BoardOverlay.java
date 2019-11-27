package game2048;

import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * BoardOverlay
 */
public class BoardOverlay extends HBox {

    private final VBox txtOverlay = new VBox(10);
    private final Label lOvrText = new Label();
    private final Label lOvrSubText = new Label();
    private final HBox buttonsOverlay = new HBox();
    private Timeline timer;
    private BooleanProperty layerOnProperty;
    private Board board;
    private int gridWidth;

    protected BoardOverlay(Timeline timer, BooleanProperty layerOnProperty, Board board) {
        this.timer = timer;
        this.layerOnProperty = layerOnProperty;
        this.board = board;

        gridWidth = board.getGridWidth();

        setAlignment(Pos.CENTER);
        setMinSize(gridWidth, gridWidth);
        setTranslateY(Constants.TOP_HEIGHT + Constants.GAP_HEIGHT);

        getChildren().setAll(txtOverlay);
        txtOverlay.setAlignment(Pos.CENTER);

        buttonsOverlay.setAlignment(Pos.CENTER);
        buttonsOverlay.setTranslateY(Constants.TOP_HEIGHT + Constants.GAP_HEIGHT + gridWidth / 2);
        buttonsOverlay.setMinSize(gridWidth, gridWidth / 2);
        buttonsOverlay.setSpacing(10);
    }

    protected ChangeListener<Boolean> createChangeListener(String message, String warning, Button btn1, Button btn2, String style1, String style2) {
        return (ov, oldValue, newValue) -> {
            if (!newValue) {
                return;
            }

            timer.stop();

            getStyleClass().setAll("game-overlay", style1);
            lOvrText.setText(message);
            lOvrText.getStyleClass().setAll("game-label", style2);
            lOvrSubText.setText(warning);
            lOvrSubText.getStyleClass().setAll("game-label", "game-lblWarning");
            txtOverlay.getChildren().setAll(lOvrText, lOvrSubText);
            buttonsOverlay.getChildren().setAll(btn1);

            if (btn2 != null) {
                buttonsOverlay.getChildren().add(btn2);
            }

            if (!layerOnProperty.get()) {
                board.getChildren().addAll(BoardOverlay.this, buttonsOverlay);
                layerOnProperty.set(true);

                var defaultBtn = btn2 == null ? btn1 : btn2;
                defaultBtn.requestFocus();
                defaultBtn.setDefaultButton(true);
            }
        };
    }

	public void displayAbout(Button keepGoing) {
        timer.stop();
        getStyleClass().setAll("game-overlay", "game-overlay-quit");

        TextFlow flow = new TextFlow();
        flow.setTextAlignment(TextAlignment.CENTER);
        flow.setPadding(new Insets(8, 0, 0, 0));
        flow.setMinSize(gridWidth, gridWidth);
        flow.setPrefSize(gridWidth, gridWidth);
        flow.setMaxSize(gridWidth, gridWidth);
        flow.setPrefSize(BASELINE_OFFSET_SAME_AS_HEIGHT, BASELINE_OFFSET_SAME_AS_HEIGHT);

        var header = new Text("About \n");
        header.getStyleClass().setAll("game-label", "game-lblAbout");

        var subheader = new Text("2048, JavaFX version\n");
        subheader.getStyleClass().setAll("game-label", "game-lblAboutSub");

        var sectionPoweredBy = new Text("Powered by ");
        sectionPoweredBy.getStyleClass().setAll("game-label", "game-lblAboutSub");

        var openJFXLink = new Hyperlink();
        openJFXLink.setText("OpenJFX");
        openJFXLink.setOnAction(e -> Game2048.urlOpener().open("https://openjfx.io/"));
        openJFXLink.getStyleClass().setAll("game-label", "game-lblAboutSub2");

        var sectionAuthors = new Text(".\n\nDeveloped by ");
        sectionAuthors.getStyleClass().setAll("game-label", "game-lblAboutSub");

        var jperedaLink = new Hyperlink();
        jperedaLink.setText("@jperedadnr");
        jperedaLink.setOnAction(e -> Game2048.urlOpener().open("https://twitter.com/jperedadnr"));
        jperedaLink.getStyleClass().setAll("game-label", "game-lblAboutSub2");

        var andJunc = new Text(" & ");
        andJunc.getStyleClass().setAll("game-label", "game-lblAboutSub");

        var brunoLink = new Hyperlink();
        brunoLink.setText("@brunoborges");
        brunoLink.setOnAction(e -> Game2048.urlOpener().open("https://twitter.com/brunoborges"));
        brunoLink.getStyleClass().setAll("game-label", "game-lblAboutSub2");

        var versionCopyright = new Text("\nv" + Constants.VERSION + " - 2019\n");
        versionCopyright.getStyleClass().setAll("game-label", "game-lblAboutSub");

        var linkForkSource = new Hyperlink();
        linkForkSource.setText("\nFork on GitHub\n");
        linkForkSource.setOnAction(e -> Game2048.urlOpener().open("https://github.com/brunoborges/fx2048"));
        linkForkSource.getStyleClass().setAll("game-label", "game-lblAboutSub2");

        flow.getChildren().setAll(header, subheader, sectionPoweredBy, openJFXLink, sectionAuthors, jperedaLink,
                andJunc, brunoLink, versionCopyright, linkForkSource);
        txtOverlay.getChildren().setAll(flow);
        buttonsOverlay.getChildren().setAll(keepGoing);
        board.getChildren().removeAll(this, buttonsOverlay);
        board.getChildren().addAll(this, buttonsOverlay);
        layerOnProperty.set(true);
	}

}
