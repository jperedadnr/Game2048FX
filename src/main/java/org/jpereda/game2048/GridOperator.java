package org.jpereda.game2048;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jpereda
 */
public class GridOperator {
    
    public static final int DEFAULT_GRID_SIZE = 4;
    
    private final int gridSize;
    private final List<Integer> traversalX=new ArrayList<>();
    private final List<Integer> traversalY=new ArrayList<>();
    
    public GridOperator(){
        this(DEFAULT_GRID_SIZE);
    }
    
    public GridOperator(int gridSize){
        this.gridSize=gridSize;
        for(int i=0; i<gridSize; i++){
            traversalX.add(i);
            traversalY.add(i);
        }
    }
    
    public void sortGrid(Direction direction){
        Collections.sort(traversalX, direction.equals(Direction.RIGHT) ? Collections.reverseOrder() : Integer::compareTo);
        Collections.sort(traversalY, direction.equals(Direction.DOWN)? Collections.reverseOrder() : Integer::compareTo);
    }
    
    public int traverseGrid() {
        AtomicInteger at = new AtomicInteger();
        return at.get();
    }
    
    public int getGridSize(){ return gridSize; }
    
    public boolean isValidLocation(Location loc){
        return loc.getX() >= 0 && loc.getX() < gridSize && loc.getY() >= 0 && loc.getY() < gridSize;
    }
    
    public List<Integer> getTraverseX() { return traversalX; }
    public List<Integer> getTraverseY() { return traversalY; }
    
}
