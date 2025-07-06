package org.example.schiffuntergang;

import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import javafx.scene.paint.Color;
import java.util.*;


public class EnemyPlayer {
    private final Gamefield playerBoard;
    private final Random rand = new Random();
    private final List<int[]> priorityTargets = new ArrayList<>();
    private int[] firstHit = null;
    private boolean directionFound = false;

    public EnemyPlayer(Gamefield playerBoard) {
        this.playerBoard = playerBoard;
    }


    /*public boolean revenge() {

        if (!priorityTargets.isEmpty()) {
            int[] target = priorityTargets.remove(priorityTargets.size() - 1);
            return shootAt(target[0], target[1]);
        }

        firstHit = null;
        directionFound = false;

        int x, y;
        Cell targetCell;
        do {
            x = rand.nextInt(playerBoard.getLang());
            y = rand.nextInt(playerBoard.getBreit());
            targetCell = playerBoard.getCell(x, y);
        } while (targetCell == null || targetCell.isShot());

        return shootAt(x, y);
    }*/
    public void revenge() {
        while (true) {
            int x, y;

            if (!priorityTargets.isEmpty()) {
                int[] target = priorityTargets.remove(priorityTargets.size() - 1);
                x = target[0];
                y = target[1];
            }

            else {
                firstHit = null;
                directionFound = false;
                x = rand.nextInt(playerBoard.getLang()); // Breite = Anzahl Spalten
                y = rand.nextInt(playerBoard.getBreit());  // Länge = Anzahl Reihen
            }

            // shootAt gibt true bei Treffer, false bei nicht treffer
            Cell cellToCheck = playerBoard.getCell(x, y);
            if (cellToCheck != null && !cellToCheck.isShot()) {
                shootAt(x, y);
                break;
            }
        }
    }
    private boolean shootAt(int x, int y) {
        Cell cell = playerBoard.getCell(x, y);

        if (cell == null || cell.isShot()) {
            return false;
        }

        cell.setShot(true);
        Ships ship = cell.getShip();

        if (ship != null) { // treffer
            ship.hit();
            javafx.application.Platform.runLater(() -> cell.setFill(Color.RED));
            System.out.println("[EnemyPlayer] KI trifft bei: (" + x + ", " + y + ")");

            if (!ship.isAlive()) {
                System.out.println("[EnemyPlayer] KI hat ein Schiff versenkt!");
                playerBoard.deleteShip();
                priorityTargets.clear();
                firstHit = null;
                directionFound = false;
            } else {
                handleHit(x, y);
            }
            return true;
        } else { // wasser
            javafx.application.Platform.runLater(() -> cell.setFill(Color.BLACK));
            System.out.println("[EnemyPlayer] KI schießt Wasser bei: (" + x + ", " + y + ")");
            return false; // fehlschuss
        }
    }
    private void handleHit(int x, int y) {
        if (firstHit == null) {
            firstHit = new int[]{x, y};
            addNeighborsToPriorityList(x, y);
        } else {
            if (!directionFound) {
                directionFound = true;
                priorityTargets.clear();
            }
            if (x == firstHit[0]) { // Vertikale Achse
                addTarget(x, y + 1);
                addTarget(x, y - 1);
            } else { // Horizontale Achse
                addTarget(x + 1, y);
                addTarget(x - 1, y);
            }
        }
    }
    private void addNeighborsToPriorityList(int x, int y) {
        List<int[]> neighbors = new ArrayList<>();
        neighbors.add(new int[]{x + 1, y});
        neighbors.add(new int[]{x - 1, y});
        neighbors.add(new int[]{x, y + 1});
        neighbors.add(new int[]{x, y - 1});

        Collections.shuffle(neighbors);
        for (int[] neighbor : neighbors) {
            addTarget(neighbor[0], neighbor[1]);
        }
    }
    private void addTarget(int x, int y) {
        Cell targetCell = playerBoard.getCell(x, y);
        if (targetCell != null && !targetCell.isShot()) {
            priorityTargets.add(new int[]{x, y});
        }
    }
    public int[] getShotCoordinates() {
        if (!priorityTargets.isEmpty()) {
            return priorityTargets.remove(priorityTargets.size() - 1);
        }

        firstHit = null;
        directionFound = false;

        int x, y;
        Cell targetCell;

        do {
            // x = Spalte, y = Reihe
            x = rand.nextInt(playerBoard.getLang());
            y = rand.nextInt(playerBoard.getBreit());

            targetCell = playerBoard.getCell(x, y);

        } while (targetCell == null || targetCell.isShot());

        return new int[]{x, y};
    }
    public void processShotResult(int x, int y, boolean hit, boolean sunk) {
        if (sunk) {
            System.out.println("KI-INFO: Gegnerisches Schiff versenkt! Setze Zielliste zurück.");
            priorityTargets.clear();
            firstHit = null;
            directionFound = false;
        } else if (hit) {
            System.out.println("KI-INFO: Treffer bei (" + x + ", " + y + "). Aktualisiere Prioritätsziele.");
            // Ruft die bestehende Logik auf, um Nachbarfelder zu Zielen zu machen.
            handleHit(x, y);
        }
        // Bei einem Fehlschuss muss nichts getan werden.
    }
}