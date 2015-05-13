package org.jpereda.game2048;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.gluon.charm.down.common.PlatformFactory;

public class PlatformService {

    public static final String DESKTOP = "Desktop";
    public static final String ANDROID = "Android";
    public static final String IOS = "iOS";

    private static final Logger LOG = Logger.getLogger(PlatformService.class.getName());

    private static PlatformService instance;

    public static synchronized PlatformService getInstance() {
        if (instance == null) {
            instance = new PlatformService();
        }
        return instance;
    }

    private final ServiceLoader<PlatformProvider> serviceLoader;
    private PlatformProvider provider;

    private PlatformService() {
        serviceLoader = ServiceLoader.load(PlatformProvider.class);
        try {
            Iterator<PlatformProvider> iterator = serviceLoader.iterator();
            while (iterator.hasNext()) {
                if (provider == null) {
                    provider = iterator.next();
                    LOG.info(format("Using PlatformProvider: %s", provider.getClass().getName()));
                } else {
                    LOG.info(format("This PlatformProvider is ignored: %s", iterator.next().getClass().getName()));
                }
            }
        } catch (Exception e) {
            throw new ServiceConfigurationError("Failed to access + ", e);
        }
        if (provider == null) {
            LOG.severe("No PlatformProvider implementation could be found!");
        }
    }

    public void launchURL(String url) {
        try {
            PlatformFactory.getPlatform().launchExternalBrowser(url);
        } catch (IOException | URISyntaxException ex) {
            LOG.severe(ex.getMessage());
        }
    }

    public ObservableList<Image> getIcons() {
        return provider == null ? FXCollections.<Image>observableArrayList() : provider.getIcons();
    }

    public BooleanProperty stopProperty() {
        return provider == null ? new SimpleBooleanProperty() : provider.stopProperty();
    }

    public BooleanProperty pauseProperty() {
        return provider == null ? new SimpleBooleanProperty() : provider.pauseProperty();
    }

    public void exit() {
        if (provider != null) {
            provider.exit();
        }
    }

}
