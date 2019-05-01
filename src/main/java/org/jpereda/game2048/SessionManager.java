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

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.SettingsService;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

import static org.jpereda.game2048.Game2048.GAME_ID;
import static org.jpereda.game2048.Game2048.GAME_MODE;

/**
 *
 * @author José Pereda
 */
public class SessionManager {

    private final int gridSize;
    private int gameMode;

    public SessionManager(int gridSize) {
        this.gridSize = gridSize;

        gameMode = Services.get(SettingsService.class)
                .map(settings -> Integer.parseInt(settings.retrieve("game_mode")))
                .orElse(0);
    }

    public void saveSession(Map<Location, Tile> gameGrid, Integer score, Long time, int gameID) {
        int[] grid = new int[gridSize * gridSize];
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                Tile t = gameGrid.get(new Location(x, y));
                grid[x + gridSize * y] = t != null ? t.getValue() : 0;
            }
        }
        Services.get(SettingsService.class).ifPresent(settings -> {
            settings.store("Location." + gridSize + "." + gameMode, storeBoard(grid));
            settings.store("score." + gridSize + "." + gameMode, score.toString());
            settings.store("time." + gridSize + "." + gameMode, time.toString());
            settings.store("gameID." + gridSize + "." + gameMode, Integer.toString(gameID));
        });
    }

    public int restoreSession(Map<Location, Tile> gameGrid, StringProperty time, IntegerProperty gameID) {
        return Services.get(SettingsService.class)
                .map(settings -> {
                    String stored = settings.retrieve("Location." + gridSize + "." + gameMode);
                    if (stored != null) {
                        int[] grid = restoreBoard(stored);
                        for (int y = 0; y < gridSize; y++) {
                            for (int x = 0; x < gridSize; x++) {
                                int val = grid[x + gridSize * y];
                                if (val > 1) {
                                    Tile t = Tile.newTile(val);
                                    Location l = new Location(x, y);
                                    t.setLocation(l);
                                    gameGrid.put(l, t);
                                }
                            }
                        }
                    }
                    String t = settings.retrieve("time." + gridSize + "." + gameMode);
                    if (t != null) {
                        time.set(t);
                    }

                    String game = settings.retrieve("gameID." + gridSize + "." + gameMode);
                    if (game != null) {
                        gameID.set(Integer.parseInt(game));
                    }

                    String score = settings.retrieve("score." + gridSize + "." + gameMode);
                    if (score != null) {
                        return new Integer(score);
                    }
                    return -1;
                })
                .orElse(-1);
    }

    public void saveRecord(Integer score) {
        int oldRecord = restoreRecord();
        Services.get(SettingsService.class)
                .ifPresent(settings ->
                        settings.store("record." + gridSize + "." + gameMode,
                                Integer.toString(Math.max(oldRecord, score))));
    }

    public int restoreRecord() {
        return Services.get(SettingsService.class)
                .map(settings -> {
                    String record = settings.retrieve("record." + gridSize + "." + gameMode);
                    if (record != null) {
                        return new Integer(record);
                    }
                    return 0;
                })
                .orElse(0);
    }

    /*
    Converts 4x4 grid into String of 32 chars
    Each tile is stored with a value of its power of 2:
    0->00, 2->01, 4->02, 8->03, ... 2048->0B, .... 32768->0F, 65536->10, 131072->11, 262144->12
    */
    private String storeBoard(int[] grid) {
        String board = "";
        for (int i = grid.length-1; i >= 0; i--) {
            int cont=0;
            int x = grid[i] == 0 ? 1 : grid[i];
            while (((x & 1) == 0) && x > 1) {
                x >>= 1;
                cont++;
            }
            String s = Long.toString(cont, 16);
            board = board.concat(s.length() == 1 ? "0" : "").concat(s);
        }
        return board;
    }

    private static int[] restoreBoard(String stored) {
        int[] grid = new int[16];
        for(int i = 0; i < grid.length; i++) {
            String s = stored.substring(stored.length() - 2, stored.length());
            stored=stored.substring(0,stored.length() - 2);
            int val = 1 << Long.parseLong(s, 16);
            grid[i] = val > 1 ? val : 0;
        }
        return grid;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
        Services.get(SettingsService.class)
                .ifPresent(settings -> settings.store(GAME_MODE, Integer.toString(gameMode)));
    }

    public int getGameMode(){
        return Services.get(SettingsService.class)
                .map(settings -> Integer.parseInt(settings.retrieve(GAME_MODE)))
                .orElse(0);
    }

    public void setGameID(int gameID) {
        Services.get(SettingsService.class)
                .ifPresent(settings -> settings.store(GAME_ID, Integer.toString(gameID)));
    }

    public int getGameID(){
        return Services.get(SettingsService.class)
                .map(settings -> Integer.parseInt(settings.retrieve(GAME_ID)))
                .orElse(0);
    }

}
