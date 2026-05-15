package io.github.brunoborges.fx2048.ui;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

final class BestScoresContent extends VBox {

    BestScoresContent(int gridDimension, List<RecordManager.BestScore> bestScores) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 0, 0, 0));
        setMinSize(gridDimension, gridDimension);
        setPrefSize(gridDimension, gridDimension);
        setMaxSize(gridDimension, gridDimension);
        getStyleClass().add("game-best-scores-panel");

        var title = new Label("Best Scores");
        title.getStyleClass().setAll("game-label", "game-best-scores-title");

        var scoresList = new GridPane();
        scoresList.setAlignment(Pos.CENTER);
        scoresList.getStyleClass().add("game-best-scores-list");
        scoresList.getColumnConstraints().setAll(labelColumn(), valueColumn());

        var rowIndex = 0;
        for (var bestScore : bestScores) {
            var gridLabel = new Label(bestScore.gridLabel());
            gridLabel.getStyleClass().setAll("game-label", "game-best-scores-label");
            scoresList.add(gridLabel, 0, rowIndex);

            var scoreLabel = new Label(bestScore.scoreLabel());
            scoreLabel.getStyleClass().setAll("game-label", "game-best-scores-value");
            scoresList.add(scoreLabel, 1, rowIndex++);
        }

        getChildren().setAll(title, scoresList);
    }

    private ColumnConstraints labelColumn() {
        var column = new ColumnConstraints();
        column.setHalignment(HPos.RIGHT);
        return column;
    }

    private ColumnConstraints valueColumn() {
        var column = new ColumnConstraints();
        column.setHalignment(HPos.LEFT);
        return column;
    }
}
