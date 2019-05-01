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

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.SettingsPane;
import com.gluonhq.charm.glisten.control.settings.DefaultOption;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jpereda.game2048.Game2048;
import org.jpereda.game2048.model.GameMode;
import org.jpereda.game2048.model.GameModel;
import org.jpereda.game2048.service.Cloud;

import javax.inject.Inject;
import java.util.Optional;

/**
 *
 * @author JosePereda
 */
public class SettingsPresenter extends GluonPresenter<Game2048> {
    
    @Inject
    private Cloud cloud;

    @Inject
    private GameModel gameModel;

    @FXML 
    private View settings;

    @FXML 
    private SettingsPane settingsPane;

    private final ObjectProperty<GameMode> gameMode = new SimpleObjectProperty<>(GameMode.EASY);

    public void initialize() {
        settings.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> getApp().goHome()));
                appBar.setTitleText("Game Settings");
                appBar.getActionItems().add(MaterialDesignIcon.CLOSE.button(e -> getApp().goHome()));
                gameMode.set(gameModel.getGameMode());
            }
        });

        gameMode.addListener((obs, ov, nv) -> {
            if (nv != gameModel.getGameMode()) {
                showAlertDialog(nv);
            }
        });
        final DefaultOption<ObjectProperty<GameMode>> gameModeOption = new DefaultOption<>(MaterialDesignIcon.APPS.graphic(),
                "Game Mode", "Choose the Game Mode", "Mode", gameMode, true);
        gameModeOption.setExtendedDescription("Select between: \n\n"
                + "\u2022 Easy: Save and restore are allowed at any time\n\n\n"
                + "\u2022 Advanced: Save allowed only after a 2048 tile, you can restore at any time\n\n\n"
                + "\u2022 Expert: Save and restore are not allowed.");
        gameModeOption.setStringConverter(new StringConverter() {
            @Override
            public String toString(Object object) {
                return ((GameMode) object).getText();
            }

            @Override
            public Object fromString(String string) {
                return GameMode.valueOf(string);
            }
        });
        
        final DefaultOption<BooleanProperty> vibrateOption = new DefaultOption<>(MaterialDesignIcon.VIBRATION.graphic(),
                "Vibrate", "Vibrate every 2048 tile", "Options", gameModel.vibrateModeOnProperty(), true);
        
        settingsPane.getOptions().addAll(gameModeOption, vibrateOption);
        
    }

    private void showAlertDialog(GameMode mode) {
        HBox title = new HBox(10);
        title.setAlignment(Pos.CENTER_LEFT);
        title.getChildren().add(new ImageView());
        title.getChildren().add(new Label("Game will be restarted"));
        Dialog<ButtonType> dialog = new Dialog();
        dialog.setContent(new Label("The game will be restarted and non saved data will be lost. \nDo you want to continue?"));
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
                gameModel.setGameMode(mode);
            } else {
                gameMode.set(gameModel.getGameMode());
            }
            getApp().switchToPreviousView();
        });
    }
}
