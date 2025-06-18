package org.example.schiffuntergang.components;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.HelloController;

import java.util.ArrayList;
import java.util.List;

public class Gamefield extends GridPane {
    private List<Ships> placedShip = new ArrayList<>();
    private int lang;
    private int breit;
    private boolean enemy;
    private Cell[][] cells;
    private int usedCells = 0;
    private HelloController control;
    private EnemyPlayer en;




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
                final int x = i;
                final int y = j;
                // Hier ein OnClickListener setzen, um jeden Klick abzufangen :P
                // Ihr könnt hier dann mehrere Fälle einbauen wie rechtsklick zum Löschen etc...
                c.setOnMouseClicked(event -> {
                    if(event.getButton() == MouseButton.PRIMARY && !enemy){

                        if (getUsedCells() <= maxShipsC()){
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, x, y, control.getDirection())){
                                increaseCells(ship.getLength());
                                addShip(ship);
                            }
                        }
                        else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                        }



                    }else if(event.getButton() == MouseButton.PRIMARY && enemy){
                        shoot((int) c.getX(), (int) c.getY());
                    }
                });

                add(c, i, j);
            }
        }
    }

    public Gamefield(boolean enemy, HelloController controler, int h, int b, EnemyPlayer e){
        en = e;
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
                final int x = i;
                final int y = j;
                // Hier ein OnClickListener setzen, um jeden Klick abzufangen :P
                // Ihr könnt hier dann mehrere Fälle einbauen wie rechtsklick zum Löschen etc...
                c.setOnMouseClicked(event -> {
                    if(event.getButton() == MouseButton.PRIMARY && !enemy){

                        if (getUsedCells() <= maxShipsC()){
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, x, y, control.getDirection())){
                                increaseCells(ship.getLength());
                                addShip(ship);
                            }
                        }
                        else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                        }



                    }else if(event.getButton() == MouseButton.PRIMARY && enemy){
                        shoot(x, y);
                    }
                });

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


    public boolean placeShip(Ships ship, int x, int y, boolean vertical) {
        int length = ship.getLength();

        // 1. Randüberprüfung
        if (vertical) {
            if (y + length > lang) {
                System.out.println("Schiff geht vertikal über den Rand.");
                return false;
            }
        } else {
            if (x + length > breit) {
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

    public int getUsedCells(){
        return usedCells;
    }
    public void increaseCells(int laenge){
        usedCells += laenge;
    }

    public void shoot(int x, int y){
        Cell c = getCell(x, y);
        Ships s = c.getShip();

        if (s != null){
            s.hit();
            c.setFill(Color.RED);
            System.out.println(control.getPlayerturn());
            if (this.enemy){
                System.out.println("nix hier in der if abfrage");
                en.revenge();

            }


        }
        else {
            c.setFill(Color.BLACK);
            System.out.println("Koordinaten x, dann y: "+x+" "+y);
            if (this.enemy){
                en.revenge();
            }

        }

    }



    public boolean inBounds(int x, int y){
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    public int getLang(){
        return lang;
    }
    public int getBreit(){
        return breit;
    }
}