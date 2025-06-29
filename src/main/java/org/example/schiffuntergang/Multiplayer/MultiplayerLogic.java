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

    public MultiplayerLogic(Client c, boolean client1, Gamefield en, Gamefield pl){
        cl = c;
        player = pl;
        enemy = en;
        client = client1;
    }

    public MultiplayerLogic(Server se, boolean client1, Gamefield en, Gamefield pl){
        s = se;
        player = pl;
        enemy = en;
        client = client1;
    }

    public void sendShips() throws IOException {
        if (client){
            clientGame();
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
            }
            //startGameFlow();
        }

    }

    private void startGameFlow() throws IOException {
        if(!client) {
            String yeye = s.receiveMessage();
            System.out.println(yeye);

            if (yeye.equals("ready")) {
                //while (true) {
                startEndS();

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
        String f = cl.receiveMessage();
        if (f.equals("ready")) {
            System.out.println("hab ready bekommen");
            cl.sendReady();

            //while (true) {


                String m = cl.receiveMessage();
                String[] p = m.split(" ");
                switch (p[0]) {
                    case "shot":
                        Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                        if (!c.isShot()) {
                            merkx = Integer.parseInt(p[1]);
                            merky = Integer.parseInt(p[2]);
                            player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (c.getShip() == null) {
                                cl.sendAnswer(0);
                            } else if (c.getShip() != null && !c.getShip().isAlive()) {
                                cl.sendAnswer(2);
                            } else {
                                cl.sendAnswer(1);
                            }
                        } else {
                            System.out.println("ist doch schon geschossen");
                        }
                        break;

                    case "answer":
                        Cell ce = enemy.getCell(x, y);
                        if (p[1].equals("0")) {
                            ce.setFill(Color.BLACK);
                            cl.sendPass();
                        }
                        if (p[1].equals("1")) {
                            //ce.getShip().hit();
                            ce.setFill(Color.RED);
                        }
                        if (p[1].equals("2")) {
                            //ce.getShip().hit();
                            //enemy.deleteShip();
                            ce.setFill(Color.RED);
                        }
                        break;

                    case "pass":
                        return;
                    //break;


                    case "ok":
                        break;

                    case "save":
                        break;

                }
            }
        //}
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
            System.out.println(x+" "+y);
            startGameFlow();
        }
        else {
            cl.sendShot(x, y);
            clientGame();
        }

    }


    public void startEndS() throws IOException {
        while (true) {


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
                        ce.setFill(Color.BLACK);
                        s.sendPass();
                    }
                    if (p[1].equals("1")) {
                        //ce.getShip().hit();
                        ce.setFill(Color.RED);
                    }
                    if (p[1].equals("2")) {
                        //ce.getShip().hit();
                        //enemy.deleteShip();
                        ce.setFill(Color.RED);
                    }
                    break;

                case "pass":
                    return;


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
