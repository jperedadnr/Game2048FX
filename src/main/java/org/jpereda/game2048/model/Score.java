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

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Cache;
import com.gluonhq.charm.down.plugins.CacheService;
import javafx.scene.image.Image;

import java.util.Objects;

/**
 *
 * @author jpereda
 */
public class Score {
    
    private String userName;
    private String userPic;
    private long timeStamp;
    private int gameID;
    private int score;
    
    private static final Cache<String, Image> cache;

    static {
        cache = Services.get(CacheService.class)
                .map(s -> s.<String, Image>getCache("images"))
                .orElseThrow(() -> new RuntimeException("No cache"));
    }
    
    public Score() {
    }

    public Score(String userName, String userPic, long timeStamp, int gameID, int score) {
        this.userName = userName;
        this.userPic = userPic;
        this.timeStamp = timeStamp;
        this.gameID = gameID;
        this.score = score;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
    
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.userName);
        hash = 53 * hash + Objects.hashCode(this.userPic);
        hash = 53 * hash + (int) (this.timeStamp ^ (this.timeStamp >>> 32));
        hash = 53 * hash + this.gameID;
        hash = 53 * hash + this.score;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Score other = (Score) obj;
        if (this.timeStamp != other.timeStamp) {
            return false;
        }
        if (this.gameID != other.gameID) {
            return false;
        }
        if (this.score != other.score) {
            return false;
        }
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return Objects.equals(this.userPic, other.userPic);
    }

    @Override
    public String toString() {
        return "Score{" + "userName=" + userName + ", userPic=" + userPic + ", timeStamp=" + timeStamp + ", gameID=" + gameID + ", score=" + score + '}';
    }

    /**
     * This method will always return the required image.
     * It will cache the image and return from cache if still there.
     * @param image: A valid url to retrieve the image
     * @return an Image
     */
    public static Image getImage (String image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        Image cachedImage = cache.get(image);
        if (cachedImage == null) {
            cachedImage = new Image(image, true);
            cache.put(image, cachedImage);
        }
        return cachedImage;
    }

}
