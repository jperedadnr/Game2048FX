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
