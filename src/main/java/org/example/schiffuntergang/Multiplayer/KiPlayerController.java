package org.example.schiffuntergang.Multiplayer;

import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.components.Gamefield;
import java.util.Random;
import javafx.application.Platform;

/**
 * Steuert eine Spielinstanz, die von einer KI gespielt wird, im Multiplayer-Modus.
 * Diese Klasse ist verantwortlich für die Automatisierung der Aktionen, die normalerweise
 * ein menschlicher Spieler durchführen würde, wie das Platzieren von Schiffen und das
 * Auslösen des nächsten Spielzugs.
 * Sie wird in einem eigenen Thread ausgeführt, um die Hauptanwendung nicht zu blockieren.
 */
public class KiPlayerController implements Runnable {
    /** Die Multiplayer-Logik, die für die Netzwerkkommunikation zuständig ist. */
    private final MultiplayerLogic logic;
    /** Die Instanz der KI, die die Spielentscheidungen trifft. */
    private final EnemyPlayer ki;
    /** Das Spielfeld der eigenen KI. */
    private final Gamefield playerBoard;
    /** Das Spielfeld der gegnerischen KI. */
    private final Gamefield enemyBoard;
    /** Der Haupt-Controller für UI-Aktualisierungen. */
    private final HelloController uiController;
    /** Ein Zufallszahlengenerator für die Schiffsplatzierung. */
    private final Random rand = new Random();
    private final boolean gameRunning = true;

    /**
     * Erstellt einen neuen KI-Controller.
     *
     * @param logic         Die Instanz der {@link MultiplayerLogic}.
     * @param ki            Die Instanz des {@link EnemyPlayer}.
     * @param playerBoard   Das Spielfeld der eigenen KI.
     * @param enemyBoard    Das Spielfeld der gegnerischen KI.
     * @param uiController  Der {@link HelloController} der Anwendung.
     */
    public KiPlayerController(MultiplayerLogic logic, EnemyPlayer ki, Gamefield playerBoard, Gamefield enemyBoard, HelloController uiController) {
        this.logic = logic;
        this.ki = ki;
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.uiController = uiController;
    }

    /**
     * Startet den KI-Controller in einem neuen, separaten Daemon-Thread.
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Die Hauptmethode des KI-Controller-Threads.
     * Führt die Initialisierungssequenz aus: platziert Schiffe und startet den
     * Multiplayer-Handshake sowie die Game-Loops. Nach der Initialisierung beendet sich dieser Thread,
     * da die weitere Steuerung von den Game-Loops in {@link MultiplayerLogic} übernommen wird.
     */
    @Override
    public void run() {
        try {
            placeShipsRandomly();
            System.out.println("[KiPlayerController] KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Schiffe platziert.");

            if (logic.getClient()) {
                // Der Client startet seinen Game-Loop, um auf die "ships"-Nachricht vom Server zu warten.
                logic.startMultiplayerloop();
                System.out.println("[KiPlayerController] [Client-KI] clientGame-Loop gestartet und lauscht auf 'ships'.");

            } else {
                // Der Server initiiert den Handshake, der blockiert, bis der Client antwortet.
                logic.sendShipsAndWaitForDone();
            }

            System.out.println("[KiPlayerController] KI-Controller [" + (logic.getClient() ? "Client" : "Server") + "]: Initialisierung übergeben. Thread beendet sich.");

        } catch (Exception e) {
            System.err.println("[KiPlayerController] Fehler im KiPlayerController.run(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Platziert zufällig Schiffe auf dem Spielfeld der KI, bis das definierte Limit erreicht ist.
     * Die Methode enthält einen Sicherheitsmechanismus, um Endlosschleifen zu verhindern,
     * falls das Feld zu voll wird, um weitere Schiffe zu platzieren.
     */
    private void placeShipsRandomly() {
        double maxCells = playerBoard.maxShipsC();
        if (maxCells <= 0) maxCells = 17;

        int retries = 0;
        final int maxRetries = 500;

        while (playerBoard.getUsedCells() < maxCells-1) {
            if (playerBoard.getUsedCells()-1 == maxCells -1 || playerBoard.getUsedCells() == maxCells){
                break;
            }
            int shipLength = 2 + rand.nextInt(4);
            boolean vertical = rand.nextBoolean();

            if (playerBoard.getUsedCells() + shipLength > maxCells) {
                continue;
            }

            int xMax = playerBoard.getLang() - (vertical ? 1 : shipLength); // Spalten
            int yMax = playerBoard.getBreit() - (vertical ? shipLength : 1);  // Reihen

            if (xMax < 0 || yMax < 0) {
                retries++;
                if (retries > maxRetries) {
                    System.out.println("[KiPlayerController] WARNUNG: Konnte keine passenden Schiffe mehr finden (xMax/yMax < 0). Breche Platzierung ab.");
                    break;
                }
                continue;
            }

            int xPos = rand.nextInt(xMax + 1);
            int yPos = rand.nextInt(yMax + 1);

            if (playerBoard.placeShip(new org.example.schiffuntergang.components.Ships(shipLength, shipLength), xPos, yPos, vertical)) {

                playerBoard.increaseCells(shipLength);
                System.out.println("[KiPlayerController] KI hat Schiff der Länge " + shipLength + " platziert. Belegte Zellen: " + playerBoard.getUsedCells());
                retries = 0;
            } else {
                retries++;
            }
            if (retries > maxRetries) {
                System.out.println("[KiPlayerController] WARNUNG: Maximale Anzahl an Fehlversuchen erreicht. Breche Schiffsplatzierung ab.");
                break;
            }
        }

        playerBoard.redrawAllCells();

        System.out.println("[KiPlayerController] KI-Schiffsplatzierung beendet. Finale belegte Zellen: " + playerBoard.getUsedCells());
        // UI benachrichtigen, dass die Platzierung fertig ist (visuelles Update)
        Platform.runLater(()->{uiController.updateRemainingCellsDisplay();});
    }

    /**
     * Gibt die Instanz der KI zurück, die von diesem Controller verwaltet wird.
     *
     * @return Das {@link EnemyPlayer}-Objekt.
     */
    public EnemyPlayer getKi() {
        return ki;
    }
}

