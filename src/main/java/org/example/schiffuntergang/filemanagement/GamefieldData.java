// File: GamefieldData.java
// Put this in your 'filemanagement' package, since it's related to saving/loading.
package org.example.schiffuntergang.filemanagement;

import org.example.schiffuntergang.components.Position;

import java.util.List;
import java.util.ArrayList;


public class GamefieldData {

    private int width;
    private int height;
    private boolean isEnemy;
    private List<SerializableShip> ships;
    private List<Position> shotPositions;

    public GamefieldData() {
        this.ships = new ArrayList<>();
        this.shotPositions = new ArrayList<>();
    }


    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public boolean isEnemy() { return isEnemy; }
    public void setEnemy(boolean enemy) { isEnemy = enemy; }

    public List<SerializableShip> getShips() { return ships; }
    public void setShips(List<SerializableShip> ships) { this.ships = ships; }

    public List<Position> getShotPositions() { return shotPositions; }
    public void setShotPositions(List<Position> shotPositions) { this.shotPositions = shotPositions; }
}