package org.example.schiffuntergang;


import javafx.scene.paint.Color;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import java.util.*;
import java.util.random.RandomGenerator;

public class EnemyPlayer {

    private final Gamefield playerBoard;
    private final Random rand = new Random();
    private final List<int[]> priorityTargets = new ArrayList<>();
    private int[] firstHit = null;
    private boolean directionFound = false;

    public EnemyPlayer(Gamefield playerBoard) {
        this.playerBoard = playerBoard;
    }

    /**
     * Hauptmethode für den Zug des Gegners.
     */
    public void revenge() {
        if (!priorityTargets.isEmpty()) {
            int[] target = priorityTargets.remove(priorityTargets.size() - 1);
            shootAt(target[0], target[1]);
            return;
        }

        firstHit = null;
        directionFound = false;

        // --- JAGD-MODUS ---
        int x, y;
        Cell targetCell;
        do {
            // KORREKTUR: x (Spalte) wird mit getLang() generiert, y (Reihe) mit getBreit().
            // So wie es dein restlicher Code erwartet.
            x = rand.nextInt(playerBoard.getLang());  // Annahme: getLang() = Anzahl Spalten
            y = rand.nextInt(playerBoard.getBreit()); // Annahme: getBreit() = Anzahl Reihen

            targetCell = playerBoard.getCell(x, y);

            // SICHERHEITSPRÜFUNG: Es ist theoretisch möglich, dass getCell hier null zurückgibt,
            // wenn die Annahmen über getLang/getBreit falsch sind. Das verhindert den Absturz.
        } while (targetCell == null || targetCell.isShot());

        shootAt(x, y);
    }

    /**
     * Führt einen Schuss auf die angegebenen Koordinaten aus und aktualisiert den KI-Status.
     */
    private void shootAt(int x, int y) {
        Cell cell = playerBoard.getCell(y, x);

        // SICHERHEITSPRÜFUNG: Verhindert Absturz, falls eine ungültige Koordinate übergeben wird.
        if (cell == null || cell.isShot()) {
            // Wenn das Ziel ungültig ist oder schon beschossen wurde, versuche es einfach nochmal.
            // Dies verhindert, dass die KI in einer Endlosschleife stecken bleibt, wenn alle Ziele in
            // priorityTargets ungültig werden.
            if(priorityTargets.isEmpty()) {
                revenge(); // Starte einen neuen Jagd-Versuch
            }
            return;
        }

        cell.setShot(true);
        Ships ship = cell.getShip();

        if (ship != null) { // TREFFER!
            ship.hit();
            cell.setFill(Color.RED);
            System.out.println("Gegner trifft bei: (" + x + ", " + y + ")");

            if (!ship.isAlive()) {
                System.out.println("Gegner hat ein Schiff versenkt!");
                playerBoard.deleteShip();
                priorityTargets.clear();
                firstHit = null;
                directionFound = false;
            } else {
                handleHit(x, y);
            }
        } else { // WASSER
            cell.setFill(Color.BLACK);
            System.out.println("Gegner schießt Wasser bei: (" + x + ", " + y + ")");
        }
    }

    /**
     * Verarbeitet die Logik nach einem erfolgreichen Treffer.
     */
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

    /**
     * Fügt die vier Nachbarn eines Punktes zur Prioritätsliste hinzu.
     */
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

    /**
     * Fügt ein einzelnes Ziel zur Prioritätsliste hinzu, wenn es gültig und unbeschossen ist.
     */
    private void addTarget(int x, int y) {
        // KORREKTUR: Zuerst die Zelle holen, dann prüfen. Das verhindert den NullPointerException.
        Cell targetCell = playerBoard.getCell(x, y);
        if (targetCell != null && !targetCell.isShot()) {
            priorityTargets.add(new int[]{x, y});
        }
    }
    }


