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


    public MultiplayerLogic(Client c, boolean client1, Gamefield en, Gamefield pl){
        cl = c;
        player = pl;
        enemy = en;
        client = client1;
        myturn = false;
    }
    public MultiplayerLogic(Server se, boolean client1, Gamefield en, Gamefield pl){
        s = se;
        player = pl;
        enemy = en;
        client = client1;
        myturn = true;
    }

    public void sendShips() throws IOException {
        if (client){
            startMultiplayerloop();
        }
        else {
            int[] shipLengths = player.getShipLengths();
            s.sendShips(shipLengths);
            System.out.println("[MultiplayerLogic] Schiffe gesendet");
            String messagedone = s.receiveMessage();
            System.out.println(messagedone);
            if (messagedone.contains("done")){
                s.sendReady();
                System.out.println("[MultiplayerLogic] Ready sent!");
                startMultiplayerloop();
            }
        }
    }
    private void startGameFlow() throws IOException {
        if(!client) { // This check is good
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
                        case "shot": // Der Client hat auf uns geschossen
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));

                            //Zelle wurde bereits beschossen
                            if (c.isShot()) {
                                s.sendAnswer(0);
                                System.out.println("[MultiplayerLogic] Schon getroffen, sende 0");
                            } else {
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null) { // Fehlschuss
                                    s.sendAnswer(0);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 0 (Miss)");
                                } else if (!c.getShip().isAlive()) { // Versenkt
                                    player.deleteShip();
                                    s.sendAnswer(2);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 2 (Sunk)");
                                } else { // Treffer
                                    s.sendAnswer(1);
                                    System.out.println("[MultiplayerLogic] Antwort gesendet: 1 (Hit)");
                                }
                            }
                            break;

                        case "answer": // Wir haben eine Antwort auf unseren Schuss erhalten
                            Cell ce = enemy.getCell(x, y);
                            if (p[1].equals("0")) { // Fehlschuss
                                Platform.runLater(() -> ce.setFill(Color.BLACK));
                                // Unser Zug ist vorbei -> übergeben mit "pass"
                                System.out.println("[MultiplayerLogic] Fehlschuss. Sende pass.");
                                s.sendPass();
                            } else { // Treffer (1) oder versenkt (2)
                                Platform.runLater(() -> ce.setFill(Color.RED));
                                ce.setShipHit(true);
                                //immer noch unser Zug -> myturn wieder auf true.
                                System.out.println("[MultiplayerLogic] Treffer/Versenkt. Ich bin wieder dran.");
                                myturn = true;
                            }
                            break;

                        case "pass": // Der Client übergibt den Zug an uns
                            System.out.println("[MultiplayerLogic] habe pass bekommen");
                            myturn = true; // Jetzt sind wir dran
                            break;
                        //andere cases
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
                    }
                } else {
                    if (kicontr != null) { // Prüfen, ob eine KI dieses Spiel steuert
                        try {
                            // Kurze Pause für die Optik
                            Thread.sleep(500);

                            // KI entscheidet und schießt
                            int[] coords = kicontr.getKi().getShotCoordinates();
                            System.out.println("[MultiplayerLogic] KI-LOGIC: Schieße auf (" + coords[0] + ", " + coords[1] + ")");
                            kiShoot(coords[0], coords[1]);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Wenn es keine KI ist, warte einfach (auf Klick des Menschen)
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
        startMultiplayerloop();
    }
    public void start() throws IOException {
        if (!client){ //man selber ist host
            s.start(5000);
            System.out.println("[MultiplayerLogic] Multiplayer connected!");
            s.sendSize(player.getBreit(), player.getLang());
            System.out.println("[MultiplayerLogic] Server: Größen geschickt mit Werten von: "+player.getBreit()+" "+player.getLang());
            String message = s.receiveMessage();
            System.out.println("[MultiplayerLogic] "+message);

            System.out.println("[MultiplayerLogic] While schleife fürs warten verlassen");
        }
        else {// man ist client und joined
            cl.connect(contr.getIP(), contr.getPort());
            boolean temp = true;

            System.out.println("[MultiplayerLogic] Nachricht wurde gesplitet");
            int rows = 0;
            int cols = 0;
            for (int i = 0; i <= 1; i++) {
                String m1 = cl.receiveMessage();
                System.out.println(m1);
                String[] p1 = m1.split(" ");

                switch (p1[0]) {
                    /*case "done":
                        cl.sendDone();
                        temp = false;
                        break;*/
                    case "size":
                        System.out.println("[MultiplayerLogic] bin in case size");
                        rows = Integer.parseInt(p1[1]);
                        cols = Integer.parseInt(p1[2]);
                        player = new Gamefield(false, contr, cols, rows, this);
                        enemy = new Gamefield(true, contr, cols, rows, this);

                        Platform.runLater(() -> {
                            contr.setupMultiplayerBoards(player, enemy);
                        });

                        cl.sendDone(); // Antwort an den Server
                        break;
                    case "load":
                        //lade ID
                        Platform.runLater(() -> {
                            // Code to be executed on the JavaFX Application Thread
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

                        cl.sendDone();
                        break;
                }
            }
        }
    }
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
            // Nur auf Nachrichten lauschen, wenn man nicht am Zug ist
            if (!myturn) {
                String m = cl.receiveMessage();
                String[] p = m.split(" ");
                switch (p[0]) {
                    case "shot": // Der Server hat auf uns geschossen
                        Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));

                        // Fall 1: Zelle wurde bereits beschossen
                        if (c.isShot()) {
                            // Sende "miss", da es keine Auswirkung hat. Der Server ist weiterhin dran.
                            cl.sendAnswer(0);
                            System.out.println("[MultiplayerLogic] Schon getroffen, sende 0");
                        } else {
                            // Ansonsten, schieße auf die Zelle
                            player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (c.getShip() == null) { // Fehlschuss
                                cl.sendAnswer(0);
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 0 (Miss)");
                            } else if (!c.getShip().isAlive()) { // Versenkt
                                player.deleteShip();
                                cl.sendAnswer(2);
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 2 (Sunk)");
                            } else { // Treffer
                                cl.sendAnswer(1);
                                // WICHTIG: myturn wird hier NICHT geändert. Der Server ist weiterhin dran.
                                System.out.println("[MultiplayerLogic] Antwort gesendet: 1 (Hit)");
                            }
                        }
                        break;

                    case "answer": // Wir haben eine Antwort auf unseren Schuss erhalten
                        Cell ce = enemy.getCell(x, y);
                        if (p[1].equals("0")) { // Fehlschuss
                            Platform.runLater(() -> ce.setFill(Color.BLACK));

                            // Unser Zug ist vorbei. Wir übergeben mit "pass".
                            System.out.println("[MultiplayerLogic] Fehlschuss. Sende pass.");
                            cl.sendPass();
                        } else { // Treffer (1) oder Versenkt (2)
                            Platform.runLater(() -> ce.setFill(Color.RED));
                            // Es ist immer noch unser Zug! Wir müssen myturn wieder auf true setzen,
                            // damit die UI einen neuen Schuss erlaubt.
                            ce.setShipHit(true);
                            System.out.println("[MultiplayerLogic] Treffer/Versenkt. Ich bin wieder dran.");
                            myturn = true;
                        }
                        break;

                    case "pass": // Der Server übergibt den Zug an uns
                        System.out.println("[MultiplayerLogic] habe pass bekommen");
                        myturn = true; // Jetzt sind wir dran
                        break;

                    case "ships":
                        System.out.println("[MultiplayerLogic] [Client] 'ships' empfangen.");
                        // Der KiPlayerController des Clients hat bereits seine Schiffe platziert
                        // und wartet quasi darauf, dass diese Nachricht ankommt.
                        // Jetzt, wo sie da ist, kann er "done" senden.
                        if (kicontr != null) { // Wenn es eine KI ist
                            System.out.println("[MultiplayerLogic] [Client-KI] Sende 'done' als Antwort auf 'ships'.");
                            cl.sendDone();
                        }
                        break;

                    case "ready":
                        System.out.println("[MultiplayerLogic] [Client] 'ready' empfangen. Spiel kann beginnen.");
                        firstturn = false; // Spiel hat begonnen
                        // myturn bleibt false, da der Server beginnt.
                        break;

                    //... andere cases

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
                }
            } else {
                if (kicontr != null) { // Prüfen, ob eine KI dieses Spiel steuert
                    try {
                        // Kurze Pause für die Optik
                        Thread.sleep(500);

                        // KI entscheidet und schießt
                        int[] coords = kicontr.getKi().getShotCoordinates();
                        System.out.println("[MultiplayerLogic] KI-LOGIC: Schieße auf (" + coords[0] + ", " + coords[1] + ")");
                        kiShoot(coords[0], coords[1]);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Wenn es keine KI ist, warte einfach (auf Klick des Menschen)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
    public void setX(int x1){
        x = x1;
    }
    public void setY(int y1){
        y = y1;
    }

    public void setEn(Gamefield en){
        enemy = en;
    }
    public void setPl(Gamefield pl){
        player = pl;
    }

    public void setController(HelloController c ){
        contr = c;
    }
    public boolean getClient(){
        return client;
    }
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
    public boolean getTurn(){
        return myturn;
    }
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

    //multiplayer
    public void sendSaveCommand(long id) throws IOException {
        if (client)
            cl.sendSave(id);
        else
            s.sendSave(id);
    }
    public void setTurn(boolean isMyTurn) {
        this.myturn = isMyTurn;
        System.out.println("[MultiplayerLogic] Spielzug nach Laden gesetzt: " + (isMyTurn ? "Ich bin dran." : "Gegner ist dran."));
    }
    public void kiShoot(int targetX, int targetY) throws IOException {
        if (!myturn) {
            System.err.println("[MultiplayerLogic] WARNUNG: KI versucht zu schießen, obwohl sie nicht am Zug ist.");
            return;
        }
        // Die globalen x und y Koordinaten setzen, die von `startShoot` verwendet werden
        this.x = targetX;
        this.y = targetY;

        // Die existierende Methode aufrufen, die "shot x y" an den Gegner sendet.
        startShoot();
    }
    public synchronized void kiIsReady() {
        try {
            if (client) {
                clientKiReady = true;
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Client-KI ist bereit.");
                // Der Client hat seine Schiffe platziert, jetzt sendet er "done" als Antwort
                // auf die "ships"-Nachricht, die er vom Server erhalten haben muss.
                cl.sendDone();
            } else {
                serverKiReady = true;
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Server-KI ist bereit.");
                // Der Server ist bereit. Er kann jetzt seine Schiffe senden.
                sendShipsForKi(); // <- NEUE, nicht-blockierende Sendemethode
            }

            // Wenn beide KIs bereit sind, startet das eigentliche Spiel.
            if (serverKiReady && clientKiReady) {
                System.out.println("[MultiplayerLogic] MultiplayerLogic: Beide KIs sind bereit. Starte Spiel-Loop.");
                s.sendReady(); // Server sagt dem Client, dass es losgeht.
                startMultiplayerloop();
            }
        } catch (IOException e) {
            System.err.println("[MultiplayerLogic] Fehler in kiIsReady: " + e.getMessage());
        }
    }

    // Eine neue Methode, die nur sendet, ohne auf eine Antwort zu warten.
    private void sendShipsForKi() throws IOException {
        if (!client) {
            int[] shipLengths = player.getShipLengths();
            s.sendShips(shipLengths);
            System.out.println("[MultiplayerLogic] Server: Schiffe an Client-KI gesendet.");
        }
    }
    public void setKicontroler(KiPlayerController ki){
        kicontr = ki;
    }
    public Client getClientObj() {
        return this.cl;
    }
    public void sendShipsAndWaitForDone() throws IOException {
        if (client) return; // Nur für Server

        int[] shipLengths = player.getShipLengths();
        s.sendShips(shipLengths);
        System.out.println("[MultiplayerLogic] [Server] 'ships' gesendet. Warte auf 'done'...");

        String messagedone = s.receiveMessage(); // Blockiert hier
        System.out.println("[MultiplayerLogic] [Server] Nachricht empfangen: " + messagedone);

        if (messagedone.contains("done")) {
            s.sendReady();
            System.out.println("[MultiplayerLogic] [Server] 'Ready' gesendet. Starte Game-Loop.");
            startMultiplayerloop(); // Startet startGameFlow in einem neuen Thread
        }
    }
}