package game2048;

import static game2048.GameManager.CELL_SIZE;
import static game2048.GameManager.DEFAULT_GRID_SIZE;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author jpereda
 */
public class Board extends Group {
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    // grid_width=4*cell_size + 2*cell_stroke/2d (14px css)+2*grid_stroke/2d (2 px css)
    private static final int GRID_WIDTH = CELL_SIZE * DEFAULT_GRID_SIZE + BORDER_WIDTH * 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;

    private final int gridSize;
    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBestProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final BooleanProperty gameWonProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePauseProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOnProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final BooleanProperty clearGame = new SimpleBooleanProperty(false);

    private LocalTime time;
    private Timeline timer;
    private final StringProperty clock = new SimpleStringProperty("00:00:00");
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    // User Interface controls
    private final VBox vGame = new VBox(0);
    private final Group gridGroup = new Group();

    private final HBox hTop = new HBox(0);
    private final VBox vScore = new VBox(-5);
    private final Label lblScore = new Label("0");
    private final Label lblBest = new Label("0");
    private final Label lblPoints = new Label();
    private final HBox hOvrLabel = new HBox();
    private final HBox hOvrButton = new HBox();
    private final Label lblTime=new Label();   
    
    public Board(int gridsize){
        this.gridSize=gridsize;
    
        createScore();
        createGrid();
        
        initGameProperties();
    }
    
    private void createScore() {
        Label lblTitle = new Label("2048");
        lblTitle.getStyleClass().add("title");
        Label lblSubtitle = new Label("FX");
        lblSubtitle.getStyleClass().add("subtitle");
        HBox hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);
        
        VBox vScores = new VBox();
        HBox hScores=new HBox(5);
        
        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("vbox");
        Label lblTit = new Label("SCORE");
        lblTit.getStyleClass().add("titScore");
        lblScore.getStyleClass().add("score");
        lblScore.textProperty().bind(gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        VBox vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("vbox");
        Label lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().add("titScore");
        lblBest.getStyleClass().add("score");
        lblBest.textProperty().bind(gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore,vRecord);
        VBox vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores,vFill);
                
        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill,vScores);
        hTop.setMinSize(GRID_WIDTH, TOP_HEIGHT);
        hTop.setPrefSize(GRID_WIDTH, TOP_HEIGHT);
        hTop.setMaxSize(GRID_WIDTH, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        HBox hTime=new HBox();
        hTime.setMinSize(GRID_WIDTH, GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().add("time");
        lblTime.textProperty().bind(clock);
        timer=new Timeline(new KeyFrame(Duration.ZERO, e->{
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt));
        }),new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        hTime.getChildren().add(lblTime);
        
        vGame.getChildren().add(hTime);
        getChildren().add(vGame);
        
        lblPoints.getStyleClass().add("points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }
    
    private void createGrid() {
        final double arcSize = CELL_SIZE / 6d;

        IntStream.range(0, gridSize)
            .mapToObj(i -> IntStream.range(0, gridSize)
                .mapToObj(j -> {
                    Rectangle rect2 = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    rect2.setArcHeight(arcSize);
                    rect2.setArcWidth(arcSize);
                    rect2.getStyleClass().add("grid-cell");
                    return rect2;
                }))
            .flatMap(s -> s)
            .forEach(gridGroup.getChildren()::add);

        gridGroup.getStyleClass().add("grid");
        gridGroup.setManaged(false);
        gridGroup.setLayoutX(BORDER_WIDTH);
        gridGroup.setLayoutY(BORDER_WIDTH);

        HBox hBottom = new HBox();
        hBottom.getStyleClass().add("backGrid");
        hBottom.setMinSize(GRID_WIDTH, GRID_WIDTH);
        hBottom.setPrefSize(GRID_WIDTH, GRID_WIDTH);
        hBottom.setMaxSize(GRID_WIDTH, GRID_WIDTH);
        
        // Clip hBottom to keep the dropshadow effects within the hBottom
        Rectangle rect = new Rectangle(GRID_WIDTH, GRID_WIDTH);
        hBottom.setClip(rect);
        hBottom.getChildren().add(gridGroup);
        
        vGame.getChildren().add(hBottom);
    }

    private void initGameProperties() {
        gameOverProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("over");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblOver = new Label("Game over!");
                lblOver.getStyleClass().add("lblOver");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblOver);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                Button bTry = new Button("Try again");
                bTry.getStyleClass().setAll("try");

                bTry.setOnTouchPressed(e -> doResetGame());
                bTry.setOnAction(e -> doResetGame());

                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });

        gameWonProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                Timeline timerPause=new Timeline(new KeyFrame(Duration.seconds(1), e->
                    time=time.plusNanos(1_000_000_000)));
                timerPause.setCycleCount(Animation.INDEFINITE);
                timerPause.play();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("won");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblWin = new Label("You win!");
                lblWin.getStyleClass().add("lblWon");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblWin);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                hOvrButton.setSpacing(10);
                Button bContinue = new Button("Keep going");
                bContinue.getStyleClass().add("try");
                bContinue.setOnTouchPressed(e -> {
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                bContinue.setOnAction(e -> {
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                Button bTry = new Button("Try again");
                bTry.getStyleClass().add("try");
                bTry.setOnTouchPressed(e -> {
                    timerPause.stop();
                    doResetGame();
                });
                bTry.setOnAction(e -> {
                    timerPause.stop();
                    doResetGame();
                });
                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bContinue, bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });
        
        gamePauseProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                Timeline timerPause=new Timeline(new KeyFrame(Duration.seconds(1), e->
                    time=time.plusNanos(1_000_000_000)));
                timerPause.setCycleCount(Animation.INDEFINITE);
                timerPause.play();
                layerOnProperty.set(true);
                hOvrLabel.getStyleClass().setAll("pause");
                hOvrLabel.setMinSize(GRID_WIDTH, GRID_WIDTH);
                Label lblWin = new Label("Game Paused");
                lblWin.getStyleClass().add("lblPause");
                hOvrLabel.setAlignment(Pos.CENTER);
                hOvrLabel.getChildren().setAll(lblWin);
                hOvrLabel.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
                this.getChildren().add(hOvrLabel);

                hOvrButton.setMinSize(GRID_WIDTH, GRID_WIDTH / 2);
                hOvrButton.setSpacing(10);
                Button bContinue = new Button("Keep going");
                bContinue.getStyleClass().add("try");
                bContinue.setOnAction(e -> {
                    gamePauseProperty.set(false);
                    timerPause.stop();
                    timer.play();
                    layerOnProperty.set(false);
                    getChildren().removeAll(hOvrLabel, hOvrButton);
                });
                Button bTry = new Button("Try again");
                bTry.getStyleClass().add("try");
                bTry.setOnTouchPressed(e -> {
                    timerPause.stop();
                    doResetGame();
                });
                bTry.setOnAction(e -> {
                    timerPause.stop();
                    doResetGame();
                });
                hOvrButton.setAlignment(Pos.CENTER);
                hOvrButton.getChildren().setAll(bContinue, bTry);
                hOvrButton.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + GRID_WIDTH / 2);
                this.getChildren().add(hOvrButton);
            }
        });
        restoreRecord();
        gameScoreProperty.addListener((ov,i,i1)->{
            if(i1.intValue()>gameBestProperty.get()){
                gameBestProperty.set(i1.intValue());
            }
        });
        
    }
    
    private void doClearGame() {
        saveRecord();
        gridGroup.getChildren().removeIf(c->c instanceof Tile);
        getChildren().removeAll(hOvrLabel, hOvrButton);
        
        clearGame.set(false);
        resetGame.set(false);
        layerOnProperty.set(false);
        gameScoreProperty.set(0);
        gameWonProperty.set(false);
        gameOverProperty.set(false);
        gamePauseProperty.set(false);
            
        clearGame.set(true);
    }
    
    private void doResetGame() {
        doClearGame();
        resetGame.set(true);
    }
    
    public void animateScore() {
        if(gameMovePoints.get()==0){
            return;
        }
        
        final Timeline timeline = new Timeline();
        lblPoints.setText("+" + gameMovePoints.getValue().toString());
        lblPoints.setOpacity(1);
        double posX=vScore.localToScene(vScore.getWidth()/2d,0).getX();
        lblPoints.setTranslateX(0);
        lblPoints.setTranslateX(lblPoints.sceneToLocal(posX, 0).getX()-lblPoints.getWidth()/2d);
        lblPoints.setLayoutY(20);
        final KeyValue kvO = new KeyValue(lblPoints.opacityProperty(), 0);
        final KeyValue kvY = new KeyValue(lblPoints.layoutYProperty(), 100);

        Duration animationDuration = Duration.millis(600);
        final KeyFrame kfO = new KeyFrame(animationDuration, kvO);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kfO);
        timeline.getKeyFrames().add(kfY);

        timeline.play();
    }
    
    public Group getGridGroup() {
        return gridGroup;
    }
    
    public void startGame(){
        restoreRecord();

        time=LocalTime.now();
        timer.playFromStart();
    }
    
    public void setPoints(int points){
        gameMovePoints.set(points);
    }
   
    public int getPoints() {
        return gameMovePoints.get();
    }
    
    public void addPoints(int points){
        gameMovePoints.set(gameMovePoints.get() + points);
        gameScoreProperty.set(gameScoreProperty.get() + points);
    }
    
    public void setGameOver(boolean gameOver){
        gameOverProperty.set(gameOver);
    }
    
    public void setGameWin(boolean won){
        gameWonProperty.set(won);
    }
    public void pauseGame(){
        if(!gamePauseProperty.get()){
            gamePauseProperty.set(true);
        }
    }
    
    public boolean isLayerOn(){
        return layerOnProperty.get();
    }
    
    public BooleanProperty resetGameProperty() {
        return resetGame;
    }
    
    public BooleanProperty clearGameProperty() {
        return clearGame;
    }
    
    public void saveSession(Map<Location, Tile> gameGrid) {
        SessionManager sessionManager = new SessionManager(gridSize);
        sessionManager.saveSession(gameGrid, gameScoreProperty.getValue(), LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
    }
    
    public void restoreSession(Map<Location, Tile> gameGrid) {
        SessionManager sessionManager = new SessionManager(gridSize);

        doClearGame();
        timer.stop();
        StringProperty sTime=new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime);
        if (score >= 0) {
            gameScoreProperty.set(score);
            if(!sTime.get().isEmpty()){
                time = LocalTime.now().minusNanos(new Long(sTime.get()));
            }
            timer.play();
        } else {
            // not session found, restart again
            doResetGame();
        }
    }
    
    public void saveRecord() {
        RecordManager recordManager = new RecordManager(gridSize);
        recordManager.saveRecord(gameScoreProperty.getValue());
    }
    
    private void restoreRecord() {
        RecordManager recordManager = new RecordManager(gridSize);
        gameBestProperty.set(recordManager.restoreRecord());
    }
    
}
