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
package org.jpereda.game2048.menu;

import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.BrowserService;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.glisten.control.Dialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.jpereda.game2048.Game2048;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Year;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JosePereda
 */
public class About {
    
    public About() {
        
        HBox title = new HBox(10);
        title.setAlignment(Pos.CENTER_LEFT);
        title.getChildren().add(new ImageView());
        title.getChildren().add(new Label("About the App"));
        
        Dialog<ButtonType> dialog = new Dialog<>();
        
        Text t00 = new Text("2048");
        t00.getStyleClass().setAll("game-label","game-lblAbout");
        Text t01 = new Text("FX");
        t01.getStyleClass().setAll("game-label","game-lblAbout2");
        Text t02 = new Text(" Game\n");
        t02.getStyleClass().setAll("game-label","game-lblAbout");
        Text t1 = new Text("JavaFX game - " + Platform.getCurrent().name() + " version\n\n");
        t1.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Text t20 = new Text("Powered by ");
        t20.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Hyperlink link11 = new Hyperlink();
        link11.setText("JavaFXPorts");
        link11.setOnAction(e -> browse("http://gluonhq.com/open-source/javafxports/"));
        link11.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        Text t210 = new Text(" & ");
        t210.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Hyperlink link12 = new Hyperlink();
        link12.setText("Gluon Mobile");
        link12.setOnAction(e -> browse("https://gluonhq.com/products/mobile/"));
        link12.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        Text t21 = new Text(" Projects \n\n");
        t21.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Text t23 = new Text("\u00A9 ");
        t23.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Hyperlink link2 = new Hyperlink();
        link2.setText("José Pereda");
        link2.setOnAction(e -> browse("https://twitter.com/JPeredaDnr"));
        link2.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        Text t22 = new Text(", ");
        t22.getStyleClass().setAll("game-label", "game-lblAboutSub");
        Hyperlink link3 = new Hyperlink();
        link3.setText("Bruno Borges");
        link3.setOnAction(e -> browse("https://twitter.com/brunoborges"));
        Text t32 = new Text(" & ");
        t32.getStyleClass().setAll("game-label", "game-lblAboutSub");
        link3.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        Hyperlink link4 = new Hyperlink();
        link4.setText("Jens Deters");
        link4.setOnAction(e -> browse("https://twitter.com/Jerady"));
        link4.getStyleClass().setAll("game-label", "game-lblAboutSub2");
        Text t24 = new Text("\n\n");
        t24.getStyleClass().setAll("game-label", "game-lblAboutSub");

        Text t31 = new Text(" Version " + Game2048.VERSION + " - " + Year.now().toString() + "\n\n");
        t31.getStyleClass().setAll("game-label", "game-lblAboutSub");

        TextFlow flow = new TextFlow();
        flow.setTextAlignment(TextAlignment.CENTER);
        flow.setPadding(new Insets(10,0,0,0));

        flow.getChildren().setAll(t00, t01, t02, t1, t20, link11, t210, link12, t21, t23, link2, t22, link3);
        flow.getChildren().addAll(t32, link4);
        flow.getChildren().addAll(t24, t31);
        
        final ImageView imageView = new ImageView();
        imageView.getStyleClass().add("about");
        
        double scale = Services.get(DisplayService.class)
                .map(display -> {
                    if (display.isTablet()) {
                        flow.setTranslateY(30);
                        return 1.3;
                    } else {
                        flow.setTranslateY(25);
                        return 1.0;
                    }
                })
                .orElse(1.0);
        
        imageView.setFitWidth(270 * scale);
        imageView.setFitHeight(270 * scale);
        imageView.setOpacity(0.1);
        
        dialog.setContent(new StackPane(imageView, flow));
        dialog.setTitle(title);
        
        Button yes = new Button("Accept");
        yes.setOnAction(e -> {
            dialog.setResult(ButtonType.YES); 
            dialog.hide();
        });
        yes.setDefaultButton(true);
        dialog.getButtons().addAll(yes);
        javafx.application.Platform.runLater(dialog::showAndWait);
    }
    
    private void browse(String url) {
        Services.get(BrowserService.class)
            .ifPresent(browser -> {
                try {
                    browser.launchExternalBrowser(url);
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
    }
}
