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

package org.jpereda.game2048.views;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableList;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jpereda.game2048.Game2048;
import org.jpereda.game2048.model.GameModel;
import org.jpereda.game2048.model.Score;
import org.jpereda.game2048.service.Cloud;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author jpereda
 */
public class BoardPresenter extends GluonPresenter<Game2048> {

    private final static int LIST_SIZE = 10;

    private final static Comparator<Score> COMPARATOR = (o1, o2) -> {
        if (o2.getScore() == o1.getScore()) {
            // old equal score first
            return Long.compare(o1.getTimeStamp(), o2.getTimeStamp());
        }
        // high score first
        return Integer.compare(o2.getScore(), o1.getScore());
    };

    @FXML View view;  
    
    @FXML private Label labelMode;
    
    @FXML private ListView<Score> scoreBoardDay;
    
    @Inject
    private Cloud cloud;
    @Inject
    private GameModel gameModel;

    private  GluonObservableList<Score> scores;

    public void initialize() {
        scoreBoardDay.setPlaceholder(new Label("No High Scores Yet"));
        scoreBoardDay.setCellFactory(data -> new ScoreListCell());
        labelMode.setText(gameModel.getGameMode().getText());
        
        final Label labelLegend = new Label("All-Time Best Scores");
        labelLegend.setStyle("-fx-font-size: 0.9em; -fx-text-fill: #f2b179;");

        view.showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> returnGame()));
                appBar.setTitleText("Leaderboard");
                appBar.getActionItems().add(labelLegend);

                if (! cloud.isAuthenticated()) {
                    AppViewManager.GAME_VIEW.switchView().ifPresent(p -> {
                        PauseTransition pause = new PauseTransition(Duration.millis(300));
                        pause.setOnFinished(f -> ((GamePresenter) p).board());
                        pause.play();
                    });
                } else {
                    updateList();
                }
            }
        });
        view.setOnHiding(e -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setProgress(1);
            appBar.setProgressBarVisible(false);
        });

        gameModel.gameModeProperty().addListener((obs, m, m1) -> {
            labelMode.setText(gameModel.getGameMode().getText());
            if (cloud.getAuthenticatedUser() != null) {
                updateList();
            }
        });

    }

    private void updateList() {
        AppBar appBar = getApp().getAppBar();
        appBar.setProgress(-1);
        appBar.setProgressBarVisible(true);
        scores = cloud.updateLeaderboard();
        scores.addListener((ListChangeListener.Change<? extends Score> c) -> {
            while (c.next()) {
                if (c.wasAdded() && ! c.getAddedSubList().isEmpty()) {
                    Platform.runLater(() -> scoreBoardDay.getSelectionModel().select(c.getAddedSubList().get(0)));
                }
            }
        });
        if (scores.isInitialized()) {
            scoreBoardDay.setItems(new SortedList<>(scores, COMPARATOR));
            appBar.setProgress(1);
            appBar.setProgressBarVisible(false);
        } else {
            scores.initializedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (scores.isInitialized()) {
                        scoreBoardDay.setItems(new SortedList<>(scores, COMPARATOR));
                        appBar.setProgress(1);
                        appBar.setProgressBarVisible(false);
                        scores.initializedProperty().removeListener(this);
                    }
                }
            });
        }
    }

    @FXML
    private void returnGame() {
        getApp().goHome();
    }
    
    void addScore(int points) {
        Score score = new Score(cloud.getAuthenticatedUser().getName(),
                cloud.getAuthenticatedUser().getPicture(),
                System.currentTimeMillis(),
                gameModel.getGame().getGameID(),
                points);
        if (scores.isInitialized()) {
            addScore(score);
        } else {
            scores.setOnSucceeded(e -> addScore(score));
        }
    }

    private void addScore(Score score) {
        // check same game from same user and update score
        boolean found = false;
        ObservableList<Score> leaderBoard = scores;
        for (Score s : leaderBoard) {
            if ((s.getUserName() != null && s.getUserName().equals(score.getUserName())) &&
                    (s.getUserPic() == null || (s.getUserPic() != null && s.getUserPic().equals(score.getUserPic()))) &&
                    s.getGameID() == score.getGameID()) {
                // if better score, replace with the new one
                if (s.getScore() < score.getScore()) {
                    try {
                        System.out.println("Found: Remove old score " + s.getScore());
                        leaderBoard.remove(s);
                    } catch (Exception e) {
                        System.out.println("ERROR removing score " + e);
                    }
                    System.out.println("Found: Add new score " + score.getScore());
                    leaderBoard.add(score);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            if (leaderBoard.size() < LIST_SIZE) {
                System.out.println("No found: Add new score " + score.getScore());
                leaderBoard.add(score);
            } else {
                Score lastScore = Collections.max(leaderBoard, COMPARATOR);
                if (leaderBoard.size() == LIST_SIZE) {
                    if (score.getScore() > lastScore.getScore()) {
                        try{
                            System.out.println("No found: remove last score " + lastScore.getScore());
                            leaderBoard.remove(lastScore);
                        } catch (Exception e) {
                            System.out.println("ERROR removing score: " + e);
                        }
                        System.out.println("No found: Add new score " + score.getScore());
                        leaderBoard.add(score);
                    }
                } else {
                    System.out.println("No found: remove last score " + lastScore.getScore());
                    leaderBoard.remove(lastScore);
                }
            }
        }
        System.out.println("score: done");
    }

    private class ScoreListCell extends ListCell<Score> {

        private final SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy - HH:mm z"); 
        private final int[] swatchs = new int[]{900, 800, 700, 600, 500, 400, 300, 200, 100, 50};

        private final ListTile listTile = new ListTile();
        {
            getStyleClass().add("board");
        }
        
        private double tam = 32, spacing = 10;
        
        @Override
        protected void updateItem(Score item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                
                int pos=Math.min(getIndex(),LIST_SIZE-1);                    
                Text text = new Text(""+(pos+1));
                text.setStyle("-fx-font-weight: bold;"
                            + "-fx-fill: derive(-primary-swatch-" + swatchs[pos] + ",-30%);");
                
                Image img = Score.getImage(item.getUserPic());
                ImageView imageView = new ImageView(img);
                Services.get(DisplayService.class)
                    .ifPresent(d -> { 
                        if (d.isTablet()) {
                            tam = 42;
                            spacing = 15;
                        }
                    });
                
                imageView.setFitHeight(tam);
                imageView.setFitWidth(tam);
                
                HBox hbox = new HBox(spacing, text,imageView);
                hbox.setAlignment(Pos.CENTER_RIGHT);
                listTile.setPrimaryGraphic(hbox);
                
                listTile.textProperty().setAll(item.getUserName(), 
                                               sdf.format(new Date(item.getTimeStamp())));
                
                Label text1 = new Label(""+item.getScore());
                text1.setStyle("-fx-text-fill: derive(-primary-swatch-" + swatchs[pos] + ",-30%);");
                listTile.setSecondaryGraphic(text1);
                setPadding(new Insets(0));
                setText(null);
                setGraphic(listTile);
            } else {
                setText(null);
                setGraphic(null);
            }
        }
        
    }
}
