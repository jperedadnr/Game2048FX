/*
 * Copyright (C) 2013-2015 2048FX 
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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class DesktopPlatformProvider implements PlatformProvider {

    @Override
    public ObservableList<Image> getIcons() {
        return FXCollections.<Image>observableArrayList(
                new Image(Game2048.class.getResourceAsStream("ic_launcher.png")));
    }

    @Override
    public BooleanProperty stopProperty() {
        return new SimpleBooleanProperty();
    }
    
    @Override
    public BooleanProperty pauseProperty() {
        return new SimpleBooleanProperty();
    }
    
    @Override
    public void exit(){
        Platform.exit();
    }
}
