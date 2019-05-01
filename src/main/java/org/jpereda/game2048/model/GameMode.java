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

/**
 *
 * @author jpereda
 */
public enum GameMode {

    // TODO: use real values: 1.1.*
    EASY        (0, "Easy Mode",        "leaderboard_v1.0.0"),
    ADVANCED    (1, "Advanced Mode",    "leaderboard_v1.0.1"),
    EXPERT      (2, "Expert Mode",      "leaderboard_v1.0.2");
    
    private final int mode;
    private final String text;
    private final String cloud;
    
    GameMode(int mode, String text, String cloud) {
        this.mode = mode;
        this.text = text;
        this.cloud = cloud;
    }
    
    public int getMode() { return mode; }
    public String getText() { return text; }
    public String getCloud() { return cloud; }
}
