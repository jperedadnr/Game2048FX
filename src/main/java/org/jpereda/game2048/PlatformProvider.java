package org.jpereda.game2048;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public interface PlatformProvider {

    ObservableList<Image> getIcons();
    
    BooleanProperty stopProperty();
    
    BooleanProperty pauseProperty();
    
    void exit();
    
}
