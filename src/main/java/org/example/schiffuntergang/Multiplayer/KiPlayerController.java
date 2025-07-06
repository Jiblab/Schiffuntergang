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
            // Schritt 1: Schiffe platzieren (unverändert)
            placeShipsRandomly();
            System.out.println("KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Schiffe platziert.");

            // Schritt 2: Den zweiten Handshake und den Start der Game-Loops koordinieren
            if (logic.getClient()) {
                // *** CLIENT-LOGIK ***
                // Der Client muss warten, bis der Server ihm die "ships" schickt.
                // Der einzige Ort, an dem er lauschen kann, ist sein clientGame-Loop.
                // Deshalb starten wir ihn hier.
                logic.startMultiplayerloop();
                System.out.println("[Client-KI] clientGame-Loop gestartet und lauscht auf 'ships'.");

                // Der clientGame-Loop wird jetzt die "ships"-Nachricht empfangen.
                // Im "case ships": muss dann "done" gesendet werden.

            } else {
                // *** SERVER-LOGIK ***
                // Der Server initiiert den Handshake.
                logic.sendShipsAndWaitForDone();
            }

            // Der KiPlayerController-Thread hat seine Aufgabe erfüllt und kann sich beenden.
            // Die Steuerung liegt jetzt vollständig bei den Threads von `startGameFlow` und `clientGame`.
            System.out.println("KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Initialisierung übergeben. Thread beendet sich.");

        } catch (Exception e) {
            System.err.println("Fehler im KiPlayerController.run(): " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void placeShipsRandomly() {
        double maxCells = playerBoard.maxShipsC();
        if (maxCells <= 0) maxCells = 17; // Standardwert, falls die Berechnung 0 ergibt

        int retries = 0; // KORREKTUR 2: Zähler für Fehlversuche
        final int maxRetries = 500; // Breche nach 500 Fehlversuchen ab

        while (playerBoard.getUsedCells() < maxCells && retries <= maxRetries) {
            if (playerBoard.getUsedCells() -1 == maxCells){
                break;
            }
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

    public EnemyPlayer getKi() {
        return ki;
    }
}