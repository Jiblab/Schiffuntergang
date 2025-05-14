package org.example.schiffuntergang;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.schiffuntergang.Gamefield;

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

        setOnMouseClicked(event -> onCellClicked(event));

    }

    public void onCellClicked(MouseEvent event){
        if(board.getStatus()){
            shoot();
        }
        else {
            if(ship == null){
                board.placeShip(ship, x, y, true);
            }
        }
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


    public void placeShip(){
        if (ship == null){

        }
    }

    public Gamefield getBoard(){
        return board;
    }

}
