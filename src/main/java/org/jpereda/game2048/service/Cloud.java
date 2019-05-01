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
package org.jpereda.game2048.service;

import com.gluonhq.cloudlink.client.data.DataClient;
import com.gluonhq.cloudlink.client.data.DataClientBuilder;
import com.gluonhq.cloudlink.client.data.OperationMode;
import com.gluonhq.cloudlink.client.data.SyncFlag;
import com.gluonhq.cloudlink.client.usage.UsageClient;
import com.gluonhq.cloudlink.client.user.User;
import com.gluonhq.cloudlink.client.user.UserClient;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.jpereda.game2048.model.GameMode;
import org.jpereda.game2048.model.GameModel;
import org.jpereda.game2048.model.Score;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpereda
 */
public class Cloud {
    
    private final ReadOnlyObjectWrapper<User> authenticatedUser = new ReadOnlyObjectWrapper<>();
    private final Map<GameMode, GluonObservableList<Score>> map = new HashMap<>();
    private DataClient dataClient;

    @Inject
    private GameModel gameModel;
    
    @PostConstruct
    public void login() {
        UsageClient usageClient = new UsageClient();
        usageClient.enable();

        UserClient userClient = new UserClient();
        userClient.authenticatedUserProperty().addListener((obs, ov, nv) -> authenticatedUser.setValue(nv));
        authenticatedUser.set(userClient.getAuthenticatedUser());

        dataClient = DataClientBuilder.create()
            .operationMode(OperationMode.CLOUD_FIRST)
            .authenticateWith(userClient)
            .build();

        if (userClient.isAuthenticated()) {
            updateLeaderboard();
        }
    }

    public void forceLogin() {
        DataProvider.<Score>retrieveList(dataClient.createListDataReader(
                GameMode.EASY.getCloud(), Score.class));
    }
    
    public ReadOnlyObjectWrapper<User> authenticatedUserProperty() {
        return authenticatedUser;
    }
    
    public User getAuthenticatedUser() { 
        return authenticatedUser.get(); 
    }
    
    public boolean isAuthenticated() { 
        return (getAuthenticatedUser() != null);
    }

    public GluonObservableList<Score> updateLeaderboard() {
        GameMode key = gameModel.gameModeProperty().get();
        if (map.get(key) != null) {
            return map.get(key);
        }

        GluonObservableList<Score> retrieveList = DataProvider.<Score>retrieveList(dataClient.createListDataReader(
                key.getCloud(),
                Score.class, SyncFlag.LIST_WRITE_THROUGH, SyncFlag.LIST_READ_THROUGH,
                SyncFlag.OBJECT_WRITE_THROUGH, SyncFlag.OBJECT_READ_THROUGH));
        retrieveList.exceptionProperty().addListener((obs, ov, nv) -> nv.printStackTrace());
        map.put(key, retrieveList);
        return retrieveList;
    }

}
