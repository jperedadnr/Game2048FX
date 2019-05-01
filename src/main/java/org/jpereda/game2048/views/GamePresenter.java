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
import com.gluonhq.charm.down.plugins.VibrationService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.animation.ShakeTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.control.BottomNavigationButton;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableList;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jpereda.game2048.Direction;
import org.jpereda.game2048.Game2048;
import org.jpereda.game2048.model.GameModel;
import org.jpereda.game2048.model.Score;
import org.jpereda.game2048.service.Cloud;

import javax.inject.Inject;
import java.util.Optional;

/**
 *
 * @author jpereda
 */
public class GamePresenter extends GluonPresenter<Game2048> {

    @FXML private View view;

    @FXML private BottomNavigation bottomNav;
    @FXML private BottomNavigationButton saveGame;
    @FXML private BottomNavigationButton restoreGame;
    @FXML private BottomNavigationButton pauseGame;
    @FXML private BottomNavigationButton tryAgain;
    @FXML private BottomNavigationButton share;
    @FXML private BottomNavigationButton board;
    
    @Inject
    private Cloud cloud;

    @Inject
    private GameModel model;

    private final static int MARGIN = 36;

    private final BooleanProperty first = new SimpleBooleanProperty();
    private final BooleanProperty stop = new SimpleBooleanProperty();
    private final BooleanProperty pause = new SimpleBooleanProperty();
    private final IntegerProperty score = new SimpleIntegerProperty();
    
    private boolean lock = false;
    private final ListChangeListener<Score> scoreListChangeListener = (ListChangeListener.Change<? extends Score> c) -> {
        while (c.next()) {
            ShakeTransition shake = new ShakeTransition(board.getGraphic());
            shake.play();
        }
    };

    public void initialize() {
        view.getStyleClass().addAll("game-root");
        view.setCenter(model.getGame());

        Label labelTit = new Label("2048");
        Label labelFX = new Label("FX");
        labelFX.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #f2b179; -fx-translate-y: -8;");
        final HBox hBoxTitle = new HBox(labelTit, labelFX);
        hBoxTitle.setAlignment(Pos.CENTER_LEFT);
        
        model.getGame().overlayVisible().addListener((obs, ov, nv) -> lock = nv);
        stop.addListener((ov, b0, b2) -> {
            if (b2) {
                model.getGame().saveRecord();
            }
        });
        pause.addListener((ov, b0, b2) -> {
            model.getGame().saveRecord();
            model.getGame().externalPause(b0, b2);
        });
        saveGame.disableProperty().bind(model.saveEnabledProperty().not().or(model.gameOverAndShareProperty()));
        restoreGame.disableProperty().bind(model.restoreEnabledProperty().not());

        model.gameShareProperty().addListener((ov, b0, b2) -> {
            if (b2) {
                model.gameOverProperty().set(true);
                share();
            }
        });

        model.getGame().tile2048FoundProperty().addListener((ov, b0, b2) -> {
            if (b2 && model.isVibrateModeOn()) {
                Services.get(VibrationService.class).ifPresent(VibrationService::vibrate);
            }
        });

        view.showingProperty().addListener((obs, b, b1) -> {
            if (b1) {
                if (!first.get()) {
                    ChangeListener<Number> resize = (ov, v, v1) -> gameResize();
                    view.getScene().widthProperty().addListener(resize);
                    view.getScene().heightProperty().addListener(resize);

                    Platform.runLater(this::gameResize);
                    first.set(true);
                }
                
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> showMenu()));
                appBar.setTitle(hBoxTitle);
                    
                addKeyHandler(view);
                addSwipeHandlers(view);
            } else {
                // when homeview is not shown, remove handlers to avoid interacting with it
                removeHandlers(view);
                if (! model.isGameOverAndShare()) {
                    model.pauseGame();
                }
            }
        });

        // sharing scores is only enabled when the game ends
        share.disableProperty().bind(model.gameOverAndShareProperty().not());

        saveGame.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                lock = true;
                model.saveSession();
                saveGame.setSelected(false);
            }
        });
        restoreGame.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                lock = true;
                model.restoreSession();
                restoreGame.setSelected(false);
            }
        });
        pauseGame.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                lock = true;
                model.pauseGame();
                pauseGame.setSelected(false);
            }
        });
        tryAgain.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                lock = true;
                model.tryAgain();
                tryAgain.setSelected(false);
            }
        });
        share.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                share();
                share.setSelected(false);
            }
        });
        board.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                board();
                board.setSelected(false);
            }
        });

        model.gameModeProperty().addListener((obs, ov, nv) -> updateBoard());
        updateBoard();
    }

    private void updateBoard() {
        if (! cloud.isAuthenticated()) {
            return;
        }

        GluonObservableList<Score> scores = cloud.updateLeaderboard();
        if (scores.isInitialized()) {
            scores.addListener(scoreListChangeListener);
        } else {
            scores.initializedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (scores.isInitialized()) {
                        scores.addListener(scoreListChangeListener);
                        scores.initializedProperty().removeListener(this);
                    }
                }
            });
        }
    }

    @FXML
    private void showMenu() {
        if (!model.isGameOverAndShare()) {
            model.pauseGame();
        }
        getApp().getDrawer().open();
    }

    private void share() {
        lock = true;
        if (! cloud.isAuthenticated()) {
            if (! model.isGameOverAndShare()) {
                model.pauseGame();
            }
            showSignInDialog("To share your result");
        } else {
            score.set(model.getScore());
            AppViewManager.BOARD_VIEW.switchView()
                    .ifPresent(boardPresenter -> ((BoardPresenter) boardPresenter).addScore(score.get()));
        }
    }

    void board() {
        lock = true;
        if (! cloud.isAuthenticated()) {
            if (! model.isGameOverAndShare()) {
                model.pauseGame();
            }
            showSignInDialog("To access the leaderboard");
        } else {
            AppViewManager.BOARD_VIEW.switchView();
        }
    }

    public void stopGame() {
        model.getGame().saveRecord();
    }

    private void gameResize() {
        if (view.getScene() == null) {
            return;
        }

        double W = view.getScene().getWidth() - MARGIN;
        double H = view.getScene().getHeight() - getApp().getAppBar().getHeight() 
                - bottomNav.getHeight() - MARGIN;
        model.getGame().setMinSize(W, H);
        model.getGame().setPrefSize(W, H);
        model.getGame().setMaxSize(W, H);
    }

    private void addKeyHandler(Node node) {
        node.getScene().setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode.equals(KeyCode.S)) {
                model.saveSession();
                return;
            }
            if (keyCode.equals(KeyCode.R)) {
                model.restoreSession();
                return;
            }
            if (keyCode.equals(KeyCode.P)) {
                model.pauseGame();
                return;
            }
            if (keyCode.equals(KeyCode.Q)) {
                model.quitGame();
                return;
            }
            if (keyCode.isArrowKey()) {
                Direction direction = Direction.valueFor(keyCode);
                model.move(direction);
            }
        });
    }

    private void addSwipeHandlers(Node node) {
        node.getScene().setOnSwipeUp(e -> move(Direction.UP));
        node.getScene().setOnSwipeRight(e -> move(Direction.RIGHT));
        node.getScene().setOnSwipeLeft(e -> move(Direction.LEFT));
        node.getScene().setOnSwipeDown(e -> move(Direction.DOWN));
    }

    private void removeHandlers(Node node) {
        node.getScene().setOnKeyPressed(null);
        node.getScene().setOnSwipeUp(null);
        node.getScene().setOnSwipeRight(null);
        node.getScene().setOnSwipeLeft(null);
        node.getScene().setOnSwipeDown(null);
    }

    private void move(Direction direction) {
        if (lock) {
            return;
        }
        model.move(direction);
    }

    private void showSignInDialog(String message) {
        // force login view
        HBox title = new HBox(10);
        title.setAlignment(Pos.CENTER_LEFT);
        title.getChildren().add(new ImageView());
        title.getChildren().add(new Label("Sign in required"));
        Dialog<ButtonType> dialog = new Dialog();
        dialog.setContent(new Label(message + ", you have to sign in\nwith your social network profile. \nDo you want to continue?"));
        dialog.setTitle(title);
        Button yes = new Button("Yes");
        yes.setOnAction(e -> {
            dialog.setResult(ButtonType.YES);
            dialog.hide();
        });
        yes.setDefaultButton(true);
        Button no = new Button("No");
        no.setCancelButton(true);
        no.setOnAction(e -> {
            dialog.setResult(ButtonType.NO);
            dialog.hide();
        });
        dialog.getButtons().addAll(yes, no);
        Platform.runLater(() -> {
            Optional result = dialog.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                cloud.forceLogin();
            }
        });
    }

    public BooleanProperty pauseProperty() {
        return pause;
    }

    public BooleanProperty stopProperty() {
        return stop;
    }

}
