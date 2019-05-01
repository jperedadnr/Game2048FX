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

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.down.plugins.LifecycleService;
import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.AppViewRegistry;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.cloudlink.client.user.User;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import org.jpereda.game2048.menu.About;
import org.jpereda.game2048.service.Cloud;

import java.time.Year;
import java.util.Locale;

import static com.gluonhq.charm.glisten.afterburner.AppView.Flag.HOME_VIEW;
import static com.gluonhq.charm.glisten.afterburner.AppView.Flag.SHOW_IN_DRAWER;
import static com.gluonhq.charm.glisten.afterburner.AppView.Flag.SKIP_VIEW_STACK;
import static org.jpereda.game2048.Game2048.VERSION;

public class AppViewManager {

    private static final AppViewRegistry REGISTRY = new AppViewRegistry();

    public static final AppView GAME_VIEW = view("Game 2048FX", GamePresenter.class, MaterialDesignIcon.GRID_ON, SHOW_IN_DRAWER, HOME_VIEW, SKIP_VIEW_STACK);
    static final AppView BOARD_VIEW = view("Leaderboard", BoardPresenter.class, MaterialDesignIcon.DVR, SHOW_IN_DRAWER);
    static final AppView SETTINGS_VIEW = view("Game Settings", SettingsPresenter.class, MaterialDesignIcon.SETTINGS, SHOW_IN_DRAWER);
    private static Cloud cloud;

    private static AppView view(String title, Class<? extends GluonPresenter<?>> presenterClass, MaterialDesignIcon menuIcon, AppView.Flag... flags ) {
        return REGISTRY.createView(name(presenterClass), title, presenterClass, menuIcon, flags);
    }

    private static String name(Class<? extends GluonPresenter<?>> presenterClass) {
        return presenterClass.getSimpleName().toUpperCase(Locale.ROOT).replace("PRESENTER", "");
    }

    public static void registerViews(MobileApplication app) {
        NavigationDrawer drawer = app.getDrawer();
        for (AppView view : REGISTRY.getViews()) {
            view.registerView(app);
            if (view.isShownInDrawer()) {
                drawer.getItems().add(view.getMenuItem());
            }
        }

        cloud = Injector.instantiateModelOrService(Cloud.class);
        cloud.authenticatedUserProperty().addListener(o ->
                drawer.setHeader(getHeader()));

        drawer.setHeader(getHeader());

        final NavigationDrawer.Item aboutItem = new NavigationDrawer.Item("About 2048FX", MaterialDesignIcon.INFO_OUTLINE.graphic());
        drawer.selectedItemProperty().addListener((obs, ov, nv) -> {
            if (aboutItem.equals(nv)) {
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(f -> new About());
                pause.play();
                drawer.setSelectedItem(GAME_VIEW.getMenuItem());
            }
        });
        drawer.getItems().add(aboutItem);

        if (Platform.isDesktop()) {
            NavigationDrawer.Item quitItem = new NavigationDrawer.Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
            quitItem.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv) {
                    Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                }
            });
            drawer.getItems().add(quitItem);
        }

        drawer.skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                ListTile versionTile = new ListTile();
                versionTile.setPrimaryGraphic(new ImageView());
                versionTile.textProperty().addAll("2048FX", "Version " + VERSION + " - " + Year.now().toString());
                HBox hBottom = new HBox(versionTile);
                hBottom.getStyleClass().add("drawer-bottom");
                HBox.setHgrow(versionTile, Priority.ALWAYS);

                ((BorderPane) drawer.getChildrenUnmodifiable().get(0)).setBottom(hBottom);

                drawer.skinProperty().removeListener(this);
            }
        });
    }

    private static NavigationDrawer.Header getHeader() {
        if (cloud.isAuthenticated()) {
            double radius = Services.get(DisplayService.class)
                    .map(display -> display.isTablet() ? 30 : 20)
                    .orElse(20);
            User authenticatedUser = cloud.getAuthenticatedUser();
            return new NavigationDrawer.Header(authenticatedUser.getName(),
                    authenticatedUser.getNick() != null && !authenticatedUser.getNick().isEmpty()
                            && !authenticatedUser.getName().equals(authenticatedUser.getNick()) ? authenticatedUser.getNick() : "",
                    authenticatedUser.getPicture() != null ? new Avatar(radius, new Image(authenticatedUser.getPicture(), true))
                            : MaterialDesignIcon.ACCOUNT_CIRCLE.graphic());
        }
        return new NavigationDrawer.Header("User not registered", "",
                MaterialDesignIcon.ACCOUNT_CIRCLE.graphic());
    }
}
