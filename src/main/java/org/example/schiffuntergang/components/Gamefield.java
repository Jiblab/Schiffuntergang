package org.example.schiffuntergang.components;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.Logic;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.sounds.*;
import org.example.schiffuntergang.GameState;
import org.example.schiffuntergang.SerializableShip;

import java.util.ArrayList;
import java.util.Iterator;
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
    private MultiplayerLogic lo;




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



                    }else if(event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()){
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



                    }else if(event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()){
                        shoot(x, y);
                    }
                });

                add(c, i, j);
            }
        }
    }

    public Gamefield(boolean enemy, HelloController controler, int h, int b, MultiplayerLogic l){
        lang = h;
        breit = b;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        this.lo = l;

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

                        int len = control.getLength();
                        if (getUsedCells() <= maxShipsC() && len > 0 && control.canPlaceShipOfLength(len)) {
                            Ships ship = new Ships(len, len);
                            if (placeShip(ship, x, y, control.getDirection())) {
                                increaseCells(len);
                                addShip(ship);
                                if (control.isClientMode()) {
                                    control.shipPlaced(len);
                                }
                            }
                        }
                        else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                            /*Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Maximum erreicht");
                                alert.setHeaderText(null);
                                alert.setContentText("Maximale Anzahl an schiffen erreicht");
                                alert.showAndWait();
                            });*/

                        }



                    }else if(event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()){
                        lo.setX((int) c.getX());
                        lo.setY((int) c.getY());
                        shoot((int) c.getX(), (int) c.getY());
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
            placedShip.add(ship);


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
            if (s.getHealth() == 0){
                deleteShip();
            }
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

    public void deleteShip(){
        Iterator<Ships> iterator = placedShip.listIterator();
        while (iterator.hasNext()) {
            Ships schiff = iterator.next();
            if (!schiff.isAlive()) {
                iterator.remove();
            }
        }

        if (placedShip.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Spiel beendet");
                alert.setHeaderText(null);
                if (enemy){
                    alert.setContentText("Du gewinnst");
                }
                else {
                    alert.setContentText("Gegner gewinnt");
                }

                alert.showAndWait();
            });
        }
    }
    private boolean turn;
    public void setTurn(boolean t){
        turn = t;
    }
    public void setLogic(MultiplayerLogic l){
        lo = l;
    }
    public HelloController getControl(){
        return control;
    }

    public int[] getShipLengths() {
        int[] lengths = new int[placedShip.size()];
        for (int i = 0; i < placedShip.size(); i++) {
            lengths[i] = placedShip.get(i).getLength();
        }
        return lengths;
    }

    public double getMusicVolume() {
        return BackgroundMusic.getInstance().getVolume();
    }
    public boolean isMusicEnabled() {
        return BackgroundMusic.getInstance().isPlaying();
    }
    public List<Ships> getShips() {
        return placedShip;
    }
    public void increaseUsedCells(int length) {
        this.usedCells += length;
    }
    public static Gamefield fromGameState(GameState state, boolean isEnemy) {
        // Dummy-Controller, da dieser im Konstruktor benötigt wird
        HelloController dummyController = new HelloController();

        // Gamefield erzeugen (ohne AI, da Player)
        Gamefield board = new Gamefield(isEnemy, dummyController,
                state.getBoardHeight(), state.getBoardWidth());

        // Schiffe rekonstruieren
        for (SerializableShip s : state.getShips()) {
            Ships ship = new Ships(s.getLength(), s.getHealth());
            if (board.placeShip(ship, s.getStartX(), s.getStartY(), s.isVertical())) {
                board.addShip(ship);
                board.increaseUsedCells(ship.getLength());
            }
        }

        // Getroffene Zellen wiederherstellen
        for (Position p : state.getHitPositions()) {
            Cell cell = board.getCell(p.getX(), p.getY());
            cell.setShot(true);
            cell.setFill(cell.getShip() != null
                    ? javafx.scene.paint.Color.RED
                    : javafx.scene.paint.Color.BLACK);
        }

        return board;
    }

}