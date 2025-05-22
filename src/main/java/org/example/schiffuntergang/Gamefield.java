package org.example.schiffuntergang;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.w3c.dom.events.MouseEvent;
import javafx.geometry.Point2D;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;

public class Gamefield extends GridPane {
    private List<Ships> placedShip = new ArrayList<>();
    private int lang;
    private int breit;
    private boolean enemy;
    private Cell [][] cells;
    private int count;
    private HelloController control;




    public Gamefield(boolean enemy, HelloController controler, int h, int b){
        lang = h;
        breit = b;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        for (int i = 0; i < h; i++){

            for(int j = 0; j < b; j++){
                Cell c = new Cell(j, i, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(Color.BLUE);

                add(c, i, j);
            }
        }
    }

    public Cell getCell(int x, int y){
        return cells[y][x];
    }

    public boolean getStatus(){
        return enemy;
    }

    /*public Ships getShip(){
        Ships aktuelleship = ship[count];
        count++;
        return aktuelleship;

    }*/

    public boolean placeShip(Ships ship, int x, int y, boolean vertical) {
        int length = ship.getLength();

        // 1. Randüberprüfung
        if (vertical) {
            if (y + length > 10) {
                System.out.println("Schiff geht vertikal über den Rand.");
                return false;
            }
        } else {
            if (x + length > 10) {
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

            Cell c = getCell(xi, yi);
            if (c.getShip() != null) {
                System.out.println("Zelle (" + xi + ", " + yi + ") ist bereits belegt.");
                return false;
            }
        }

        // 3. Platzieren des Schiffs
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

            Cell c = getCell(xi, yi);
            c.setShip(ship);

            if (!enemy) {
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


    public boolean isEnemy() {
        return enemy;
    }

    public double maxShipsC(){
        return (double) lang * (double) breit * 0.3;
    }

    public void addShip(Ships ship){
        placedShip.add(ship);
    }
}