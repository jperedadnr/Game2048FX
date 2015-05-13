package org.jpereda.game2048;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.gluon.charm.down.common.PlatformFactory;
import org.gluon.charm.down.common.Platform.LifecycleEvent;

public class IosPlatformProvider implements PlatformProvider {

    private final BooleanProperty stop = new SimpleBooleanProperty();
    private final BooleanProperty pause = new SimpleBooleanProperty();

    {
        PlatformFactory.getPlatform().setOnLifecycleEvent((LifecycleEvent param) -> {
            switch (param) {
                case START:
                    pause.set(false);
                    stop.set(false);
                    break;
                case PAUSE:
                    pause.set(true);
                    break;
                case RESUME:
                    pause.set(false);
                    stop.set(false);
                    break;
                case STOP:
                    stop.set(true);
                    break;
            }
            return null;
        });
    }

    @Override
    public void exit() {
        Platform.exit();
    }

    @Override
    public ObservableList<Image> getIcons() {
        return FXCollections.<Image>observableArrayList();
    }

    @Override
    public BooleanProperty stopProperty() {
        return stop;
    }

    @Override
    public BooleanProperty pauseProperty() {
        return pause;
    }

}
