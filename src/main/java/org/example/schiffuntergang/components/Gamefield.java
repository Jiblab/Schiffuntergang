package org.example.schiffuntergang.components;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.GameState;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.SerializableShip;
import org.example.schiffuntergang.components.Position;

import java.util.ArrayList;
import java.util.List;

public class Gamefield extends GridPane {

    private final List<Ships> placedShip = new ArrayList<>();
    private final int lang;
    private final int breit;
    private final boolean enemy;
    private final Cell[][] cells;
    private int usedCells = 0;
    private final HelloController control;
    private EnemyPlayer en;

    public Gamefield(boolean enemy, HelloController controller, int h, int b) {
        this.lang = h;
        this.breit = b;
        this.enemy = enemy;
        this.control = controller;
        this.cells = new Cell[h][b];
        initializeCells();
    }
    public Gamefield(boolean enemy, HelloController controller, int h, int b, EnemyPlayer enemyPlayer) {
        this(enemy, controller, h, b);
        this.en = enemyPlayer;
    }

    private void initializeCells() {
        for (int i = 0; i < lang; i++) {
            for (int j = 0; j < breit; j++) {
                Cell c = new Cell(j, i, this, 30, 30, control);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(Color.BLUE);

                final int x = i;
                final int y = j;

                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {
                        if (getUsedCells() <= maxShipsC()) {
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, x, y, control.getDirection())) {
                                increaseCells(ship.getLength());
                                addShip(ship);
                            }
                        } else {
                            System.out.println("Maximale Anzahl an Schiffen erreicht.");
                        }
                    } else if (event.getButton() == MouseButton.PRIMARY && enemy) {
                        shoot(x, y);
                    }
                });

                add(c, i, j);
            }
        }
    }
    public static Gamefield fromGameState(GameState state, boolean isEnemy) {
        HelloController dummyController = new HelloController();
        Gamefield field = new Gamefield(isEnemy, dummyController, state.getBoardWidth(), state.getBoardWidth());

        for (SerializableShip shipData : state.getShips()) {
            Ships ship = new Ships(shipData.getLength(), shipData.getHealth());
            boolean vertical = shipData.isVertical();
            int x = shipData.getStartX();
            int y = shipData.getStartY();

            if (field.placeShip(ship, x, y, vertical)) {
                field.addShip(ship);
                field.increaseCells(ship.getLength());
            }
        }

        for (Position pos : state.getShotCells()) {
            Cell c = field.getCell(pos.getX(), pos.getY());
            c.setShot(true);
            c.setFill(c.getShip() != null ? Color.RED : Color.BLACK);
        }

        return field;
    }
    public boolean placeShip(Ships ship, int x, int y, boolean vertical) {
        int length = ship.getLength();

        if (vertical && y + length > lang) return false;
        if (!vertical && x + length > breit) return false;

        for (int i = 0; i < length; i++) {
            int xi = vertical ? x : x + i;
            int yi = vertical ? y + i : y;

            if (getCell(xi, yi).getShip() != null) return false;
        }

        for (int i = 0; i < length; i++) {
            int xi = vertical ? x : x + i;
            int yi = vertical ? y + i : y;

            Cell c = getCell(xi, yi);
            c.setShip(ship);
            c.setFill(enemy ? Color.GRAY : Color.WHITE);
            c.setStroke(enemy ? Color.BLACK : Color.GREEN);
        }

        return true;
    }
    public void shoot(int x, int y) {
        Cell c = getCell(x, y);
        Ships s = c.getShip();

        if (s != null) {
            s.hit();
            c.setFill(Color.RED);
        } else {
            c.setFill(Color.BLACK);
        }

        c.setShot(true);

        if (control.getPlayerturn()) {
            en.revenge();
        } else {
            control.setPlayerturn();
        }
    }
    public Cell getCell(int x, int y) {
        return cells[x][y];
    }
    public void addShip(Ships ship) {
        placedShip.add(ship);
    }
    public void increaseCells(int length) {
        usedCells += length;
    }
    public double maxShipsC() {
        return lang * breit * 0.3;
    }
    public int getUsedCells() {
        return usedCells;
    }
    public int getLang() {
        return lang;
    }
    public int getBreit() {
        return breit;
    }
    public boolean isEnemy() {
        return enemy;
    }
    public boolean getStatus() {
        return enemy;
    }
    public List<Ships> getShips() {
        return placedShip;
    }
    public double getMusicVolume() {
        return 1.0; // Placeholder
    }
    public boolean isMusicEnabled() {
        return true; // Placeholder
    }
    public void increaseUsedCells(int length) {
        this.usedCells += length;
    }
}