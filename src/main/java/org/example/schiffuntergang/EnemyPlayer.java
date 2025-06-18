package org.example.schiffuntergang;


import javafx.scene.paint.Color;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import java.util.*;
import java.util.random.RandomGenerator;

public class EnemyPlayer {
    private Gamefield board;
    private Random rand = new Random();

    private Integer safeX = null;
    private Integer safeY = null;
    private String direction = null;
    private int step = 1;

    public EnemyPlayer(Gamefield playerField) {
        this.board = playerField; // Gegner schießt auf Spielerfeld
    }

    public void revenge() {
        int x, y;

        // Wenn noch ein Ziel aktiv ist, gezielt weiterschießen
        if (safeX != null && safeY != null) {
            // Richtung noch unbekannt
            if (direction == null) {
                if (tryShoot(safeX + 1, safeY)) {
                    direction = "horizontal";
                    step = 2;
                    return;
                }
                if (tryShoot(safeX - 1, safeY)) {
                    direction = "horizontal";
                    step = 2;
                    return;
                }
                if (tryShoot(safeX, safeY + 1)) {
                    direction = "vertical";
                    step = 2;
                    return;
                }
                if (tryShoot(safeX, safeY - 1)) {
                    direction = "vertical";
                    step = 2;
                    return;
                }

                // Kein Treffer -> zurücksetzen
                resetTarget();
            } else {
                boolean hit;
                if (direction.equals("horizontal")) {
                    hit = tryShoot(safeX + step, safeY) || tryShoot(safeX - step, safeY);
                } else {
                    hit = tryShoot(safeX, safeY + step) || tryShoot(safeX, safeY - step);
                }

                if (hit) {
                    step++;
                    return;
                } else {
                    resetTarget();
                }
            }
        }

        // Zufälliger Schuss
        do {
            x = rand.nextInt(board.getBreit());
            y = rand.nextInt(board.getLang());
        } while (board.getCell(x, y).isShot());

        shootAt(x, y);
    }

    private boolean tryShoot(int x, int y) {
        if (x < 0 || x >= board.getBreit() || y < 0 || y >= board.getLang()) return false;
        Cell c = board.getCell(x, y);
        if (c.isShot()) return false;
        return shootAt(x, y);
    }

    private boolean shootAt(int x, int y) {
        Cell c = board.getCell(x, y);
        Ships s = c.getShip();
        c.setShot(true);

        if (s != null) {
            s.hit();
            c.setFill(Color.RED);
            System.out.println("Gegner trifft bei: " + x + ", " + y);
            if (safeX == null) {
                safeX = x;
                safeY = y;
            }
            return true;
        } else {
            c.setFill(Color.BLACK);
            System.out.println("Gegner schießt Wasser bei: " + x + ", " + y);
            return false;
        }
    }

    private void resetTarget() {
        safeX = null;
        safeY = null;
        direction = null;
        step = 1;
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

