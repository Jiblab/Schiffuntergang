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



    public Gamefield(boolean enemy){
        this.enemy = enemy;
        for (int i = 0; i < 10; i++){

            for(int j = 0; j < 10; j++){
                Cell c = new Cell(j, i, this, lang, breit);
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

    public boolean placeShip(Ships ship, int x, int y, boolean vertical){
        return false;
    }

    public boolean isEnemy() {
        return enemy;
    }
}