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

package org.jpereda.game2048;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.jpereda.game2048.legacy.OldRecordManager;
import org.jpereda.game2048.legacy.OldSessionManager;

/**
 *
 * @author jpereda
 */
public class GameManager extends Region {

    static final int FINAL_VALUE_TO_WIN = 2048;
    private final double timeFactor=1;

    private Board board;
    private GridOperator gridOperator;
    private volatile boolean movingTiles = false;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    // true only after every 2048 found, until the game is saved
    private final BooleanProperty tile2048Found = new SimpleBooleanProperty();

    public GameManager() {
        this(GridOperator.DEFAULT_GRID_SIZE);
    }

    /**
     * GameManager is a Group containing a Board that holds a grid and the score
     * a Map holds the location of the tiles in the grid
     *
     * @param gridSize defines the size of the grid, default 4x4
     */
    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();

        gridOperator = new GridOperator(gridSize);
        board = new Board(gridOperator);
        this.getChildren().add(board);
        board.clearGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                initializeGameGrid();
            }
        });
        board.resetGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                startGame();
            }
        });
        board.restoreGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                doRestoreSession();
            }
        });
        board.saveGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                doSaveSession();
            }
        });

        initializeGameGrid();
        startGame();
    }

    /**
     * Initializes all cells in gameGrid map to null
     */
    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        for (Integer x : gridOperator.getTraverseX()) {
            for (Integer y : gridOperator.getTraverseY()) {
                Location thisloc = new Location(x, y);
                locations.add(thisloc);
                gameGrid.put(thisloc, null);
            }
        }
    }

    /**
     * Starts the game by adding 1 or 2 tiles at random locations
     */
    private void startGame() {
        tile2048Found.set(false);
        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocs = new ArrayList<>(locations);
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        Collections.shuffle(randomLocs, random);
        tile0.setLocation(randomLocs.get(0));

        gameGrid.put(tile0.getLocation(), tile0);

        if (new Random().nextFloat() <= 0.8) { // gives 80% chance to add a second tile
            Tile tile1 = Tile.newRandomTile();
            if (tile1.getValue() == 4 && tile0.getValue() == 4) {
                tile1 = Tile.newTile(2);
            }
            tile1.setLocation(randomLocs.get(1));
            gameGrid.put(tile1.getLocation(), tile1);
        }

        redrawTilesInGameGrid();

        board.startGame();
    }

    /**
     * Redraws all tiles in the <code>gameGrid</code> object
     */
    private void redrawTilesInGameGrid() {
        for(Tile t: gameGrid.values()){
            if(t!=null){
                board.addTile(t);
            }
        }
    }

    private int tilesWereMoved = 0;
    private void moveTiles(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        ParallelTransition parallelTransition = new ParallelTransition();
        board.setPoints(0);
        tilesWereMoved = 0;
        mergedToBeRemoved.clear();
        gridOperator.sortGrid(direction);
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
                Location thisloc = new Location(x, y);
                Location farthestLocation = findFarthestLocation(thisloc, direction); // farthest available location
                Tile tile = gameGrid.get(thisloc);
                AtomicInteger result=new AtomicInteger();
                Location nextLocation = farthestLocation.offset(direction); // calculates to a possible merge
                if(nextLocation!=null && gameGrid.get(nextLocation)!=null){
                    Tile t = gameGrid.get(nextLocation);
                    if (t.isMergeable(tile) && !t.isMerged()) {
                        t.merge(tile);
                        t.toFront();
                        gameGrid.put(nextLocation, t);
                        gameGrid.put(thisloc, null);

                        parallelTransition.getChildren().add(animateExistingTile(tile, t.getLocation()));
                        parallelTransition.getChildren().add(animateMergedTile(t));
                        mergedToBeRemoved.add(tile);

                        board.addPoints(t.getValue());

                        if (t.getValue() == FINAL_VALUE_TO_WIN) {
                            tile2048Found.set(false);
                            board.setGameWin(true);
                            tile2048Found.set(true);
                        }
                        result.set(1);
                    }
                }
                if (result.get() == 0 && tile != null && !farthestLocation.equals(thisloc)) {
                    parallelTransition.getChildren().add(animateExistingTile(tile, farthestLocation));

                    gameGrid.put(farthestLocation, tile);
                    gameGrid.put(thisloc, null);

                    tile.setLocation(farthestLocation);

                    result.set(1);
                }
                tilesWereMoved+=result.get();
            }
        }

        board.animateScore();

        if(parallelTransition.getChildren().size()>0){

            parallelTransition.setOnFinished(e -> {
                board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);

                // reset merged after each movement
                for (Tile t : gameGrid.values()) {
                    if (t != null) {
                        t.clearMerge();
                    }
                }

                Location randomAvailableLocation = findRandomAvailableLocation();
                if (randomAvailableLocation == null && mergeMovementsAvailable() == 0 ) {
                    // game is over if there are no more moves available
                    board.setGameOverAndShare(true);
                } else if (randomAvailableLocation != null && tilesWereMoved > 0) {
                    synchronized (gameGrid) {
                        movingTiles = false;
                    }
                    ScaleTransition scaleTransition=addAndAnimateRandomTile(randomAvailableLocation);
                    scaleTransition.setOnFinished(t -> {
                        if(checkEndGame()) {
                            board.setGameOverAndShare(true);
                        }
                    });
                    scaleTransition.play();
                }
            });

            synchronized (gameGrid) {
                movingTiles = true;
            }
            parallelTransition.play();
        }
    }

    /**
     * Searchs for the farthest empty location where the current tile could go
     * @param location of the tile
     * @param direction of movement
     * @return a location
     */
    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (gridOperator.isValidLocation(location) && gameGrid.get(location)==null);

        return farthest;
    }

    /**
     * Finds the number of pairs of tiles that can be merged
     *
     * This method is called only when the grid is full of tiles,
     * what makes the use of Optional unnecessary, but it could be used when the
     * board is not full to find the number of pairs of mergeable tiles and provide a hint
     * for the user, for instance
     * @return the number of pairs of tiles that can be merged
     */
    private int mergeMovementsAvailable() {
        final AtomicInteger pairsOfMergeableTiles = new AtomicInteger();
        int cont=0;
        Direction direction= Direction.UP;
        do {
            for(Integer x: gridOperator.getTraverseX()){
                for(Integer y:gridOperator.getTraverseY()){
                    Location thisloc = new Location(x, y);
                    Tile tile = gameGrid.get(thisloc);
                    if(tile!=null){
                        Tile offsetTile = gameGrid.get(thisloc.offset(direction));
                        if(offsetTile!=null && tile.isMergeable(offsetTile)){
                            pairsOfMergeableTiles.incrementAndGet();
                        }
                    }
                }
            }
            direction = Direction.LEFT;
            cont++;
        } while(cont<2);
        return pairsOfMergeableTiles.get();
    }

    /**
     * Finds a random location or returns null if none exist
     *
     * @return a random location or <code>null</code> if there are no more
     * locations available
     */
    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = new ArrayList<>();
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
                Location thisloc = new Location(x, y);
                if(gameGrid.get(thisloc)==null){
                    availableLocations.add(thisloc);
                }

            }
        }

        if (availableLocations.isEmpty()) {
            return null;
        }

        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        Collections.shuffle(availableLocations, random);
        Location randomLocation = availableLocations.get(random.nextInt(availableLocations.size()));
        return randomLocation;
    }

    /**
     * Adds a tile of random value to a random location with a proper animation
     *
     * @param randomLocation
     */
    private ScaleTransition addAndAnimateRandomTile(Location randomLocation) {
        Tile tile = board.addRandomTile(randomLocation);
        gameGrid.put(tile.getLocation(), tile);

        return animateNewlyAddedTile(tile);
    }

    /**
     * Animation that creates a fade in effect when a tile is added to the game
     * by increasing the tile scale from 0 to 100%
     * @param tile to be animated
     * @return a scale transition
     */
    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(125*timeFactor), tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        return scaleTransition;
    }

    /**
     * Animation that moves the tile from its previous location to a new location
     * @param tile to be animated
     * @param newLocation new location of the tile
     * @return a timeline
     */
    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(tile.layoutXProperty(),
                newLocation.getLayoutX(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(),
                newLocation.getLayoutY(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        KeyFrame kfX = new KeyFrame(Duration.millis(65*timeFactor), kvX);
        KeyFrame kfY = new KeyFrame(Duration.millis(65*timeFactor), kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    /**
     * Animation that creates a pop effect when two tiles merge
     * by increasing the tile scale to 120% at the middle, and then going back to 100%
     * @param tile to be animated
     * @return a sequential transition
     */
    private SequentialTransition animateMergedTile(Tile tile) {
        final ScaleTransition scale0 = new ScaleTransition(Duration.millis(80*timeFactor), tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final ScaleTransition scale1 = new ScaleTransition(Duration.millis(80*timeFactor), tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        return new SequentialTransition(scale0, scale1);
    }

    private boolean checkEndGame(){
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
                if(gameGrid.get(new Location(y, x))==null){
                    return false;
                }
            }
        }
        return mergeMovementsAvailable() == 0;
    }

    /**
     * Set gameManager scale to adjust overall game size
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double scale = Math.min(this.getWidth() / board.getWidth(), this.getHeight() / board.getHeight());
        board.setScaleX(scale);
        board.setScaleY(scale);
        board.setLayoutX((this.getWidth() - board.getWidth()) / 2d);
        board.setLayoutY((this.getHeight()- board.getHeight()) / 2d);
    }

    /*************************************************************************/
    /************************ Public methods *********************************/
    /*************************************************************************/

    /**
     * Move the tiles according user input if overlay is not on
     * @param direction
     */
    public void move(Direction direction){
        if (!board.isLayerOn().get()) {
            moveTiles(direction);
        }
    }

    /**
     * Check if overlay covers the grid or not
     * @return
     */
    public BooleanProperty overlayVisible() {
        return board.isLayerOn();
    }

    /**
     * Pauses the game time, covers the grid
     */
    public void pauseGame() {
        board.pauseGame();
    }

    /**
     * Resumes gameplay when paused
     */
    public void keepGoing(){
        board.keepGoing();
    }

    /**
     * Quit the game with confirmation
     */
    public void quitGame() {
        board.quitGame();
    }

    /**
     * Ask to save the game from a properties file with confirmation
     */
    public void saveSession() {
        board.saveSession();
    }
    /**
     * Save the game to a properties file, without confirmation
     */
    private void doSaveSession() {
        tile2048Found.set(false);
        board.saveSession(gameGrid);
    }

    /**
     * Ask to restore the game from a properties file with confirmation
     */
    public void restoreSession() {
        board.restoreSession();
    }

    /**
     * Restore the game from a properties file, without confirmation
     */
    private void doRestoreSession() {
        initializeGameGrid();
        if (board.restoreSession(gameGrid)) {
            redrawTilesInGameGrid();
        }
    }

    /**
     * Save actual record to a properties file
     */
    public void saveRecord() {
        board.saveRecord();
    }

    public void tryAgain() {
        tryAgain(true);
    }

    public void tryAgain(boolean ask) {
        board.tryAgain(ask);
    }

    public void externalPause(boolean b, boolean b1){
        board.externalPause(b,b1);
    }

    public int getScore(){
        return board.getScore();
    }

    public int getPoints(){
        return board.getPoints();
    }

    public boolean isGameOverAndShare() {
        return board.isGameOverAndShare();
    }

    public boolean isGameOver() {
        return board.isGameOver();
    }

    public boolean isGameWon() {
        return board.isGameWon();
    }

    public BooleanProperty gameWonProperty() {
        return board.gameWonProperty();
    }

    public BooleanProperty gameOverAndShareProperty() {
        return board.gameOverAndShareProperty();
    }

    public BooleanProperty gameOverProperty() {
        return board.gameOverProperty();
    }

    public BooleanProperty gameShareProperty() {
        return board.gameShareProperty();
    }

    public BooleanProperty tile2048FoundProperty() {
        return tile2048Found;
    }

    public void setTile2048Found(boolean enabled){
        tile2048Found.set(enabled);
    }

    public boolean isTile2048Found(){
        return tile2048Found.get();
    }

    public void setGameMode(int gameMode){
        board.setGameMode(gameMode);
    }

    public void setGameID(int gameID) {
        board.setGameID(gameID);
    }

    public int getGameID() {
        return board.getGameID();
    }
    public IntegerProperty gameIDProperty() {
        return board.gameIDProperty();
    }

    public final static void legacySettings() {
        GridOperator oldGridOperator = new GridOperator();
        OldSessionManager oldSession = new OldSessionManager(oldGridOperator);
        Map<Location, Tile> oldGameGrid = new HashMap<>();
        StringProperty oldTime = new SimpleStringProperty();
        int oldScore = oldSession.restoreSession(oldGameGrid, oldTime);

        if (oldScore > -1) {
            SessionManager session = new SessionManager(oldGridOperator.getGridSize());
            LocalTime oldLocalTime = LocalTime.now().minusNanos(new Long(oldTime.get()));
            session.saveSession(oldGameGrid, oldScore, LocalTime.now().minusNanos(oldLocalTime.toNanoOfDay()).toNanoOfDay(), 0);

            OldRecordManager oldRecord = new OldRecordManager(oldGridOperator.getGridSize());
            int restoreRecord = oldRecord.restoreRecord();
            session.saveRecord(restoreRecord);
        }
    }
}
