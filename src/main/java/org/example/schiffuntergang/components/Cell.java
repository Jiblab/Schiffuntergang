package org.example.schiffuntergang.components;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.schiffuntergang.HelloController;

public class Cell extends Rectangle {
    private boolean shot = false;
    private Ships ship = null;
    public int x;
    public int y;
    private final Gamefield board;
    private final HelloController control;

    public Cell(int x, int y, Gamefield board, int h, int w, HelloController controler){

        super(h, w);
        this.x = x;
        this.y = y;
        this.board = board;
        this.control = controler;

        //setOnMouseClicked(event -> onCellClicked(event));

    }

    public Gamefield getBoard(){
        return board;
    }

    public void setShip(Ships ships){
        ship = ships;
        System.out.println("auf "+x+"und "+y+" liegt ein schiff");
    }
    public Ships getShip(){
        return ship;
    }

    public void setShot(boolean s){
        this.shot = s;
        if (s) {
            setFill(ship != null ? Color.RED : Color.LIGHTGREEN);
        }

    }
    public boolean isShot(){
        return shot;
    }
}
