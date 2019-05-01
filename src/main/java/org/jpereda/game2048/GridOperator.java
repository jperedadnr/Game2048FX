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
