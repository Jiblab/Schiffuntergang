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
        if(!client) {
            String yeye = s.receiveMessage();
            System.out.println(yeye);
            if (firstturn){
                if (yeye.equals("ready")) {
                    firstturn = false;
                }
            }
            else {
                while (true) {
                    if (myturn) {


                        String m = s.receiveMessage();
                        String[] p = m.split(" ");
                        switch (p[0]) {
                            case "shot":
                                Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (!c.isShot()) {
                                    merkx = Integer.parseInt(p[1]);
                                    merky = Integer.parseInt(p[2]);
                                    player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                    if (c.getShip() == null) {
                                        s.sendAnswer(0);
                                    } else if (c.getShip() != null && !c.getShip().isAlive()) {
                                        player.deleteShip();
                                        s.sendAnswer(2);
                                    } else {
                                        s.sendAnswer(1);
                                    }
                                } else {
                                    System.out.println("ist doch schon geschossen");
                                }
                                break;

                            case "answer":
                                Cell ce = enemy.getCell(x, y);
                                if (p[1].equals("0")) {
                                    System.out.println("habe 0 antwort");

                                    Platform.runLater(()->ce.setFill(Color.BLACK));
                                    myturn = false;
                                    System.out.println("habe pass gesendet server");
                                    s.sendPass();
                                }
                                if (p[1].equals("1")) {
                                    System.out.println("habe 1 antwort");

                                    //ce.getShip().hit();
                                    Platform.runLater(()->ce.setFill(Color.RED));
                                }
                                if (p[1].equals("2")) {
                                    System.out.println("habe 2 antwort");

                                    //ce.getShip().hit();
                                    //enemy.deleteShip();
                                    Platform.runLater(()->ce.setFill(Color.RED));
                                }
                                break;

                            case "pass":
                                System.out.println("habe pass bekommen");
                                myturn = true;
                                break;


                            case "ok":
                                break;

                            case "save":
                                break;

                            case "ships":
                                break;
                        }

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
                        player = new Gamefield(false, contr, rows, cols, this);  // z. B. true = eigenes Feld
                        enemy = new Gamefield(true, contr, rows, cols, this); // false = Gegnerfeld
                        Platform.runLater(() -> {
                            contr.setupGameMult(enemy, player);
                        });
                        cl.sendDone(); // Antwort an den Server
                        break;
                    case "load":
                        //lade ID
                        break;
                    case "ships":
                        player.clearShips();
                        int[] lengths = new int[p1.length - 1];
                        for (int j = 1; j < p1.length; j++) {
                            System.out.println(p1[j]);
                            int len = Integer.parseInt(p1[j]);

                            lengths[j - 1] = len;
                            player.addShip(new Ships(len, len));
                        }
                        player.getControl().setShipCountsFromNetwork(lengths);
                        //while (!contr.getReady()) {

                        //}
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

                if (!myturn) {


                    String m = cl.receiveMessage();
                    String[] p = m.split(" ");
                    switch (p[0]) {
                        case "shot": //TODO hier wird die answer nicht richtig geschickt oder im server nicht richtig enmpfangen
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                merkx = Integer.parseInt(p[1]);
                                merky = Integer.parseInt(p[2]);
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null) {
                                    cl.sendAnswer(0);
                                } else if (c.getShip() != null && !c.getShip().isAlive()) {
                                    player.deleteShip();
                                    cl.sendAnswer(2);
                                } else {
                                    cl.sendAnswer(1);
                                }
                                if(!c.isShot()) {

                                  cl.sendAnswer(0);
                                //gehört zur ifabfrage
                                //System.out.println("ist doch schon geschossen");
                            }
                            break;

                        case "answer":
                            Cell ce = enemy.getCell(x, y);
                            if (p[1].equals("0")) {
                                System.out.println("habe 0 antwort");

                                Platform.runLater(()->ce.setFill(Color.BLACK));
                                myturn = false;
                                System.out.println("sende pass");
                                cl.sendPass();
                            }
                            if (p[1].equals("1")) {
                                //ce.getShip().hit();
                                System.out.println("habe 1 antwort");
                                Platform.runLater(()->ce.setFill(Color.RED));
                            }
                            if (p[1].equals("2")) {
                                //ce.getShip().hit();
                                //enemy.deleteShip();
                                System.out.println("habe 2 antwort");

                                Platform.runLater(()->ce.setFill(Color.RED));
                            }
                            break;

                        case "pass":
                            System.out.println("habe pass bekommen");
                            myturn = true;
                        break;


                        case "ok":
                            break;

                        case "save":
                            break;

                    }
                }
                else {

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
