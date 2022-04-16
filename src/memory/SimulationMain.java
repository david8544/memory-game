/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.util.Set;
import java.util.Random;

/**
 * Example code.
 * 
 * <p>PS4 instructions: you may use, modify, or remove this class.
 */
public class SimulationMain {
    
    /**
     * Simulate a game.
     * 
     * @param args unused
     */
    public static void main(String[] args) {
        final int size = 10;
        final int players = 1;
        final int tries = 10;
        
        final Board board = Board.generateRandom(size, size, Set.of("A", "B"));
        
        for (int ii = 0; ii < players; ii++) {
            new Thread(() -> {
                final Random random = new Random();
                Player p = new Player("AA");
                
                for (int jj = 0; jj < tries; jj++) {
                    try {
                        board.flipCard(p, random.nextInt(size), random.nextInt(size));
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    //      which might block until this player can control that card
                    try {
                        board.flipCard(p, random.nextInt(size), random.nextInt(size));
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    
                    //      try to flip over a second card at (random.nextInt(size), random.nextInt(size))
                    System.out.println(board.webString(p));
                    
                }
            }).start();
        }
    }
}
