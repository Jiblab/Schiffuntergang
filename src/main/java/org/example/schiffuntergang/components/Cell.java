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
    private Gamefield board;
    private HelloController control;

    public Cell(int x, int y, Gamefield board, int h, int w, HelloController controler){

        super(h, w);
        this.x = x;
        this.y = y;
        this.board = board;
        this.control = controler;

        //setOnMouseClicked(event -> onCellClicked(event));

    }

    // Nicht mehr in Nutzung! Wird im @Gamefield.java:40 behandelt
    public void onCellClicked(MouseEvent event){
        if(board.getStatus()){
            shoot();
        }
        else {
            if(ship == null){
                if (board.getUsedCells() <= board.maxShipsC()){
                    Ships ship = new Ships(control.getLength(), control.getLength());
                    if (board.placeShip(ship, x, y, control.getDirection())){
                        board.addShip(ship);
                        board.increaseCells(ship.getLength());
                    }
                }
                else {
                    System.out.println("Maximale anzahl an schiffen erreicht");
                }

            }
        }
    }
    public void shoot(){
        shot = true;
        if(ship == null){
            setFill(Color.BLACK);
            System.out.println("Koordinaten x, dann y: "+x+" "+y);
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
            System.out.println("ist es hier die ganze zeit null?");
        }
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

}
