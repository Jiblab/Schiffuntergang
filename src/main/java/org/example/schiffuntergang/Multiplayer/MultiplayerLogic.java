package org.example.schiffuntergang.Multiplayer;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

import javax.imageio.IIOException;
import java.io.IOException;

public class MultiplayerLogic {
    private Client cl;
    private Server s;
    private boolean client;
    private Gamefield enemy;
    private Gamefield player;
    private int merkx;
    private int merky;
    private int x;
    private int y;
    private HelloController contr;
    private boolean myturn;
    private boolean firstturn = true;

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
            //int[] shipLengths = {5,4,3};
            s.sendShips(shipLengths);
            System.out.println("Schiffe gesendet");
            String messagedone = s.receiveMessage();
            System.out.println(messagedone);
            if (messagedone.contains("done")){
                s.sendReady();
                System.out.println("Ready sent!");
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
            // The 'else' block was missing, but the core logic is inside the while loop.
            // Let's assume the firstturn logic is handled and we proceed to the main loop.

            while (true) {
                // Nur auf Nachrichten lauschen, wenn man nicht am Zug ist
                if (!myturn) {
                    String m = s.receiveMessage();
                    String[] p = m.split(" ");
                    switch (p[0]) {
                        case "shot": // Der Client hat auf uns geschossen
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));

                            // Fall 1: Zelle wurde bereits beschossen
                            if (c.isShot()) {
                                s.sendAnswer(0);
                                System.out.println("Schon getroffen, sende 0");
                            } else {
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null) { // Fehlschuss
                                    s.sendAnswer(0);
                                    System.out.println("Antwort gesendet: 0 (Miss)");
                                } else if (!c.getShip().isAlive()) { // Versenkt
                                    player.deleteShip();
                                    s.sendAnswer(2);
                                    System.out.println("Antwort gesendet: 2 (Sunk)");
                                } else { // Treffer
                                    s.sendAnswer(1);
                                    System.out.println("Antwort gesendet: 1 (Hit)");
                                }
                            }
                            break;

                        case "answer": // Wir haben eine Antwort auf unseren Schuss erhalten
                            Cell ce = enemy.getCell(x, y);
                            if (p[1].equals("0")) { // Fehlschuss
                                Platform.runLater(() -> ce.setFill(Color.BLACK));
                                // Unser Zug ist vorbei. Wir übergeben mit "pass".
                                System.out.println("Fehlschuss. Sende pass.");
                                s.sendPass();
                            } else { // Treffer (1) oder Versenkt (2)
                                Platform.runLater(() -> ce.setFill(Color.RED));
                                // Es ist immer noch unser Zug! Wir setzen myturn wieder auf true.
                                System.out.println("Treffer/Versenkt. Ich bin wieder dran.");
                                myturn = true;
                            }
                            break;

                        case "pass": // Der Client übergibt den Zug an uns
                            System.out.println("habe pass bekommen");
                            myturn = true; // Jetzt sind wir dran
                            break;
                        // ... andere cases
                    }
                } else {
                    // Wenn wir am Zug sind, warten wir auf eine UI-Aktion.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public void start() throws IOException {
        if (!client){ //man selber ist host
            s.start(5000);
            System.out.println("Multiplayer connected!");
            s.sendSize(player.getBreit(), player.getLang());
            System.out.println("Server: Größen geschickt mit Werten von: "+player.getBreit()+" "+player.getLang());
            String message = s.receiveMessage();
            System.out.println(message);
            //hier sollen die platzierten schiffe dann rübergeschickt werden vielleicht am besten mit einem button oder so
            //das thread ding von chat mal übernommen und ausprobiert aber funktioniert auch d

            System.out.println("While schleife fürs warten verlassen");

        }
        else {// man ist client und joined
            cl.connect(contr.getIP(), contr.getPort());
            boolean temp = true;

            System.out.println("nachricht wurde gesplitet");
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
                        System.out.println("bin in case size");
                        rows = Integer.parseInt(p1[1]);
                        cols = Integer.parseInt(p1[2]);
                        // Jetzt kannst du das Spielfeld erzeugen
                        player = new Gamefield(false, contr, cols, rows, this);  // z. B. true = eigenes Feld
                        enemy = new Gamefield(true, contr, cols, rows, this); // false = Gegnerfeld
                        Platform.runLater(() -> {
                            contr.setupGameMult(player, enemy);
                        });
                        cl.sendDone(); // Antwort an den Server
                        break;
                    case "load":
                        //lade ID
                        break;
                    case "ships":
                        int[] receivedShipCounts = new int[6]; // Index = Länge, Wert = Anzahl
                        for (int j = 1; j < p1.length; j++) {
                            int len = Integer.parseInt(p1[j]);
                            if (len < receivedShipCounts.length) {
                                receivedShipCounts[len]++;
                            }
                        }

                        // Übergib dieses Zähler-Array an den Controller
                        final int[] finalCounts = receivedShipCounts; // Finale Kopie für Lambda
                        Platform.runLater(() -> {
                            contr.setShipRules(finalCounts); // Eine neue Methode im Controller
                            contr.setupClientPlacementUI(finalCounts); // Baut die UI auf
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
                System.out.println("hab ready bekommen");
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
                            System.out.println("Schon getroffen, sende 0");
                        } else {
                            // Ansonsten, schieße auf die Zelle
                            player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (c.getShip() == null) { // Fehlschuss
                                cl.sendAnswer(0);
                                System.out.println("Antwort gesendet: 0 (Miss)");
                            } else if (!c.getShip().isAlive()) { // Versenkt
                                player.deleteShip();
                                cl.sendAnswer(2);
                                System.out.println("Antwort gesendet: 2 (Sunk)");
                            } else { // Treffer
                                cl.sendAnswer(1);
                                // WICHTIG: myturn wird hier NICHT geändert. Der Server ist weiterhin dran.
                                System.out.println("Antwort gesendet: 1 (Hit)");
                            }
                        }
                        break;

                    case "answer": // Wir haben eine Antwort auf unseren Schuss erhalten
                        Cell ce = enemy.getCell(x, y);
                        if (p[1].equals("0")) { // Fehlschuss
                            Platform.runLater(() -> ce.setFill(Color.BLACK));
                            // Unser Zug ist vorbei. Wir übergeben mit "pass".
                            System.out.println("Fehlschuss. Sende pass.");
                            cl.sendPass();
                        } else { // Treffer (1) oder Versenkt (2)
                            Platform.runLater(() -> ce.setFill(Color.RED));
                            // Es ist immer noch unser Zug! Wir müssen myturn wieder auf true setzen,
                            // damit die UI einen neuen Schuss erlaubt.
                            System.out.println("Treffer/Versenkt. Ich bin wieder dran.");
                            myturn = true;
                        }
                        break;

                    case "pass": // Der Server übergibt den Zug an uns
                        System.out.println("habe pass bekommen");
                        myturn = true; // Jetzt sind wir dran
                        break;

                    //... andere cases
                }
            } else {
                // Wenn wir am Zug sind, warten wir auf eine UI-Aktion.
                // Ein kurzer Sleep verhindert, dass die Schleife die CPU voll auslastet.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
            myturn = false;
            System.out.println(x+" "+y);
        }
        else {
            cl.sendShot(x, y);
            myturn = false;
        }

    }


    /*public void setmyturn(boolean turn) {
        myturn = !turn;
    }*/

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
}
