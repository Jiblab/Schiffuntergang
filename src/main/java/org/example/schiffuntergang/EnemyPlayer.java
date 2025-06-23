package org.example.schiffuntergang;

import javafx.scene.paint.Color;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import java.util.*;
import java.util.random.RandomGenerator;

public class EnemyPlayer {
    private Gamefield board;
    private HelloController contr;

    private Random rand = new Random();

    // Zielsystem
    private final Queue<int[]> targetQueue = new LinkedList<>();
    private final List<int[]> hitStack = new ArrayList<>();
    private int[] lastDirection = null;

    public EnemyPlayer(Gamefield b, HelloController c){
        board = b;
        contr = c;
    }

    public void revenge(){

        //contr.setPlayerturn();
        //TODO es wird irgendwo eine nullpointer exception geworfen muss gefixt werden
        int nextposx = RandomGenerator.getDefault().nextInt(0,  board.getBreit());
        int nextposy = RandomGenerator.getDefault().nextInt(0, board.getLang());
        board.shoot(nextposx, nextposy);

    }



    /*private void fireAt(int x, int y){
        if (!board.inBounds(x, y)) return;

        Cell c = board.getCell(x, y);
        if (c.isShot()) return;

        Ships s = c.getShip();
        c.setShot(true);

        if (s != null){
            s.hit();
            c.setFill(Color.RED);
            hitStack.add(new int[]{x, y});
            enqueueNeighbors(x, y); // gezielt weiterschießen
        }
        else {
            c.setFill(Color.BLACK);
        }
    }*/


}

