/*
 * Copyright (C) 2013-2019 2048FX
 * Jose Pereda, Bruno Borges & Jens Deters
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpereda.game2048;

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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author jpereda
 */
public class Board extends Region {
    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;

    private final IntegerProperty gameScore = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBest = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final IntegerProperty gameID = new SimpleIntegerProperty(0);
    private final BooleanProperty gameWon = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverAndShare = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePause = new SimpleBooleanProperty(false);
    private final BooleanProperty gameTryAgain = new SimpleBooleanProperty(false);
    private final BooleanProperty gameShare = new SimpleBooleanProperty(false);
    private final BooleanProperty gameSave = new SimpleBooleanProperty(false);
    private final BooleanProperty gameRestore = new SimpleBooleanProperty(false);
    private final BooleanProperty gameQuit = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOn = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final BooleanProperty clearGame = new SimpleBooleanProperty(false);
    private final BooleanProperty restoreGame = new SimpleBooleanProperty(false);
    private final BooleanProperty saveGame = new SimpleBooleanProperty(false);

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
    private final VBox txtOverlay = new VBox(10);
    private final Label lOvrText = new Label();
    private final Label lOvrSubText = new Label();
    private final HBox buttonsOverlay = new HBox();
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bContinueNo = new Button("No, keep going");
    private final Button bSave = new Button("Save");
    private final Button bRestore = new Button("Restore");
    private final Button bQuit = new Button("Quit");
    private final Button bShare = new Button("Share");

    private final Label lblMode = new Label();
    private final Label lblTime = new Label();
    private Timeline timerPause;

    private final int gridWidth;
    private final GridOperator gridOperator;
    private final SessionManager sessionManager;

    public Board(GridOperator grid) {

        this.gridOperator = grid;
        gridWidth = CELL_SIZE * grid.getGridSize() + BORDER_WIDTH * 2;
        sessionManager = new SessionManager(gridOperator.getGridSize());

        createScore();
        createGrid();

        initGameProperties();
    }

    private void createScore() {
        Label lblTitle = new Label("2048");
        lblTitle.getStyleClass().addAll("game-label", "game-title");
        Label lblSubtitle = new Label("FX");
        lblSubtitle.getStyleClass().addAll("game-label", "game-subtitle");
        HBox hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);

        VBox vScores = new VBox();
        HBox hScores = new HBox(5);

        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("game-vbox");
        Label lblTit = new Label("SCORE");
        lblTit.getStyleClass().addAll("game-label", "game-titScore");
        lblScore.getStyleClass().addAll("game-label", "game-score");
        lblScore.textProperty().bind(gameScore.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        VBox vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("game-vbox");
        Label lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().addAll("game-label", "game-titScore");
        lblBest.getStyleClass().addAll("game-label", "game-score");
        lblBest.textProperty().bind(gameBest.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore, vRecord);
        VBox vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores, vFill);

        hTop.getChildren().addAll(lblTitle, lblSubtitle, hFill, vScores);
        hTop.setMinSize(gridWidth, TOP_HEIGHT);
        hTop.setPrefSize(gridWidth, TOP_HEIGHT);
        hTop.setMaxSize(gridWidth, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        HBox hTime = new HBox();
        hTime.setMinSize(gridWidth, GAP_HEIGHT);
        hTime.setAlignment(Pos.BOTTOM_CENTER);

        lblMode.getStyleClass().addAll("game-label", "game-time");

        lblTime.getStyleClass().addAll("game-label", "game-time");
        lblTime.textProperty().bind(clock);
        HBox hGap = new HBox();
        HBox.setHgrow(hGap, Priority.ALWAYS);
        timer = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt));
        }), new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        hTime.getChildren().addAll(lblMode, hGap, lblTime);

        vGame.getChildren().add(hTime);
        getChildren().add(vGame);

        lblPoints.getStyleClass().addAll("game-label", "game-points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = CELL_SIZE / 6d;
        Rectangle cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        // provide default style in case css are not loaded
        cell.setFill(Color.WHITE);
        cell.setStroke(Color.GREY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        cell.getStyleClass().setAll("game-grid-cell");
        return cell;
    }

    private void createGrid() {

        for (int i = 0; i < gridOperator.getGridSize(); i++) {
            for (int j = 0; j < gridOperator.getGridSize(); j++) {
                gridGroup.getChildren().add(createCell(i, j));
            }
        }

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

    public void tryAgain(boolean ask) {
        if (ask) {
            if (!gameTryAgain.get()) {
                gameTryAgain.set(true);
            }
        } else {
            btnTryAgain();
        }
    }

    private void btnTryAgain() {
        setGameID(Math.max(sessionManager.getGameID(), gameID.get()) + 1);

        timerPause.stop();
        layerOn.set(false);
        doResetGame();
    }

    public void keepGoing() {
        timerPause.stop();
        layerOn.set(false);
        gamePause.set(false);
        gameTryAgain.set(false);
        gameSave.set(false);
        gameRestore.set(false);
        gameQuit.set(false);
        gameShare.set(false);
        timer.play();
    }

    private final Overlay wonListener = new Overlay("You win!", "", bContinue, bTry, "game-overlay-won", "game-lblWon", true);

    public boolean isGameOverAndShare() {
        return gameOverAndShare.get();
    }

    public boolean isGameOver() {
        return gameOver.get();
    }

    public boolean isGameWon() {
        return gameWon.get();
    }

    private class Overlay implements ChangeListener<Boolean> {

        private final Button btn1, btn2;
        private final String message, warning;
        private final String style1, style2;
        private final boolean pause;

        public Overlay(String message, String warning, Button btn1, Button btn2, String style1, String style2, boolean pause) {
            this.message = message;
            this.warning = warning;
            this.btn1 = btn1;
            this.btn2 = btn2;
            this.style1 = style1;
            this.style2 = style2;
            this.pause = pause;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                timer.stop();
                if (pause) {
                    timerPause.play();
                }
                overlay.getStyleClass().setAll("game-overlay", style1);
                lOvrText.setText(message);
                lOvrText.getStyleClass().setAll("game-label", style2);
                lOvrSubText.setText(warning);
                lOvrSubText.getStyleClass().setAll("game-label", "game-lblWarning");
                txtOverlay.getChildren().setAll(lOvrText, lOvrSubText);
                buttonsOverlay.getChildren().setAll(btn1);
                if (btn2 != null) {
                    buttonsOverlay.getChildren().add(btn2);
                }
                if (!layerOn.get()) {
                    Board.this.getChildren().addAll(overlay, buttonsOverlay);
                    layerOn.set(true);
                }
            }
        }
    }

    private void initGameProperties() {

        overlay.setMinSize(gridWidth, gridWidth);
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);

        overlay.getChildren().setAll(txtOverlay);
        txtOverlay.setAlignment(Pos.CENTER);

        buttonsOverlay.setAlignment(Pos.CENTER);
        buttonsOverlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + gridWidth / 2);
        buttonsOverlay.setMinSize(gridWidth, gridWidth / 2);
        buttonsOverlay.setSpacing(10);

        bTry.getStyleClass().add("game-button");
        bTry.setOnAction(e -> btnTryAgain());
        bTry.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                btnTryAgain();
            }
        });

        bShare.getStyleClass().add("game-button");
        bShare.setOnAction(e -> {
            gameShare.set(true);
            gameShare.set(false);
        });
        bShare.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                gameShare.set(true);
                gameShare.set(false);
            }
        });

        bContinue.getStyleClass().add("game-button");
        bContinue.setOnMouseClicked(e -> keepGoing());
        bContinue.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                keepGoing();
            }
        });
        bContinueNo.getStyleClass().add("game-button");
        bContinueNo.setOnMouseClicked(e -> keepGoing());
        bContinueNo.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                keepGoing();
            }
        });

        bSave.getStyleClass().add("game-button");
        bSave.setOnMouseClicked(e -> saveGame.set(true));
        bSave.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                saveGame.set(true);
            }
        });

        bRestore.getStyleClass().add("game-button");
        bRestore.setOnMouseClicked(e -> restoreGame.set(true));
        bRestore.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
                restoreGame.set(true);
            }
        });

        timerPause = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> time = time.plusNanos(1_000_000_000)));
        timerPause.setCycleCount(Animation.INDEFINITE);

        gameWon.addListener(wonListener);
        gameOverAndShare.addListener(new Overlay("Game over!", "", bShare, bTry, "game-overlay-over", "game-lblOver", false));
        gameOver.addListener(new Overlay("Game over!", "", bTry, null, "game-overlay-over", "game-lblOver", false));
        gamePause.addListener(new Overlay("Game Paused", "", bContinue, null, "game-overlay-pause", "game-lblPause", true));
        gameTryAgain.addListener(new Overlay("Try Again?", "Current game will be deleted", bTry, bContinueNo, "game-overlay-pause", "game-lblPause", true));
        gameSave.addListener(new Overlay("Save?", "Previous saved data will be overwritten", bSave, bContinueNo, "game-overlay-pause", "game-lblPause", true));
        gameRestore.addListener(new Overlay("Restore?", "Current game will be deleted", bRestore, bContinueNo, "game-overlay-pause", "game-lblPause", true));
        gameQuit.addListener(new Overlay("Quit Game?", "Non saved data will be lost", bQuit, bContinueNo, "game-overlay-quit", "game-lblQuit", true));

        restoreRecord();

        gameScore.addListener((ov, i, i1) -> {
            if (i1.intValue() > gameBest.get()) {
                gameBest.set(i1.intValue());
            }
        });

        layerOn.addListener((ov, b, b1) -> {
            if (!b1) {
                getChildren().removeAll(overlay, buttonsOverlay);
                // Keep the focus on the game when the layer is removed:
                getParent().requestFocus();
            } else if (b1) {
                // Set focus on the first button
                buttonsOverlay.getChildren().get(0).requestFocus();
            }
        });

        gameID.addListener(o -> {
            int gameMode = sessionManager.getGameMode();
            lblMode.setText((gameMode == 0 ? "Easy" : gameMode == 1 ? "Advanced" : "Expert") + " Mode, game #" + (gameID.get()+1));
        });
        setGameID(sessionManager.getGameID() + 1);
    }

    private void doClearGame() {
        saveRecord();
        for (ListIterator<Node> iterator = gridGroup.getChildren().listIterator(); iterator.hasNext();) {
            if (iterator.next() instanceof Tile) {
                iterator.remove();
            }
        }
        getChildren().removeAll(overlay, buttonsOverlay);

        clearGame.set(false);
        resetGame.set(false);
        restoreGame.set(false);
        saveGame.set(false);
        layerOn.set(false);
        gameScore.set(0);
        gameWon.set(false);
        gameOverAndShare.set(false);
        gameOver.set(false);
        gamePause.set(false);
        gameTryAgain.set(false);
        gameShare.set(false);
        gameSave.set(false);
        gameRestore.set(false);
        gameQuit.set(false);

        clearGame.set(true);
    }

    private void doResetGame() {
        doClearGame();
        resetGame.set(true);
    }

    public void animateScore() {
        if (gameMovePoints.get() == 0) {
            return;
        }

        final Timeline timeline = new Timeline();
        lblPoints.setText("+" + gameMovePoints.getValue().toString());
        lblPoints.setOpacity(1);
        double posX = vScore.localToScene(vScore.getWidth() / 2d, 0).getX();
        lblPoints.setTranslateX(0);
        lblPoints.setTranslateX(lblPoints.sceneToLocal(posX, 0).getX() - lblPoints.getWidth() / 2d);
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

    public void addTile(Tile tile) {
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
        tile.setScaleX(0.1);
        tile.setScaleY(0.1);

        gridGroup.getChildren().add(tile);

        return tile;
    }

    public Group getGridGroup() {
        return gridGroup;
    }

    public void startGame() {
        restoreRecord();

        time = LocalTime.now();
        timer.playFromStart();
    }

    public void setPoints(int points) {
        gameMovePoints.set(points);
    }

    public int getPoints() {
        return gameMovePoints.get();
    }

    public void addPoints(int points) {
        gameMovePoints.set(gameMovePoints.get() + points);
        gameScore.set(gameScore.get() + points);
    }

    public void setGameOverAndShare(boolean gameOverAndShare) {
        this.gameOverAndShare.set(gameOverAndShare);
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver.set(gameOver);
    }

    public void setGameWin(boolean won) {
        if (!gameWon.get()) {
            gameWon.set(won);
        }
    }

    public void pauseGame() {
        if (!gamePause.get()) {
            gamePause.set(true);
        }
    }

    public void quitGame() {
        if (!gameQuit.get()) {
            gameQuit.set(true);
        }
    }

    public BooleanProperty isLayerOn() {
        return layerOn;
    }

    public BooleanProperty resetGameProperty() {
        return resetGame;
    }

    public BooleanProperty clearGameProperty() {
        return clearGame;
    }

    public BooleanProperty saveGameProperty() {
        return saveGame;
    }

    public BooleanProperty restoreGameProperty() {
        return restoreGame;
    }

    public BooleanProperty gameWonProperty() {
        return gameWon;
    }

    public BooleanProperty gameOverAndShareProperty() {
        return gameOverAndShare;
    }

    public BooleanProperty gameOverProperty() {
        return gameOver;
    }

    public BooleanProperty gameShareProperty() {
        return gameShare;
    }

    public boolean saveSession() {
        if (!gameSave.get()) {
            gameSave.set(true);
        }
        return true;
    }

    /*
     Once we have confirmation
     */
    public void saveSession(Map<Location, Tile> gameGrid) {
        saveGame.set(false);
        sessionManager.saveSession(gameGrid, gameScore.getValue(),
                LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay(),
                getGameID());
        keepGoing();
    }

    public boolean restoreSession() {
        if (!gameRestore.get()) {
            gameRestore.set(true);
        }
        return true;
    }

    /*
     Once we have confirmation
     */
    public boolean restoreSession(Map<Location, Tile> gameGrid) {
        timerPause.stop();
        restoreGame.set(false);
        doClearGame();
        timer.stop();
        StringProperty sTime = new SimpleStringProperty("");
        int score = sessionManager.restoreSession(gameGrid, sTime, gameID);
        if (score >= 0) {
            gameScore.set(score);
            // check tiles>=2048
            gameWon.set(false);
            for (Map.Entry pair : gameGrid.entrySet()) {
                Tile t = (Tile) pair.getValue();
                if (t != null && t.getValue() >= GameManager.FINAL_VALUE_TO_WIN) {
                    gameWon.removeListener(wonListener);
                    gameWon.set(true);
                    gameWon.addListener(wonListener);
                }
            }

            if (!sTime.get().isEmpty()) {
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
        sessionManager.saveRecord(gameBest.getValue());
    }

    private void restoreRecord() {
        gameBest.set(sessionManager.restoreRecord());
    }

    public void externalPause(boolean b, boolean b1) {
        if (!layerOn.get()) {
            if (b1) {
                timerPause.play();
            } else if (b && !b1) {
                timerPause.stop();
            }
        }
    }

    public int getScore() {
        return gameScore.get();
    }

    public void setGameMode(int gameMode) {
        sessionManager.setGameMode(gameMode);
        restoreRecord();
    }

    public final void setGameID(int gameID) {
        this.gameID.set(gameID);
        sessionManager.setGameID(gameID);
    }

    public int getGameID() {
        return gameID.get();
    }

    public IntegerProperty gameIDProperty() {
        return gameID;
    }
    
}
