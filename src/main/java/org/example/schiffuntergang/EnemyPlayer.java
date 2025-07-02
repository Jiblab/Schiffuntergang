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

        // Liste für "intelligente" Ziele. Ein Ziel ist ein int-Array {x, y}.
        private final List<int[]> priorityTargets = new ArrayList<>();

        // Speichert den ersten Treffer auf einem neuen Schiff, um die Richtung zu finden.
        private int[] firstHit = null;
        private boolean directionFound = false;

        public EnemyPlayer(Gamefield playerBoard) {
            this.playerBoard = playerBoard;
        }

        /**
         * Hauptmethode für den Zug des Gegners.
         */
        public void revenge() {
            // 1. Priorität: Gezielt ein angeschlagenes Schiff zerstören
            if (!priorityTargets.isEmpty()) {
                // Nimm das letzte Ziel aus der Liste (LIFO-Verhalten wie ein Stack)
                int[] target = priorityTargets.remove(priorityTargets.size() - 1);
                shootAt(target[0], target[1]);
                return; // Zug beenden
            }

            // 2. Priorität: Wenn kein Ziel mehr in der Liste ist, sind wir wieder im Jagd-Modus.
            // Alle Targeting-Variablen zurücksetzen.
            firstHit = null;
            directionFound = false;

            // 3. HUNTING-Modus: Zufälligen, noch nicht beschossenen Punkt finden.
            int x, y;
            do {
                x = rand.nextInt(playerBoard.getBreit());
                y = rand.nextInt(playerBoard.getLang());
            } while (playerBoard.getCell(x, y).isShot());

            shootAt(x, y);
        }

        /**
         * Führt einen Schuss auf die angegebenen Koordinaten aus und aktualisiert den KI-Status.
         *
         * @param x X-Koordinate
         * @param y Y-Koordinate
         */
        private void shootAt(int x, int y) {
            Cell cell = playerBoard.getCell(x, y);
            if (cell.isShot()) return; // Doppelschüsse absolut verhindern

            cell.setShot(true);
            Ships ship = cell.getShip();

            if (ship != null) { // TREFFER!
                ship.hit();
                cell.setFill(Color.RED);
                System.out.println("Gegner trifft bei: (" + x + ", " + y + ")");

                if (!ship.isAlive()) {
                    System.out.println("Gegner hat ein Schiff versenkt!");
                    // Schiff ist versenkt, alle Ziele, die mit diesem Schiff zu tun hatten, sind irrelevant.
                    // Wir löschen die Prioritätsliste und kehren in den Jagd-Modus zurück.
                    priorityTargets.clear();
                    firstHit = null;
                    directionFound = false;
                } else {
                    // Schiff getroffen, aber nicht versenkt. Wir müssen klüger werden.
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
                // Dies ist der ERSTE Treffer auf ein neues Schiff.
                firstHit = new int[]{x, y};
                // Füge alle 4 Nachbarn als potenzielle Ziele hinzu.
                addNeighborsToPriorityList(x, y);
            } else {
                // Dies ist ein ZWEITER oder weiterer Treffer auf dasselbe Schiff.
                // Jetzt kennen wir die Richtung.
                if (!directionFound) {
                    directionFound = true;
                    // Lösche alle alten Ziele, die nicht in der richtigen Richtung liegen.
                    priorityTargets.clear();
                }

                // Bestimme die Achse (horizontal oder vertikal)
                if (x == firstHit[0]) { // Vertikale Achse
                    // Füge die nächsten Zellen oben und unten hinzu
                    addTarget(x, y + 1);
                    addTarget(x, y - 1);
                } else { // Horizontale Achse
                    // Füge die nächsten Zellen links und rechts hinzu
                    addTarget(x + 1, y);
                    addTarget(x - 1, y);
                }
            }
        }

        /**
         * Fügt die vier Nachbarn eines Punktes zur Prioritätsliste hinzu, wenn sie gültig sind.
         */
        private void addNeighborsToPriorityList(int x, int y) {
            List<int[]> neighbors = new ArrayList<>();
            neighbors.add(new int[]{x + 1, y});
            neighbors.add(new int[]{x - 1, y});
            neighbors.add(new int[]{x, y + 1});
            neighbors.add(new int[]{x, y - 1});

            // Mische die Nachbarn, damit die KI nicht vorhersagbar ist (z.B. immer erst rechts probiert)
            Collections.shuffle(neighbors);

            for (int[] neighbor : neighbors) {
                addTarget(neighbor[0], neighbor[1]);
            }
        }

        /**
         * Fügt ein einzelnes Ziel zur Prioritätsliste hinzu, wenn es gültig und unbeschossen ist.
         */
        private void addTarget(int x, int y) {
            if (x >= 0 && x < playerBoard.getBreit() && y >= 0 && y < playerBoard.getLang()) {
                if (!playerBoard.getCell(x, y).isShot()) {
                    priorityTargets.add(new int[]{x, y});
                }
            }
        }

        // Annahme: Ships Klasse hat eine isSunk() Methode
        // public boolean isSunk() { return getHealth() == 0; }
    }


