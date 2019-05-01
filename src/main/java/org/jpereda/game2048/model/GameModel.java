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
package org.jpereda.game2048.model;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.SettingsService;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jpereda.game2048.Direction;
import org.jpereda.game2048.GameManager;

import static org.jpereda.game2048.Game2048.GAME_ID;
import static org.jpereda.game2048.Game2048.GAME_LEGACY;
import static org.jpereda.game2048.Game2048.GAME_MODE;
import static org.jpereda.game2048.Game2048.GAME_VIBRATE_MODE_ON;

/**
 *
 * @author jpereda
 */
public class GameModel {

    private GameManager gameManager;

    private final ObjectProperty<GameMode> gameMode = new SimpleObjectProperty<>(GameMode.EASY);

    private final BooleanProperty vibrateModeOn = new SimpleBooleanProperty();
    private final BooleanProperty saveEnabled = new SimpleBooleanProperty();
    private final BooleanProperty restoreEnabled = new SimpleBooleanProperty();

    public GameModel() {

        Services.get(SettingsService.class)
            .ifPresent(settings -> {
                if (settings.retrieve(GAME_MODE) == null || settings.retrieve(GAME_MODE).isEmpty()) {
                    settings.store(GAME_MODE, Integer.toString(gameMode.get().getMode()));
                }

                if (settings.retrieve(GAME_VIBRATE_MODE_ON) == null || settings.retrieve(GAME_VIBRATE_MODE_ON).isEmpty()) {
                    settings.store(GAME_VIBRATE_MODE_ON, "1");
                }

                if (settings.retrieve(GAME_ID) == null || settings.retrieve(GAME_ID).isEmpty()) {
                    settings.store(GAME_ID, "-1");
                }

                if (settings.retrieve(GAME_LEGACY) == null
                        || settings.retrieve(GAME_LEGACY).isEmpty()
                        || !settings.retrieve(GAME_LEGACY).equals("1")) {

                    GameManager.legacySettings();
                    settings.store(GAME_LEGACY, Integer.toString(1));
                }

                for (GameMode m : GameMode.values()) {
                    if (m.getMode() == Integer.parseInt(settings.retrieve(GAME_MODE))) {
                        gameMode.set(m);
                        break;
                    }
                }
                this.vibrateModeOn.set(settings.retrieve(GAME_VIBRATE_MODE_ON).equals("1"));

                gameManager = new GameManager(4); // default 4x4

                gameModeProperty().addListener((obs, i, i1) -> {
                    gameManager.saveRecord();
                    gameManager.setGameMode(gameMode.get().getMode());
                    gameManager.tryAgain(false);
                });

                switch (getGameMode()) {
                    case EXPERT:
                        saveEnabled.set(false); // Save always disabled
                        restoreEnabled.set(false); // Restore always disabled
                        break;
                    case ADVANCED:
                        // Save only enabled after a new 2048 tile is found
                        saveEnabled.set(gameManager.isTile2048Found());
                        restoreEnabled.set(true); // Restore always enabled
                        break;
                    default:
                        // EASY
                        saveEnabled.set(true); // Save always enabled
                        restoreEnabled.set(true); // Restore always enabled
                        break;
                }

                // Save disabled state, depending on game mode
                BooleanBinding saveBinding = Bindings.createBooleanBinding(() -> {
                    switch (getGameMode()) {
                        case EXPERT:
                            return false;
                        case EASY:
                            return true;
                        default:
                            return gameManager.isTile2048Found();
                    }
                }, gameModeProperty(), gameManager.tile2048FoundProperty());
                saveEnabled.bind(saveBinding);

                // Restore disabled state, depending on game mode
                restoreEnabled.bind(gameModeProperty().isNotEqualTo(GameMode.EXPERT));
            });
    }

    public GameManager getGame() {
        return gameManager;
    }

    public void move(Direction direction) {
        gameManager.move(direction);
    }

    public void setGameMode(GameMode mode) {
        this.gameMode.set(mode);
    }

    public final ObjectProperty<GameMode> gameModeProperty() {
        return gameMode;
    }

    public final GameMode getGameMode() {
        return gameMode.get();
    }

    public void saveSession() {
        gameManager.saveSession();
    }

    public void restoreSession() {
        gameManager.restoreSession();
    }

    public void pauseGame() {
        gameManager.pauseGame();
    }

    public void keepGoing() {
        gameManager.keepGoing();
    }

    public void tryAgain() {
        gameManager.tryAgain();
    }

    public void quitGame() {
        gameManager.quitGame();
    }

    public boolean isGameOverAndShare() {
        return gameManager.isGameOverAndShare();
    }

    public boolean isGameOver() {
        return gameManager.isGameOver();
    }

    public BooleanProperty gameWonProperty() {
        return gameManager.gameWonProperty();
    }

    public BooleanProperty gameOverAndShareProperty() {
        return gameManager.gameOverAndShareProperty();
    }

    public BooleanProperty gameOverProperty() {
        return gameManager.gameOverProperty();
    }

    public BooleanProperty gameShareProperty() {
        return gameManager.gameShareProperty();
    }

    public int getScore() {
        return gameManager.getScore();
    }

    public BooleanProperty vibrateModeOnProperty() {
        return vibrateModeOn;
    }

    public void setVibrateModeOn(boolean vibrateMode) {
        this.vibrateModeOn.set(vibrateMode);
    }

    public boolean isVibrateModeOn() {
        return vibrateModeOn.get();
    }

    public BooleanProperty saveEnabledProperty() {
        return saveEnabled;
    }

    public void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled.set(saveEnabled);
    }

    public boolean isSaveEnabled() {
        return saveEnabled.get();
    }

    public BooleanProperty restoreEnabledProperty() {
        return restoreEnabled;
    }

    public void setRestoreEnabled(boolean restoreEnabled) {
        this.restoreEnabled.set(restoreEnabled);
    }

    public boolean isRestoreEnabled() {
        return restoreEnabled.get();
    }

}
