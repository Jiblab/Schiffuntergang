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
    /*public boolean revenge() {

        if (!priorityTargets.isEmpty()) {
            int[] target = priorityTargets.remove(priorityTargets.size() - 1);
            return shootAt(target[0], target[1]); // Gib das Ergebnis von shootAt zurück
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

        return shootAt(x, y); // Gib das Ergebnis von shootAt zurück
    }*/
    public void revenge() { // Die Methode braucht keinen boolean mehr zurückgeben
        while (true) { // Eine Schleife, um so lange zu schießen, bis ein gültiger Zug gemacht wurde
            int x, y;

            // Modus 1: Zerstören (wenn Ziele in der Liste sind)
            if (!priorityTargets.isEmpty()) {
                int[] target = priorityTargets.remove(priorityTargets.size() - 1);
                x = target[0];
                y = target[1];
            }
            // Modus 2: Jagen (wenn keine Ziele in der Liste sind)
            else {
                firstHit = null;
                directionFound = false;
                x = rand.nextInt(playerBoard.getBreit()); // Breite = Anzahl Spalten
                y = rand.nextInt(playerBoard.getLang());  // Länge = Anzahl Reihen
            }

            // Versuche zu schießen. shootAt gibt true bei Treffer, false bei Wasser oder ungültig.
            // Wir brauchen das Ergebnis hier nicht, da shootAt bereits alles erledigt.
            // Wichtig ist aber zu prüfen, ob der Schuss überhaupt gültig war.
            Cell cellToCheck = playerBoard.getCell(x, y);
            if (cellToCheck != null && !cellToCheck.isShot()) {
                shootAt(x, y); // Schuss ausführen
                break; // Die Schleife verlassen, da ein gültiger Zug gemacht wurde
            }
            // Wenn der Schuss ungültig war (z.B. aus der Priority-List, aber inzwischen getroffen),
            // macht die while(true)-Schleife einfach weiter und holt sich das nächste Ziel.
        }
    }

// Bisher:
// private void shootAt(int x, int y) { ... }

    // Neu: gibt boolean zurück
    private boolean shootAt(int x, int y) {
        // KORREKTUR: Koordinatenverwechslung beheben. Gamefield.getCell(x, y) erwartet (Spalte, Reihe)
        Cell cell = playerBoard.getCell(x, y);

        // Wenn der Schuss ungültig ist (außerhalb oder schon getroffen), gib einfach false zurück.
        if (cell == null || cell.isShot()) {
            return false; // KEINE Rekursion mehr!
        }

        cell.setShot(true);
        Ships ship = cell.getShip();

        if (ship != null) { // TREFFER!
            ship.hit();
            javafx.application.Platform.runLater(() -> cell.setFill(Color.RED));
            System.out.println("KI trifft bei: (" + x + ", " + y + ")");

            // Fehlerbehebung: Prüfen, ob das Schiff bereits versenkt ist, bevor man es löscht.
            if (!ship.isAlive()) { // isSunk() statt !isAlive() verwenden
                System.out.println("KI hat ein Schiff versenkt!");
                playerBoard.deleteShip(); // Diese Methode muss die Liste im Gamefield aktualisieren
                priorityTargets.clear();
                firstHit = null;
                directionFound = false;
            } else {
                handleHit(x, y);
            }
            return true; // Es war ein Treffer
        } else { // WASSER
            javafx.application.Platform.runLater(() -> cell.setFill(Color.BLACK));
            System.out.println("KI schießt Wasser bei: (" + x + ", " + y + ")");
            return false; // Es war ein Fehlschuss
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

    public int[] getShotCoordinates() {
        // ZERSTÖR-MODUS: Wenn wir bereits ein Schiff getroffen haben
        if (!priorityTargets.isEmpty()) {
            // Nimm das letzte Ziel aus der Liste (LIFO-Prinzip, gut für lineare Suche)
            return priorityTargets.remove(priorityTargets.size() - 1);
        }

        // JAGD-MODUS: Suche ein zufälliges neues Ziel
        // Setze den Treffer-Status zurück, da alle Ziele abgearbeitet wurden.
        firstHit = null;
        directionFound = false;

        int x, y;
        Cell targetCell;

        // Suche so lange, bis ein gültiges, unbeschossenes Feld gefunden wird.
        do {
            // x = Spalte, y = Reihe
            x = rand.nextInt(playerBoard.getBreit()); // Breite = Anzahl Spalten
            y = rand.nextInt(playerBoard.getLang());  // Länge = Anzahl Reihen

            targetCell = playerBoard.getCell(x, y);

        } while (targetCell == null || targetCell.isShot());

        return new int[]{x, y};
    }

}


