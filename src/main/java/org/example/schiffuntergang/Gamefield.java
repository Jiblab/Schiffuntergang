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
    private Ships ship [] = new Ships[3];
    private int schiffe;
    private int lang = 30;
    private int breit = 30;
    private boolean enemy;
    private Cell [][] cells = new Cell[10][10];
    private int count;
    private HelloController control;



    public Gamefield(boolean enemy, HelloController controler){
        this.enemy = enemy;
        this.control = controler;
        for (int i = 0; i < 10; i++){

            for(int j = 0; j < 10; j++){
                Cell c = new Cell(j, i, this, lang, breit, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(Color.BLUE);

                add(c, i, j);
            }
        }
    }

    public Cell getCell(int x, int y){
        return cells[x][y];
    }

    public boolean getStatus(){
        return enemy;
    }

    public Ships getShip(){
        Ships aktuelleship = ship[count];
        count++;
        return aktuelleship;

    }


        public boolean placeShip(Ships ship, int x, int y, boolean vertical) {
            int length = ship.getLength();

            // 1. Randprüfung
            if (vertical) {
                if (y + length > 10) {
                    System.out.println("Randüberschreitung (vertikal)");
                    return false;
                }
            } else {
                if (x + length > 10) {
                    System.out.println("Randüberschreitung (horizontal)");
                    return false;
                }
            }

            // 2. Prüfen ob schon Schiffe dort liegen
            for (int i = 0; i < length; i++) {
                int xi = vertical ? x : x + i;
                int yi = vertical ? y + i : y;
                Cell cell = getCell(xi, yi);
                if (cell.getShip() != null) {
                    System.out.println("Zelle bereits belegt bei: " + xi + ", " + yi);
                    return false;
                }
            }

            // 3. Platzieren
            for (int i = 0; i < length; i++) {
                int xi = vertical ? x : x + i;
                int yi = vertical ? y + i : y;
                Cell cell = getCell(xi, yi);
                cell.setShip(ship);

                if (!enemy) {
                    cell.setFill(Color.WHITE);
                    cell.setStroke(Color.GREEN);
                }
            }

            return true;
        }

    public boolean isEnemy() {
        return enemy;
    }
}