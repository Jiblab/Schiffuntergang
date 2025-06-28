package org.example.schiffuntergang.Multiplayer;

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

    public void start() throws IOException {
        if (!client){ //man selber ist host
            s.start(5000);
            s.sendSize(player.getBreit(), player.getLang());
            while(!s.receiveMessage().equals("done")){

            }
            //hier sollen die platzierten schiffe dann rübergeschickt werden vielleicht am besten mit einem button oder so
            while(!player.getControl().getReady()){

            }
            int[] shipLengths = player.getShipLengths();
            s.sendShips(shipLengths);

            while(true){
                if (s.receiveMessage().equals("done")){
                    s.sendReady();
                    break;
                }
            }


            if (s.receiveMessage().equals("ready")){
                while(true){
                    String m = s.receiveMessage();
                    String [] p = m.split(" ");
                    switch(s.receiveMessage()){
                        case "shot":
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (!c.isShot()){
                                merkx = Integer.parseInt(p[1]);
                                merky = Integer.parseInt(p[2]);
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null){
                                    s.sendAnswer(0);
                                } else if (c.getShip() != null && !c.getShip().isAlive()) {
                                    s.sendAnswer(2);
                                }
                                else {
                                    s.sendAnswer(1);
                                }
                            }
                            else{
                                System.out.println("ist doch schon geschossen");
                            }
                            break;

                        case "answer":
                            Cell ce = enemy.getCell(merkx, merky);
                            if (p[1].equals("0")){
                                ce.setFill(Color.BLACK);
                                s.sendPass();
                            }
                            if (p[1].equals("1")){
                                ce.getShip().hit();
                                ce.setFill(Color.RED);
                            }
                            if (p[1].equals("2")){
                                ce.getShip().hit();
                                enemy.deleteShip();
                                ce.setFill(Color.RED);
                            }
                            break;

                        case "pass":
                            s.sendShot(x, y);
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
        else {// man ist client und joined
            cl.connect(contr.getIP(), contr.getPort());
            boolean temp = true;
            String m1 = cl.receiveMessage();
            String [] p1 = m1.split(" ");
            int rows = 0;
            int cols = 0;
            while(temp){
                switch (cl.receiveMessage()){
                    /*case "done":
                        cl.sendDone();
                        temp = false;
                        break;*/
                    case "size":
                        rows = Integer.parseInt(p1[1]);
                        cols = Integer.parseInt(p1[2]);
                        // Jetzt kannst du das Spielfeld erzeugen
                        player = new Gamefield(false, contr, rows, cols, this);  // z. B. true = eigenes Feld
                        enemy = new Gamefield(true, contr, rows, cols, this); // false = Gegnerfeld
                        contr.setBoard(enemy, player);
                        contr.temp();
                        cl.sendDone(); // Antwort an den Server
                        break;
                    case "load":
                        //lade ID
                        break;
                    case "ships":
                        int[] lengths = new int[p1.length - 1];
                        for (int i = 1; i < p1.length; i++) {
                            int len = Integer.parseInt(p1[i]);
                            lengths[i - 1] = len;
                            player.addShip(new Ships(len, len));
                        }
                        player.getControl().setShipCountsFromNetwork(lengths);
                        while(!contr.getReady()){

                        }
                        cl.sendDone();
                        break;
                }
            }

            if (cl.receiveMessage().equals("ready")) {
                cl.sendReady();

                while (true) {
                    String m = cl.receiveMessage();
                    String[] p = m.split(" ");
                    switch (cl.receiveMessage()) {
                        case "shot":
                            Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                            if (!c.isShot()) {
                                merkx = Integer.parseInt(p[1]);
                                merky = Integer.parseInt(p[2]);
                                player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                                if (c.getShip() == null){
                                    cl.sendAnswer(0);
                                } else if (c.getShip() != null && !c.getShip().isAlive()) {
                                    cl.sendAnswer(2);
                                }
                                else {
                                    cl.sendAnswer(1);
                                }
                            } else {
                                System.out.println("ist doch schon geschossen");
                            }
                            break;

                        case "answer":
                            Cell ce = enemy.getCell(merkx, merky);
                            if (p[1].equals("0")) {
                                ce.setFill(Color.BLACK);
                                cl.sendPass();
                            }
                            if (p[1].equals("1")) {
                                ce.getShip().hit();
                                ce.setFill(Color.RED);
                            }
                            if (p[1].equals("2")) {
                                ce.getShip().hit();
                                enemy.deleteShip();
                                ce.setFill(Color.RED);
                            }
                            break;

                        case "pass":
                            cl.sendShot(x, y);
                            break;


                        case "ok":
                            break;

                        case "save":
                            break;

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
}
