package org.example.schiffuntergang;

import javafx.scene.paint.Color;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.components.Cell;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;

public class Logic {
    private Gamefield board;
    private EnemyPlayer en;

    public Logic(Gamefield b, EnemyPlayer p){
        board = b;
        en = p;

    }

    public boolean placeShip(Ships ship, int x, int y, boolean vertical) {
        int length = ship.getLength();

        // 1. Randüberprüfung
        if (vertical) {
            if (y + length > board.getLang()) {
                System.out.println("Schiff geht vertikal über den Rand.");
                return false;
            }
        } else {
            if (x + length > board.getBreit()) {
                System.out.println("Schiff geht horizontal über den Rand.");
                return false;
            }
        }

        // 2. Überprüfung auf Kollisionen
        for (int i = 0; i < length; i++) {
            int xi;
            int yi;

            if (vertical) {
                xi = x;
                yi = y + i;
            } else {
                xi = x + i;
                yi = y;
            }

            Cell c = board.getCell(xi, yi);
            if (c.getShip() != null) {
                System.out.println("Zelle (" + xi + ", " + yi + ") ist bereits belegt.");
                return false;
            }
        }
        for (int i = 0; i < length; i++) {
            int xi;
            int yi;

            if (vertical) {
                xi = x;
                yi = y + i;
            } else {
                xi = x + i;
                yi = y;
            }

            Cell c = board.getCell(xi, yi);
            c.setShip(ship);


            if (!board.isEnemy()) {
                c.setFill(Color.WHITE);
                c.setStroke(Color.GREEN);
            }
            else{
                c.setFill(Color.GRAY);
            }
        }

        System.out.println("Schiff erfolgreich platziert bei Start (" + x + ", " + y + "), Richtung: " + (vertical ? "vertikal" : "horizontal"));
        return true;
    }

    public void shoot(int x, int y){
        Cell c = board.getCell(x, y);
        Ships s = c.getShip();

        if (s != null){
            s.hit();
            c.setFill(Color.RED);

            if (board.isEnemy()){
                System.out.println("nix hier in der if abfrage");
                en.revenge();

            }


        }
        else {
            c.setFill(Color.BLACK);
            System.out.println("Koordinaten x, dann y: "+x+" "+y);
            if (board.isEnemy()){
                en.revenge();
            }

        }

    }
}
