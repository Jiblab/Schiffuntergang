package org.example.schiffuntergang;

import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import javafx.scene.paint.Color;
import java.util.*;

/**
 * Implementiert die künstliche Intelligenz (KI) für einen computergesteuerten Gegner.
 * Die KI verwendet eine "Hunt and Target"-Strategie: Sie sucht zufällig nach Schiffen (Hunt-Modus)
 * und wechselt nach einem Treffer in einen gezielten Modus (Target-Modus), um das getroffene
 * Schiff systematisch zu versenken.
 */
public class EnemyPlayer {
    /** Das Spielfeld des menschlichen Spielers, auf das die KI schießt. */
    private final Gamefield playerBoard;
    /** Ein Zufallszahlengenerator für den Jagd-Modus. */
    private final Random rand = new Random();
    /** Eine Liste von Koordinaten, die mit hoher Priorität beschossen werden sollen (Target-Modus). */
    private final List<int[]> priorityTargets = new ArrayList<>();
    /** Speichert die Koordinate des ersten Treffers auf ein neues Schiff. */
    private int[] firstHit = null;
    /** Gibt an, ob die Ausrichtung des getroffenen Schiffs bereits ermittelt wurde. */
    private boolean directionFound = false;

    /**
     * Erstellt eine neue KI-Instanz.
     *
     * @param playerBoard Das Spielfeld des Gegners, auf das diese KI ihre Züge ausführt.
     */
    public EnemyPlayer(Gamefield playerBoard) {
        this.playerBoard = playerBoard;
    }

    /**
     * Führt einen vollständigen Spielzug der KI aus (nur für den Einzelspieler-Modus).
     * Die KI sucht sich ein Ziel und führt den Schuss direkt aus.
     * @deprecated Diese Methode wird im aktuellen Multiplayer-Setup nicht mehr verwendet.
     *             Stattdessen werden {@link #getShotCoordinates()} und {@link #processShotResult(int, int, boolean, boolean)} genutzt.
     */
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

            Cell cellToCheck = playerBoard.getCell(x, y);
            if (cellToCheck != null && !cellToCheck.isShot()) {
                shootAt(x, y);
                break;
            }
        }
    }

    /**
     * Private Hilfsmethode, die den eigentlichen Schuss auf eine Zelle ausführt (nur für Einzelspieler).
     * @param x Die X-Koordinate des Ziels.
     * @param y Die Y-Koordinate des Ziels.
     * @return true bei einem Treffer, false bei Wasser.
     * @deprecated Diese Methode wird nur von der veralteten {@code revenge()}-Methode genutzt.
     */
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

    /**
     * Verarbeitet die Logik nach einem erfolgreichen Treffer.
     * Wechselt vom Jagd- in den Zerstör-Modus und füllt die Prioritäts-Zielliste.
     *
     * @param x Die X-Koordinate des Treffers.
     * @param y Die Y-Koordinate des Treffers.
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
     * Fügt die vier direkten Nachbarn einer Koordinate zur Prioritäts-Zielliste hinzu.
     * Die Reihenfolge wird zufällig gewählt.
     *
     * @param x Die X-Koordinate des initialen Treffers.
     * @param y Die Y-Koordinate des initialen Treffers.
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
     * Fügt eine einzelne Koordinate zur Prioritäts-Zielliste hinzu,
     * aber nur, wenn die Zelle existiert und noch nicht beschossen wurde.
     *
     * @param x Die X-Koordinate des potenziellen Ziels.
     * @param y Die Y-Koordinate des potenziellen Ziels.
     */
    private void addTarget(int x, int y) {
        Cell targetCell = playerBoard.getCell(x, y);
        if (targetCell != null && !targetCell.isShot()) {
            priorityTargets.add(new int[]{x, y});
        }
    }

    /**
     * Ermittelt die Koordinaten für den nächsten Schuss der KI.
     * Diese Methode ist die primäre Schnittstelle für den KiPlayerController.
     * Sie wählt ein Ziel aus der Prioritätsliste oder, falls diese leer ist, ein zufälliges Ziel.
     *
     * @return Ein int-Array der Form {x, y} mit den Zielkoordinaten.
     */
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

    /**
     * Verarbeitet das Ergebnis eines Schusses, das von der MultiplayerLogic gemeldet wird.
     * Diese Methode ist das "Gedächtnis" der KI. Sie aktualisiert die interne Strategie
     * basierend darauf, ob der letzte Schuss ein Treffer oder ein versenktes Schiff war.
     *
     * @param x    Die X-Koordinate des ausgeführten Schusses.
     * @param y    Die Y-Koordinate des ausgeführten Schusses.
     * @param hit  true, wenn der Schuss ein Treffer war.
     * @param sunk true, wenn der Schuss ein Schiff versenkt hat.
     */
    public void processShotResult(int x, int y, boolean hit, boolean sunk) {
        if (sunk) {
            System.out.println("KI-INFO: Gegnerisches Schiff versenkt! Setze Zielliste zurück.");
            priorityTargets.clear();
            firstHit = null;
            directionFound = false;
        } else if (hit) {
            System.out.println("KI-INFO: Treffer bei (" + x + ", " + y + "). Aktualisiere Prioritätsziele.");
            handleHit(x, y);
        }
        // Bei einem Fehlschuss muss nichts getan werden.
    }
}