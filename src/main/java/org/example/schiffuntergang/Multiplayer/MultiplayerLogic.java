package org.example.schiffuntergang.Multiplayer;

import javafx.scene.paint.Color;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;

import javax.imageio.IIOException;
import java.io.IOException;

public class MultiplayerLogic {
    private Client cl;
    private Server s;
    private boolean server;
    private Gamefield enemy;
    private Gamefield player;
    private int merkx;
    private int merky;
    private int x;
    private int y;

    public MultiplayerLogic(Client c, boolean client, Gamefield en, Gamefield pl){
        cl = c;
        player = pl;
        enemy = en;
        server = client;
    }

    public MultiplayerLogic(Server se, boolean client, Gamefield en, Gamefield pl){
        s = se;
        player = pl;
        enemy = en;
        server = client;
    }

    public void start() throws IOException {
        if (server){ //man selber ist host
            s.start(5000);
            s.sendSize(player.getBreit(), player.getLang());
            while(true){
                if (s.receiveMessage().equals("done")){
                    break;
                }
            }
            //s.sendShips();

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

                        case "load":
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
            while(true){
                String m = cl.receiveMessage();
                String [] p = m.split(" ");
                switch(cl.receiveMessage()){
                    case "shot":
                        Cell c = player.getCell(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                        if (!c.isShot()){
                            merkx = Integer.parseInt(p[1]);
                            merky = Integer.parseInt(p[2]);
                            player.shoot(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                        }
                        else{
                            System.out.println("ist doch schon geschossen");
                        }
                        break;

                    case "answer":
                        Cell ce = enemy.getCell(merkx, merky);
                        if (p[1].equals("0")){
                            ce.setFill(Color.BLACK);
                            cl.sendPass();
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
                        cl.sendShot(x, y);
                        break;

                    case "load":
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
}
