package org.example.schiffuntergang.filemanagement;

public class SerializableShip {
    private int startX;
    private int startY;
    private int length;
    private boolean vertical;
    private int health;


    public SerializableShip() {

    }


    public SerializableShip(int startX, int startY, int length, boolean vertical, int health) {
        this.startX = startX;
        this.startY = startY;
        this.length = length;
        this.vertical = vertical;
        this.health = health;
    }

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getLength() { return length; }
    public int getHealth() { return health; }
    public boolean isVertical() { return vertical; }


    public void setStartX(int startX) { this.startX = startX; }
    public void setStartY(int startY) { this.startY = startY; }
    public void setLength(int length) { this.length = length; }
    public void setVertical(boolean vertical) { this.vertical = vertical; }
    public void setHealth(int health) { this.health = health; }
}