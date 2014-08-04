package game2048;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author jpereda
 */
public class Board extends Group {
    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;

    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBestProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final BooleanProperty gameWonProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePauseProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameQuitProperty = new SimpleBooleanProperty(false);
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
    
    private final HBox overlay = new HBox();
    private final Label lOvrText= new Label();
    private final HBox buttonsOverlay = new HBox();
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bQuit = new Button("Quit");
        
    private final Label lblTime=new Label();  
    private Timeline timerPause;
    
    private final int gridWidth;
    private final GridOperator gridOperator;
    private final SessionManager sessionManager;

    
    public Board(GridOperator grid){
        this.gridOperator=grid;
        gridWidth = CELL_SIZE * grid.getGridSize() + BORDER_WIDTH * 2;
        sessionManager = new SessionManager(gridOperator);
        
        createScore();
        createGrid();
        
        initGameProperties();
    }
    
    private void createScore() {
        Label lblTitle = new Label("2048");
        lblTitle.getStyleClass().addAll("game-label","game-title");
        Label lblSubtitle = new Label("FX");
        lblSubtitle.getStyleClass().addAll("game-label","game-subtitle");
        HBox hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);
        
        VBox vScores = new VBox();
        HBox hScores=new HBox(5);
        
        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("game-vbox");
        Label lblTit = new Label("SCORE");
        lblTit.getStyleClass().addAll("game-label","game-titScore");
        lblScore.getStyleClass().addAll("game-label","game-score");
        lblScore.textProperty().bind(gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        VBox vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("game-vbox");
        Label lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().addAll("game-label","game-titScore");
        lblBest.getStyleClass().addAll("game-label","game-score");
        lblBest.textProperty().bind(gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore,vRecord);
        VBox vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores,vFill);
                
        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill,vScores);
        hTop.setMinSize(gridWidth, TOP_HEIGHT);
        hTop.setPrefSize(gridWidth, TOP_HEIGHT);
        hTop.setMaxSize(gridWidth, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        HBox hTime=new HBox();
        hTime.setMinSize(gridWidth, GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().addAll("game-label","game-time");
        lblTime.textProperty().bind(clock);
        timer=new Timeline(new KeyFrame(Duration.ZERO, e->{
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt));
        }),new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        hTime.getChildren().add(lblTime);
        
        vGame.getChildren().add(hTime);
        getChildren().add(vGame);
        
        lblPoints.getStyleClass().addAll("game-label","game-points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }
    
    private Rectangle createCell(int i, int j){
        final double arcSize = CELL_SIZE / 6d;
        Rectangle cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        // provide default style in case css are not loaded
        cell.setFill(Color.WHITE);
        cell.setStroke(Color.GREY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        cell.getStyleClass().add("game-grid-cell");
        return cell;
    }
    
    private void createGrid() {
        
        gridOperator.traverseGrid((i,j)->{
            gridGroup.getChildren().add(createCell(i, j));
            return 0;
        });

        gridGroup.getStyleClass().add("game-grid");
        gridGroup.setManaged(false);
        gridGroup.setLayoutX(BORDER_WIDTH);
        gridGroup.setLayoutY(BORDER_WIDTH);

        HBox hBottom = new HBox();
        hBottom.getStyleClass().add("game-backGrid");
        hBottom.setMinSize(gridWidth, gridWidth);
        hBottom.setPrefSize(gridWidth, gridWidth);
        hBottom.setMaxSize(gridWidth, gridWidth);
        
        // Clip hBottom to keep the dropshadow effects within the hBottom
        Rectangle rect = new Rectangle(gridWidth, gridWidth);
        hBottom.setClip(rect);
        hBottom.getChildren().add(gridGroup);
        
        vGame.getChildren().add(hBottom);
    }

    private void tryAgain(){
        timerPause.stop();
        layerOnProperty.set(false);
        doResetGame();
    }
    private void keepGoing(){
        timerPause.stop();
        layerOnProperty.set(false);
        gamePauseProperty.set(false);
        gameQuitProperty.set(false);
        timer.play();
    }
    private void quit() {
        timerPause.stop();
        Platform.exit();
    }
    
    private final ChangeListener<Boolean> wonListener=(observable, oldValue, newValue) -> {
        if (newValue) {
            timer.stop();
            timerPause.play();
            overlay.getStyleClass().setAll("game-overlay","game-overlay-won");
            lOvrText.setText("You win!");
            lOvrText.getStyleClass().setAll("game-label","game-lblWon");
            buttonsOverlay.getChildren().setAll(bContinue, bTry);
            this.getChildren().addAll(overlay,buttonsOverlay);
            layerOnProperty.set(true);
        }
    };
    
    private void initGameProperties() {
        
        overlay.setMinSize(gridWidth, gridWidth);
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);
        
        overlay.getChildren().setAll(lOvrText);
        
        buttonsOverlay.setAlignment(Pos.CENTER);
        buttonsOverlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + gridWidth / 2);
        buttonsOverlay.setMinSize(gridWidth, gridWidth / 2);
        buttonsOverlay.setSpacing(10);
        
        bTry.getStyleClass().add("game-button");
        bTry.setOnTouchPressed(e -> tryAgain());
        bTry.setOnAction(e -> tryAgain());
        bTry.setOnKeyPressed(e->{
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                tryAgain();
            }
        });

        bContinue.getStyleClass().add("game-button");
        bContinue.setOnTouchPressed(e -> keepGoing());
        bContinue.setOnMouseClicked(e -> keepGoing());
        bContinue.setOnKeyPressed(e->{
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                keepGoing();
            }
        });

        bQuit.getStyleClass().add("game-button");
        bQuit.setOnTouchPressed(e -> quit());
        bQuit.setOnMouseClicked(e -> quit());
        bQuit.setOnKeyPressed(e->{
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                quit();
            }
        });
      
        timerPause=new Timeline(new KeyFrame(Duration.seconds(1), 
                e->time=time.plusNanos(1_000_000_000)));
        timerPause.setCycleCount(Animation.INDEFINITE);
        
        gameOverProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                overlay.getStyleClass().setAll("game-overlay","game-overlay-over");
                lOvrText.setText("Game over!");
                lOvrText.getStyleClass().setAll("game-label","game-lblOver");
                buttonsOverlay.getChildren().setAll(bTry);
                this.getChildren().addAll(overlay,buttonsOverlay);
                layerOnProperty.set(true);
            }
        });

        gameWonProperty.addListener(wonListener);
        
        gamePauseProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                timerPause.play();
                overlay.getStyleClass().setAll("game-overlay","game-overlay-pause");
                lOvrText.setText("Game Paused");
                lOvrText.getStyleClass().setAll("game-label","game-lblPause");
                buttonsOverlay.getChildren().setAll(bContinue, bTry);
                this.getChildren().addAll(overlay,buttonsOverlay);
                layerOnProperty.set(true);
            }
        });
        gameQuitProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                timerPause.play();
                overlay.getStyleClass().setAll("game-overlay","game-overlay-quit");
                lOvrText.setText("Quit Game?");
                lOvrText.getStyleClass().setAll("game-label","game-lblQuit");
                buttonsOverlay.getChildren().setAll(bContinue, bQuit);
                this.getChildren().addAll(overlay,buttonsOverlay);
                layerOnProperty.set(true);
            }
        });
        
        restoreRecord();
        
        gameScoreProperty.addListener((ov,i,i1)->{
            if(i1.intValue()>gameBestProperty.get()){
                gameBestProperty.set(i1.intValue());
            }
        });
        
        layerOnProperty.addListener((ov,b,b1)->{
            if(!b1){
                getChildren().removeAll(overlay, buttonsOverlay);
                // Keep the focus on the game when the layer is removed:
                getParent().requestFocus();
            } else if(b1){
                // Set focus on the first button
                buttonsOverlay.getChildren().get(0).requestFocus();
            }
        });
        
    }
    
    private void doClearGame() {
        saveRecord();
        gridGroup.getChildren().removeIf(c->c instanceof Tile);
        getChildren().removeAll(overlay, buttonsOverlay);
        
        clearGame.set(false);
        resetGame.set(false);
        layerOnProperty.set(false);
        gameScoreProperty.set(0);
        gameWonProperty.set(false);
        gameOverProperty.set(false);
        gamePauseProperty.set(false);
        gameQuitProperty.set(false);
        
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
    
    public void addTile(Tile tile){
        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        gridGroup.getChildren().add(tile);
    }
    
    public Tile addRandomTile(Location randomLocation) {
        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);

        gridGroup.getChildren().add(tile);
        
        return tile;
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
        if(!gameWonProperty.get()){
            gameWonProperty.set(won);
        }
    }
    public void pauseGame(){
        if(!gamePauseProperty.get()){
            gamePauseProperty.set(true);
        }
    }
    public void quitGame(){
        if(!gameQuitProperty.get()){
            gameQuitProperty.set(true);
        }
    }
    
    public BooleanProperty isLayerOn(){
        return layerOnProperty;
    }
    
    public BooleanProperty resetGameProperty() {
        return resetGame;
    }
    
    public BooleanProperty clearGameProperty() {
        return clearGame;
    }
    
    public void saveSession(Map<Location, Tile> gameGrid) {
        sessionManager.saveSession(gameGrid, gameScoreProperty.getValue(), LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
    }
    
    public boolean restoreSession(Map<Location, Tile> gameGrid) {
        doClearGame();
        timer.stop();
        StringProperty sTime=new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime);
        if (score >= 0) {
            gameScoreProperty.set(score);
            // check tiles>=2048
            gameWonProperty.set(false);
            gameGrid.forEach((l,t)->{
               if(t!=null && t.getValue()>=GameManager.FINAL_VALUE_TO_WIN){
                   gameWonProperty.removeListener(wonListener);
                   gameWonProperty.set(true);
                   gameWonProperty.addListener(wonListener);
               }
            });
            if(!sTime.get().isEmpty()){
                time = LocalTime.now().minusNanos(new Long(sTime.get()));
            }
            timer.play();
            return true;
        } 
        // not session found, restart again
        doResetGame();
        return false;
    }
    
    public void saveRecord() {
        RecordManager recordManager = new RecordManager(gridOperator.getGridSize());
        recordManager.saveRecord(gameScoreProperty.getValue());
    }
    
    private void restoreRecord() {
        RecordManager recordManager = new RecordManager(gridOperator.getGridSize());
        gameBestProperty.set(recordManager.restoreRecord());
    }
    
}
