package org.example.schiffuntergang.Multiplayer;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;
import org.example.schiffuntergang.filemanagement.FileManager;
import org.example.schiffuntergang.filemanagement.GamefieldData;
import org.example.schiffuntergang.filemanagement.GameState;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Die zentrale Logik-Klasse für alle Multiplayer-Interaktionen.
 * Sie verwaltet die Netzwerkverbindung (entweder als Server oder Client),
 * implementiert das Kommunikationsprotokoll und steuert den Spielablauf,
 * indem sie Züge, Schüsse und Ergebnisse zwischen den Spielern synchronisiert.
 * Diese Klasse kann sowohl von menschlichen Spielern als auch vom {@link KiPlayerController} genutzt werden.
 */
public class MultiplayerLogic {
    private Client cl;
    private Server s;
    private final boolean client;
    private Gamefield enemy;
    private Gamefield player;
    private int x;
    private int y;
    private HelloController contr;
    private boolean myturn;
    private boolean firstturn = true;
    private boolean serverKiReady = false;
    private boolean clientKiReady = false;
    private KiPlayerController kicontr;
    private int[][] shotEnemyPositions;
    private int maxShoot;

    /**
     * Konstruktor für eine Multiplayer-Instanz, die als Client agiert.
     *
     * @param c       Die {@link Client}-Instanz für die Netzwerkverbindung.
     * @param client1 Muss true sein, um den Client-Modus zu kennzeichnen.
     * @param en      Das Spielfeld des Gegners (kann anfangs null sein).
     * @param pl      Das Spielfeld des eigenen Spielers (kann anfangs null sein).
     */
    public MultiplayerLogic(Client c, boolean client1, Gamefield en, Gamefield pl){
        cl = c;
        player = pl;
        enemy = en;
        client = client1;
        myturn = false;
    }

    /**
     * Konstruktor für eine Multiplayer-Instanz, die als Server agiert.
     *
     * @param se      Die {@link Server}-Instanz für die Netzwerkverbindung.
     * @param client1 Muss false sein, um den Server-Modus zu kennzeichnen.
     * @param en      Das Spielfeld des Gegners.
     * @param pl      Das Spielfeld des eigenen Spielers.
     */
    public MultiplayerLogic(Server se, boolean client1, Gamefield en, Gamefield pl){
        s = se;
        player = pl;
        enemy = en;
        client = client1;
        myturn = true;
    }

    /**
     * Initiiert den Handshake zum Senden der Schiffe an den Gegner.
     * Diese Methode wird typischerweise von einem menschlichen Spieler durch einen Button-Klick ausgelöst.
     * @throws IOException bei Netzwerkfehlern.
     */
    public void sendShips() throws IOException {
        if (client){
            maxShoot = player.getUsedCells();
            startMultiplayerloop();
        }
        else {
            if(firstturn){
                int[] shipLengths = player.getShipLengths();
                s.sendShips(shipLengths);
                maxShoot = player.getUsedCells();
                System.out.println("[MultiplayerLogic] Schiffe gesendet");
                String messagedone = s.receiveMessage();
                System.out.println(messagedone);
                if (messagedone.contains("done")){
                    s.sendReady();
                    System.out.println("[MultiplayerLogic] Ready sent!");
                    startMultiplayerloop();
                }
            }else{
                startMultiplayerloop();
            }
        }
    }

    /**
     * Die Haupt-Schleife für den Server, die auf Nachrichten vom Client lauscht und darauf reagiert.
     * Wenn der Server am Zug ist, wartet er auf eine Aktion (entweder von der KI oder einem Menschen).
     * @throws IOException bei Netzwerkfehlern.
     */
    private void startGameFlow() throws IOException {
        if(!client) {
            String yeye = s.receiveMessage();
            System.out.println(yeye);
            if (firstturn){
                if (yeye.equals("ready")) {
                    firstturn = false;
                }
            }
            while (true) {
                if (!myturn) {
                    String m = s.receiveMessage();
                    String[] p = m.split(" ");
                    switch (p[0]) {
                        case "shot":
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (c.isShot()) {
                                s.sendAnswer(0);
                                System.out.println("[MultiplayerLogic] Schon getroffen, sende 0");
                            } else {
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null) {
                                    s.sendAnswer(0);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 0 (Miss)");
                                } else if (!c.getShip().isAlive()) {
                                    maxShoot--;
                                    if (maxShoot <= 0 && kicontr == null){
                                        s.send("won");
                                        Platform.runLater(()-> contr.showGameOverScreen(false));
                                    }
                                    player.deleteShip();
                                    s.sendAnswer(2);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 2 (Sunk)");
                                } else {
                                    maxShoot--;
                                    if (maxShoot <= 0 && kicontr == null){
                                        s.send("won");
                                        Platform.runLater(()-> contr.showGameOverScreen(false));
                                    }
                                    s.sendAnswer(1);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 1 (Hit)");
                                }
                            }
                            break;
                        case "load":
                            long saveId = Long.parseLong(p[1]);
                            try{
                                FileManager fm = new FileManager(false);
                                GameState loadedstate = fm.loadfromid(saveId);
                                loadGameFromSave(loadedstate, null);
                            }catch(Exception e){
                                System.err.println("[MultiplayerLogic] Fehler beim Verarbeiten des Loads"+e.getMessage());
                            }
                            break;
                        case "answer":
                            Cell ce = enemy.getCell(x, y);
                            if (p[1].equals("0")) {
                                Platform.runLater(() -> ce.setFill(Color.BLACK));
                                System.out.println("[MultiplayerLogic] Fehlschuss. Sende pass.");
                                s.sendPass();
                            } else {
                                Platform.runLater(() -> ce.setFill(Color.RED));
                                ce.setShipHit(true);
                                System.out.println("[MultiplayerLogic] Treffer/Versenkt. Ich bin wieder dran.");
                                myturn = true;
                            }
                            if (kicontr != null) {
                                boolean wasHit = (p[1].equals("1")  || p[1].equals("2") );
                                boolean wasSunk = (p[1].equals("2"));
                                kicontr.getKi().processShotResult(x, y, wasHit, wasSunk);
                            }
                            break;
                        case "pass":
                            System.out.println("[MultiplayerLogic] habe pass bekommen");
                            myturn = true;
                            break;
                        case "save":
                            try {
                                System.out.println("\n[MultiplayerLogic] Remote-Speicherbefehl vom Gegner empfangen.");
                                long id = Long.parseLong(p[1]);
                                String filename = "mp_save_" + id + ".save";
                                System.out.println("[MultiplayerLogic] Verarbeite Speicher-ID: " + id);
                                GamefieldData playerData = player.toData();
                                GamefieldData enemyData = enemy.toData();
                                GameState remoteSaveState = new GameState();
                                remoteSaveState.setPlayerBoardData(playerData);
                                remoteSaveState.setEnemyBoardData(enemyData);
                                remoteSaveState.setMultiplayer(true, id);
                                remoteSaveState.setPlayerTurn(myturn);
                                if (player != null) {
                                    remoteSaveState.setMusikAktiv(player.isMusicEnabled());
                                    remoteSaveState.setMusikVolume(player.getMusicVolume());
                                }
                                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                String json = gson.toJson(remoteSaveState);
                                System.out.println("[MultiplayerLogic] Versuche, das Spiel lokal nach Remote-Befehl zu speichern...");
                                FileManager fm = new FileManager(false);
                                fm.saveGameData(json, filename);
                                Platform.runLater(() -> contr.showNotification("Spiel vom Gegner gespeichert!", "info"));
                            } catch (Exception e) {
                                System.err.println("[MultiplayerLogic] Fehler beim Verarbeiten des Remote-Save-Befehls: " + e.getMessage());
                            }
                            break;
                        case "won":
                            Platform.runLater(()-> contr.showGameOverScreen(true));
                            break;
                    }
                } else {
                    if (kicontr != null) {
                        try {
                            Thread.sleep(500);
                            int[] coords = kicontr.getKi().getShotCoordinates();
                            System.out.println("[MultiplayerLogic] KI-LOGIC: Schieße auf (" + coords[0] + ", " + coords[1] + ")");
                            kiShoot(coords[0], coords[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }

    /**
     * Lädt einen Spielzustand aus einem {@link GameState}-Objekt.
     * Rekonstruiert die Spielfelder und stellt den Spielzug wieder her.
     *
     * @param loadedState Das zu ladende Spielzustands-Objekt.
     * @param ctr Die Referenz zum Haupt-Controller.
     * @throws IOException bei Fehlern.
     */
    public void loadGameFromSave(GameState loadedState, HelloController ctr) throws IOException {
        if(ctr != null){
            setController(ctr);
        }
        if(s != null){
            s.sendLoad(loadedState.getId());
        }

        player = Gamefield.fromData(loadedState.getPlayerBoardData(), this.contr, this);
        enemy = Gamefield.fromData(loadedState.getEnemyBoardData(), this.contr, this);

        player.setLogic(this);
        enemy.setLogic(this);
        setPl(this.player);
        setEn(this.enemy);
        setTurn(loadedState.isPlayerTurn());

        contr.setup(this.player, this.enemy);

        BackgroundMusic.getInstance().setVolume(loadedState.getMusikVolume());
        if (!loadedState.isMusikAktiv()) {
            BackgroundMusic.getInstance().stop();
        }
        firstturn = false;
        if(client){
            cl.send("ok");
        }
    }

    /**
     * Startet die anfängliche Netzwerk-Initialisierung.
     * Der Server öffnet einen Port und wartet. Der Client verbindet sich.
     * Führt den ersten Handshake zum Austausch der Spielfeldgröße durch.
     * @throws IOException bei Netzwerkfehlern.
     */
    public void start() throws IOException {
        if (!client){
            s.start(5000);
            System.out.println("[MultiplayerLogic] Multiplayer connected!");
            s.sendSize(player.getBreit(), player.getLang());
            System.out.println("[MultiplayerLogic] Server: Größen geschickt mit Werten von: "+player.getBreit()+" "+player.getLang());
            String message = s.receiveMessage();
            System.out.println("[MultiplayerLogic] "+message);
            System.out.println("[MultiplayerLogic] While schleife fürs warten verlassen");
        }
        else {
            cl.connect(contr.getIP(), contr.getPort());
            System.out.println("[MultiplayerLogic] Nachricht wurde gesplitet");
            int rows = 0;
            int cols = 0;
            for (int i = 0; i <= 1; i++) {
                String m1 = cl.receiveMessage();
                System.out.println(m1);
                String[] p1 = m1.split(" ");

                switch (p1[0]) {
                    case "size":
                        System.out.println("[MultiplayerLogic] bin in case size");
                        rows = Integer.parseInt(p1[1]);
                        cols = Integer.parseInt(p1[2]);
                        player = new Gamefield(false, contr, cols, rows, this);
                        enemy = new Gamefield(true, contr, cols, rows, this);
                        Platform.runLater(() -> {
                            contr.setupMultiplayerBoards(player, enemy);
                        });
                        cl.sendDone();
                        break;
                    case "load":
                        Platform.runLater(() -> {
                            System.out.println("Running on the JavaFX Application Thread.");
                            long saveId = Long.parseLong(p1[1]);
                            try{
                                FileManager fm = new FileManager(false);
                                GameState loadedstate = fm.loadfromid(saveId);
                                loadGameFromSave(loadedstate, null);
                            }catch(Exception e){
                                System.err.println("Fehler beim Verarbeiten des Loads: "+e.getMessage());
                            }
                        });
                        break;
                    case "ships":
                        int[] receivedShipCounts = new int[6];
                        for (int j = 1; j < p1.length; j++) {
                            int len = Integer.parseInt(p1[j]);
                            if (len < receivedShipCounts.length) {
                                receivedShipCounts[len]++;
                            }
                        }
                        final int[] finalCounts = receivedShipCounts;
                        Platform.runLater(() -> {
                            contr.setShipRules(finalCounts);
                            contr.setupClientPlacementUI(finalCounts);
                        });
                        maxShoot = player.getUsedCells();
                        cl.sendDone();
                        break;
                }
            }
        }
    }

    /**
     * Die Haupt-Schleife für den Client, die auf Nachrichten vom Server lauscht und darauf reagiert.
     * Wenn der Client am Zug ist, wartet er auf eine Aktion (entweder von der KI oder einem Menschen).
     * @throws IOException bei Netzwerkfehlern.
     */
    public void clientGame() throws IOException {
        if (firstturn){
            String f = cl.receiveMessage();
            if (f.equals("ready")) {
                System.out.println("[MultiplayerLogic] hab ready bekommen");
                cl.sendReady();
                firstturn = false;
            }
        }
        while (true) {
            if (!myturn) {
                String m = cl.receiveMessage();
                String[] p = m.split(" ");
                switch (p[0]) {
                    case "shot":
                        Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                        if (c.isShot()) {
                            cl.sendAnswer(0);
                            System.out.println("[MultiplayerLogic] Schon getroffen, sende 0");
                        } else {
                            player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (c.getShip() == null) {
                                cl.sendAnswer(0);
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 0 (Miss)");
                            } else if (!c.getShip().isAlive()) {
                                maxShoot--;
                                if (maxShoot <= 0 && kicontr == null){
                                    cl.send("won");
                                    Platform.runLater(()-> contr.showGameOverScreen(false));
                                }
                                player.deleteShip();
                                cl.sendAnswer(2);
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 2 (Sunk)");
                            } else {
                                maxShoot--;
                                if (maxShoot <= 0 && kicontr == null){
                                    cl.send("won");
                                    Platform.runLater(()-> contr.showGameOverScreen(false));
                                }
                                cl.sendAnswer(1);
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 1 (Hit)");
                            }
                        }
                        break;
                    case "answer":
                        Cell ce = enemy.getCell(x, y);
                        if (p[1].equals("0")) {
                            Platform.runLater(() -> ce.setFill(Color.BLACK));
                            System.out.println("[MultiplayerLogic] Fehlschuss. Sende pass.");
                            cl.sendPass();
                        } else {
                            Platform.runLater(() -> ce.setFill(Color.RED));
                            ce.setShipHit(true);
                            System.out.println("[MultiplayerLogic] Treffer/Versenkt. Ich bin wieder dran.");
                            myturn = true;
                        }
                        if (kicontr != null) {
                            boolean wasHit = (p[1].equals("1")  || p[1].equals("2") );
                            boolean wasSunk = (p[1].equals("2"));
                            kicontr.getKi().processShotResult(x, y, wasHit, wasSunk);
                        }
                        break;
                    case "pass":
                        System.out.println("[MultiplayerLogic] habe pass bekommen");
                        myturn = true;
                        break;
                    case "ships":
                        System.out.println("[MultiplayerLogic] [Client] 'ships' empfangen.");
                        if (kicontr != null) {
                            System.out.println("[MultiplayerLogic] [Client-KI] Sende 'done' als Antwort auf 'ships'.");
                            cl.sendDone();
                        }
                        break;
                    case "ready":
                        System.out.println("[MultiplayerLogic] [Client] 'ready' empfangen. Spiel kann beginnen.");
                        firstturn = false;
                        break;
                    case "save":
                        try {
                            System.out.println("\n[MultiplayerLogic] Remote-Speicherbefehl vom Gegner empfangen.");
                            long saveId = Long.parseLong(p[1]);
                            String filename = "mp_save_" + saveId + ".save";
                            System.out.println("[MultiplayerLogic] Verarbeite Speicher-ID: " + saveId);
                            GamefieldData playerData = player.toData();
                            GamefieldData enemyData = enemy.toData();
                            GameState remoteSaveState = new GameState();
                            remoteSaveState.setPlayerBoardData(playerData);
                            remoteSaveState.setEnemyBoardData(enemyData);
                            remoteSaveState.setMultiplayer(true, saveId);
                            remoteSaveState.setPlayerTurn(myturn);
                            if (player != null) {
                                remoteSaveState.setMusikAktiv(player.isMusicEnabled());
                                remoteSaveState.setMusikVolume(player.getMusicVolume());
                            }
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            String json = gson.toJson(remoteSaveState);
                            System.out.println("[MultiplayerLogic] Versuche, das Spiel lokal nach Remote-Befehl zu speichern...");
                            FileManager fm = new FileManager(false);
                            fm.saveGameData(json, filename);
                            Platform.runLater(() -> contr.showNotification("Spiel vom Gegner gespeichert!", "info"));
                        } catch (Exception e) {
                            System.err.println("[MultiplayerLogic] Fehler beim Verarbeiten des Remote-Save-Befehls: " + e.getMessage());
                        }
                        break;
                    case "won":
                        Platform.runLater(()-> contr.showGameOverScreen(true));
                        break;
                    default:
                        System.out.println("Iwas stimmt nicht!!");
                        break;
                }
            } else {
                if (kicontr != null) {
                    try {
                        Thread.sleep(500);
                        int[] coords = kicontr.getKi().getShotCoordinates();
                        System.out.println("[MultiplayerLogic] KI-LOGIC: Schieße auf (" + coords[0] + ", " + coords[1] + ")");
                        kiShoot(coords[0], coords[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /** Setzt die X-Koordinate für den nächsten Schuss. @param x1 die Spalten-Koordinate. */
    public void setX(int x1){ x = x1; }
    /** Setzt die Y-Koordinate für den nächsten Schuss. @param y1 die Reihen-Koordinate. */
    public void setY(int y1){ y = y1; }
    /** Setzt die Referenz auf das gegnerische Spielfeld. @param en das {@link Gamefield} des Gegners. */
    public void setEn(Gamefield en){ this.enemy = en; }
    /** Setzt die Referenz auf das eigene Spielfeld. @param pl das eigene {@link Gamefield}. */
    public void setPl(Gamefield pl){ this.player = pl; }
    /** Setzt die Referenz zum Haupt-Controller. @param c der {@link HelloController}. */
    public void setController(HelloController c ){ this.contr = c; }
    /** Gibt zurück, ob diese Instanz ein Client ist. @return true, wenn Client, sonst false. */
    public boolean getClient(){ return client; }

    /**
     * Sendet die "shot"-Nachricht an den Gegner und markiert den eigenen Zug als beendet.
     * @throws IOException bei Netzwerkfehlern.
     */
    public void startShoot() throws IOException {
        if (!client){
            s.sendShot(x, y);
            enemy.getCell(x,y).setShot(true);
            myturn = false;
            System.out.println("[MultiplayerLogic] "+x+" "+y);
        }
        else {
            cl.sendShot(x, y);
            enemy.getCell(x,y).setShot(true);
            myturn = false;
        }
    }

    /** Gibt zurück, ob der aktuelle Spieler am Zug ist. @return der Zugstatus. */
    public boolean getTurn(){ return myturn; }

    /**
     * Startet die entsprechende Game-Loop (entweder {@code startGameFlow} oder {@code clientGame})
     * in einem neuen Daemon-Thread, um die Hauptanwendung nicht zu blockieren.
     * @throws IOException bei Fehlern.
     */
    public void startMultiplayerloop() throws IOException {
        if (!client){
            Thread t = new Thread(() -> {
                try {
                    startGameFlow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
        }
        else {
            Thread t = new Thread(() -> {
                try {
                    clientGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    /** Sendet einen Befehl zum Speichern des Spiels an den Gegner. @param id die Spielstands-ID. */
    public void sendSaveCommand(long id) throws IOException {
        if (client) cl.sendSave(id); else s.sendSave(id);
    }

    /**
     * Setzt den Zugstatus direkt. Wird hauptsächlich beim Laden eines Spiels verwendet.
     * @param isMyTurn true, wenn dieser Spieler am Zug ist, andernfalls false.
     */
    public void setTurn(boolean isMyTurn) {
        this.myturn = isMyTurn;
        System.out.println("[MultiplayerLogic] Spielzug nach Laden gesetzt: " + (isMyTurn ? "Ich bin dran." : "Gegner ist dran."));
    }

    /**
     * Löst einen Schuss für die KI aus.
     * @param targetX Die Ziel-Spalte.
     * @param targetY Die Ziel-Reihe.
     * @throws IOException bei Netzwerkfehlern.
     */
    public void kiShoot(int targetX, int targetY) throws IOException {
        if (!myturn) {
            System.err.println("[MultiplayerLogic] WARNUNG: KI versucht zu schießen, obwohl sie nicht am Zug ist.");
            return;
        }
        this.x = targetX;
        this.y = targetY;
        startShoot();
    }

    /**
     * Veraltete Methode zur Synchronisation der KI-Bereitschaft.
     * @deprecated Diese Methode wurde durch einen robusteren Handshake-Ablauf ersetzt.
     */
    public synchronized void kiIsReady() {
        try {
            if (client) {
                clientKiReady = true;
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Client-KI ist bereit.");
                cl.sendDone();
            } else {
                serverKiReady = true;
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Server-KI ist bereit.");
                sendShipsForKi();
            }
            if (serverKiReady && clientKiReady) {
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Beide KIs sind bereit. Starte Spiel-Loop.");
                s.sendReady();
                startMultiplayerloop();
            }
        } catch (IOException e) {
            System.err.println("[MultiplayerLogic] Fehler in kiIsReady: " + e.getMessage());
        }
    }

    /**
     * Veraltete Methode zum Senden der Schiffe für die KI.
     * @deprecated Diese Methode ist nicht mehr Teil des aktuellen Handshake-Prozesses.
     */
    private void sendShipsForKi() throws IOException {
        if (!client) {
            int[] shipLengths = player.getShipLengths();
            s.sendShips(shipLengths);
            maxShoot = player.getUsedCells();
            System.out.println("[MultiplayerLogic] Server: Schiffe an Client-KI gesendet.");
        }
    }

    /** Setzt die Referenz zum KI-Controller. @param ki der {@link KiPlayerController}. */
    public void setKicontroler(KiPlayerController ki){ kicontr = ki; }
    /** Gibt das {@link Client}-Objekt zurück. @return die Client-Instanz. */
    public Client getClientObj() { return this.cl; }

    /**
     * Führt den zweiten Teil des Handshakes für den Server aus:
     * Sendet die Schiffe, wartet auf die "done"-Bestätigung und startet dann die Game-Loops.
     * @throws IOException bei Netzwerkfehlern.
     */
    public void sendShipsAndWaitForDone() throws IOException {
        if (client) return;
        int[] shipLengths = player.getShipLengths();
        s.sendShips(shipLengths);
        System.out.println("[MultiplayerLogic] [Server] 'ships' gesendet. Warte auf 'done'...");
        String messagedone = s.receiveMessage();
        System.out.println("[MultiplayerLogic] [Server] Nachricht empfangen: " + messagedone);
        if (messagedone.contains("done")) {
            s.sendReady();
            System.out.println("[MultiplayerLogic] [Server] 'Ready' gesendet. Starte Game-Loop.");
            startMultiplayerloop();
        }
    }

    /**
     * Setzt die maximale Anzahl an Schüssen, die zum Gewinnen benötigt werden.
     * Dies entspricht der Gesamtanzahl der gegnerischen Schiffszellen.
     * @param maxShoot Die Anzahl der zu treffenden Zellen.
     */
    public void setMaxShoot(int maxShoot) { this.maxShoot = maxShoot; }
}