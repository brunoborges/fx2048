/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package game2048;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author jpereda
 */
public class Grid {
    
    private final List<Integer> traversalX;
    private final List<Integer> traversalY;
    
    public Grid(int gridSize){
        this.traversalX = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
        this.traversalY = IntStream.range(0, gridSize).boxed().collect(Collectors.toList());
    }
    
    public void sortGrid(Direction direction){
        Collections.sort(traversalX, direction.equals(Direction.RIGHT) ? Collections.reverseOrder() : Integer::compareTo);
        Collections.sort(traversalY, direction.equals(Direction.DOWN)? Collections.reverseOrder() : Integer::compareTo);
    }
    
    public int traverseGrid(IntBinaryOperator func) {
        AtomicInteger at = new AtomicInteger();
        traversalX.forEach(t_x -> {
            traversalY.forEach(t_y -> {
                at.addAndGet(func.applyAsInt(t_x, t_y));
            });
        });

        return at.get();
    }
    
}
