package org.example.schiffuntergang.Multiplayer;

import javafx.application.Platform;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.components.Gamefield;
import java.io.IOException;
import java.util.Random;

public class KiPlayerController implements Runnable {

    private final MultiplayerLogic logic;
    private final EnemyPlayer ki;
    private final Gamefield playerBoard; // Das eigene Spielfeld
    private final Gamefield enemyBoard;  // Das Spielfeld des Gegners
    private final HelloController uiController;
    private final Random rand = new Random();
    private volatile boolean gameRunning = true;

    public KiPlayerController(MultiplayerLogic logic, EnemyPlayer ki, Gamefield playerBoard, Gamefield enemyBoard, HelloController uiController) {
        this.logic = logic;
        this.ki = ki;
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.uiController = uiController;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            // 1. Schiffe zufällig platzieren
            placeShipsRandomly();
            System.out.println("KI: Schiffe platziert. Sende an Gegner...");

            // 2. "Fertig"-Signal über MultiplayerLogic senden (startet den Handshake)
            logic.kiIsReady();

            // 3. Hauptschleife: Warten, bis die KI am Zug ist
            while (gameRunning) {
                if (logic.getTurn()) {
                    System.out.println("KI-Controller: Ich bin am Zug.");

                    // Kurze Pause für die Optik
                    Thread.sleep(500);

                    // KI entscheidet, wohin sie schießt
                    int[] coords = ki.getShotCoordinates();
                    int x = coords[0];
                    int y = coords[1];

                    System.out.println("KI-Controller: Schieße auf (" + x + ", " + y + ")");

                    // Schuss über MultiplayerLogic auslösen
                    logic.kiShoot(x, y);
                }

                // Kurze Pause, um die CPU nicht zu überlasten
                Thread.sleep(100);

                // Spielende-Bedingung (optional, da die UI dies auch anzeigt)
                if (playerBoard.getShipCount() == 0 || enemyBoard.getShipCount() == 0) {
                    System.out.println("KI-Controller: Spiel beendet.");
                    gameRunning = false;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Fehler im KIPlayerController: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void placeShipsRandomly() {
        double maxCells = playerBoard.maxShipsC();
        if (maxCells <= 0) maxCells = 17; // Standardwert, falls die Berechnung 0 ergibt

        int retries = 0; // KORREKTUR 2: Zähler für Fehlversuche
        final int maxRetries = 500; // Breche nach 500 Fehlversuchen ab

        while (playerBoard.getUsedCells() < maxCells) {
            int shipLength = 2 + rand.nextInt(4);
            boolean vertical = rand.nextBoolean();

            // Überprüfen, ob die gewünschte Schiffslänge überhaupt noch in die verbleibenden Zellen passt.
            if (playerBoard.getUsedCells() + shipLength > maxCells) {
                // Wenn nicht, überspringe diesen Versuch, um kleinere Schiffe zu ermöglichen
                continue;
            }

            int xMax = playerBoard.getBreit() - (vertical ? 1 : shipLength); // Spalten
            int yMax = playerBoard.getLang() - (vertical ? shipLength : 1);  // Reihen

            if (xMax < 0 || yMax < 0) {
                retries++; // Zählt als Fehlversuch
                if (retries > maxRetries) {
                    System.out.println("WARNUNG: Konnte keine passenden Schiffe mehr finden (xMax/yMax < 0). Breche Platzierung ab.");
                    break;
                }
                continue;
            }

            int xPos = rand.nextInt(xMax + 1);
            int yPos = rand.nextInt(yMax + 1);

            // KORREKTUR 1: Überprüfe das Ergebnis von placeShip!
            if (playerBoard.placeShip(new org.example.schiffuntergang.components.Ships(shipLength, shipLength), xPos, yPos, vertical)) {
                // Nur wenn die Platzierung erfolgreich war:
                // Deine Gamefield-Klasse hat vermutlich eine Methode, um den Zähler zu erhöhen.
                // Passe den Namen an, falls er anders lautet.
                playerBoard.increaseCells(shipLength);
                System.out.println("KI hat Schiff der Länge " + shipLength + " platziert. Belegte Zellen: " + playerBoard.getUsedCells());
                retries = 0; // Setze den Zähler bei Erfolg zurück
            } else {
                // KORREKTUR 2: Wenn die Platzierung fehlschlägt, erhöhe den Retry-Zähler.
                retries++;
            }

            // KORREKTUR 2: Wenn zu viele Fehlversuche aufgetreten sind, brich die Schleife ab.
            if (retries > maxRetries) {
                System.out.println("WARNUNG: Maximale Anzahl an Fehlversuchen erreicht. Breche Schiffsplatzierung ab.");
                break;
            }
        }

        playerBoard.redrawAllCells();

        System.out.println("KI-Schiffsplatzierung beendet. Finale belegte Zellen: " + playerBoard.getUsedCells());
        // UI benachrichtigen, dass die Platzierung fertig ist (visuelles Update)
        Platform.runLater(()->{uiController.updateRemainingCellsDisplay();});
    }
}