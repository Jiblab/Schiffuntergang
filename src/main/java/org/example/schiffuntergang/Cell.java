package org.example.schiffuntergang;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class Cell extends Rectangle {
    private boolean shot = false;
    private Ships ship = null;
    public int x;
    public int y;
    private Gamefield board;

    public Cell(int x, int y, Gamefield board, int h, int w){
        super(h, w);
        this.x = x;
        this.y = y;
        this.board = board;

    }

    public void shoot(){
        shot = true;
        if(ship == null){
            setFill(Color.BLACK);
        }
        else{
            ship.hit();
            setFill(Color.RED);

            if(!ship.isAlive()){

            }
        }

    }

}
