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
    private final Gamefield playerBoard;
    private final Gamefield enemyBoard;
    private final HelloController uiController;
    private final Random rand = new Random();
    private final boolean gameRunning = true;

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
            placeShipsRandomly();
            System.out.println("KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Schiffe platziert.");

            if (logic.getClient()) {
                // Der Client muss warten, bis der Server ihm die ships schickt.
                logic.startMultiplayerloop();
                System.out.println("[Client-KI] clientGame-Loop gestartet und lauscht auf 'ships'.");

            } else {
                logic.sendShipsAndWaitForDone();
            }

            System.out.println("KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Initialisierung übergeben. Thread beendet sich.");

        } catch (Exception e) {
            System.err.println("Fehler im KiPlayerController.run(): " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void placeShipsRandomly() {
        double maxCells = playerBoard.maxShipsC();
        if (maxCells <= 0) maxCells = 17;

        int retries = 0;
        final int maxRetries = 500;

        while (playerBoard.getUsedCells() < maxCells && retries <= maxRetries) {
            if (playerBoard.getUsedCells() -1 == maxCells){
                break;
            }
            int shipLength = 2 + rand.nextInt(4);
            boolean vertical = rand.nextBoolean();


            if (playerBoard.getUsedCells() + shipLength > maxCells) {
                continue;
            }

            int xMax = playerBoard.getBreit() - (vertical ? 1 : shipLength); // Spalten
            int yMax = playerBoard.getLang() - (vertical ? shipLength : 1);  // Reihen

            if (xMax < 0 || yMax < 0) {
                retries++;
                if (retries > maxRetries) {
                    System.out.println("WARNUNG: Konnte keine passenden Schiffe mehr finden (xMax/yMax < 0). Breche Platzierung ab.");
                    break;
                }
                continue;
            }

            int xPos = rand.nextInt(xMax + 1);
            int yPos = rand.nextInt(yMax + 1);

            if (playerBoard.placeShip(new org.example.schiffuntergang.components.Ships(shipLength, shipLength), xPos, yPos, vertical)) {

                playerBoard.increaseCells(shipLength);
                System.out.println("KI hat Schiff der Länge " + shipLength + " platziert. Belegte Zellen: " + playerBoard.getUsedCells());
                retries = 0;
            } else {
                retries++;
            }
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