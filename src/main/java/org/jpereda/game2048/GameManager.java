package org.jpereda.game2048;

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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 *
 * @author jpereda
 */
public class GameManager extends Group {
    
    public static final int FINAL_VALUE_TO_WIN = 2048;
    
    private Board board;
    private GridOperator gridOperator;
    private volatile boolean movingTiles = false;
    private final List<Location> locations = new ArrayList<>();
    private Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();
    private final BooleanProperty moving = new SimpleBooleanProperty(false);
    private final ParallelTransition parallelTransition = new ParallelTransition();
    
    public GameManager() {
        this(GridOperator.DEFAULT_GRID_SIZE);
    }
    
    /**
     * GameManager is a Group containing a Board that holds a grid and the score
     * a Map holds the location of the tiles in the grid
     * 
     * The purpose of the game is sum the value of the tiles up to 2048 points
     * Based on the Javascript version: https://github.com/gabrielecirulli/2048
     * 
     * @param gridSize defines the size of the grid, default 4x4
     */
    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        
        gridOperator=new GridOperator(gridSize);
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
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
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
        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocs = new ArrayList<>(locations);
        Collections.shuffle(randomLocs);
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
        
        board.setPoints(0);
        tilesWereMoved = 0;
        gridOperator.sortGrid(direction);
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
                Location thisloc = new Location(x, y);
                Location farthestLocation = findFarthestLocation(thisloc, direction); // farthest available location
                Tile tile = gameGrid.get(thisloc);
                AtomicInteger result=new AtomicInteger();
                Location nextLocation = farthestLocation.offset(direction); // calculates to a possible merge
                if(nextLocation!=null && gameGrid.get(nextLocation)!=null){
                    Tile t=gameGrid.get(nextLocation);
                    if(t.isMergeable(tile) && !t.isMerged()){
                        t.merge(tile);
                        t.toFront();
                        gameGrid.put(nextLocation, t);
                        gameGrid.put(thisloc, null);

                        parallelTransition.getChildren().add(animateExistingTile(tile, t.getLocation()));
                        parallelTransition.getChildren().add(animateMergedTile(t));
                        mergedToBeRemoved.add(tile);

                        board.addPoints(t.getValue());

                        if (t.getValue() == FINAL_VALUE_TO_WIN) {
                            board.setGameWin(true);
                        }
                        result.set(1);
                    }
                }
                if (result.get()==0 && tile!=null && !farthestLocation.equals(thisloc)) {
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

        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                movingTiles = false;
                moving.set(false);
            }
            
            board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);

            Location randomAvailableLocation = findRandomAvailableLocation();
            if (randomAvailableLocation == null && mergeMovementsAvailable() == 0 ) {
                // game is over if there are no more moves available
                board.setGameOver(true);
            } else if (randomAvailableLocation != null && tilesWereMoved > 0) {
                addAndAnimateRandomTile(randomAvailableLocation);
            }

            mergedToBeRemoved.clear();

            // reset merged after each movement
            for(Tile t:gameGrid.values()){
                if(t!=null){
                    t.clearMerge();
                }
            }
            
        });
                
        synchronized (gameGrid) {
            movingTiles = true;
            moving.set(true);
        }
        
        parallelTransition.play();
        parallelTransition.getChildren().clear();
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

        Collections.shuffle(availableLocations);
        Location randomLocation = availableLocations.get(new Random().nextInt(availableLocations.size()));
        return randomLocation;
    }
    
    /**
     * Adds a tile of random value to a random location with a proper animation
     * 
     * @param randomLocation 
     */
    private void addAndAnimateRandomTile(Location randomLocation) {
        Tile tile = board.addRandomTile(randomLocation);
        gameGrid.put(tile.getLocation(), tile);
        
        animateNewlyAddedTile(tile).play();
    }
    
    /**
     * Animation that creates a fade in effect when a tile is added to the game 
     * by increasing the tile scale from 0 to 100% 
     * @param tile to be animated
     * @return a scale transition 
     */
    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(125), tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setOnFinished(e -> {
            // after last movement on full grid, check if there are movements available
            if (checkEndGame()) {
                board.setGameOver(true);
            }
        });
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

        KeyFrame kfX = new KeyFrame(Duration.millis(65), kvX);
        KeyFrame kfY = new KeyFrame(Duration.millis(65), kvY);

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
        final ScaleTransition scale0 = new ScaleTransition(Duration.millis(80), tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final ScaleTransition scale1 = new ScaleTransition(Duration.millis(80), tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        return new SequentialTransition(scale0, scale1);
    }
    
    private boolean checkEndGame(){
        int result=0;
        for(Integer x: gridOperator.getTraverseX()){
            for(Integer y:gridOperator.getTraverseY()){
                 if(gameGrid.get(new Location(y, x))==null){
                    result+=1;
                }
            }
        }
        return result==0 && mergeMovementsAvailable() == 0;
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
     * Set gameManager scale to adjust overall game size
     * @param scale 
     */
    public void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    /**
     * Check if overlay covers the grid or not
     * @return 
     */
    public BooleanProperty isLayerOn() {
        return board.isLayerOn();
    }
    
    /**
     * Pauses the game time, covers the grid
     */
    public void pauseGame() {
        board.pauseGame();
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
        board.tryAgain();
    }
    
    public void aboutGame() {
        board.aboutGame();
    }
    
    public void setToolBar(HBox toolbar){
        board.setToolBar(toolbar);
    }
    
    public void externalPause(boolean b, boolean b1){
        board.externalPause(b,b1);
    }
}
