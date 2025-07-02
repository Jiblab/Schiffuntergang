package org.example.schiffuntergang.filemanagement;

public class SerializableShip {
    private int startX;
    private int startY;
    private int length;
    private boolean vertical;
    private int health;

    public SerializableShip(int startX, int startY, int length, boolean vertical, int health) {
        this.startX = startX;
        this.startY = startY;
        this.length = length;
        this.vertical = vertical;
        this.health = health;
    }

    // Getter/Setter ...
    public int getLength(){
        return length;
    }
    public int getHealth(){
        return health;
    }
    public int getStartX(){
        return startX;
    }
    public int getStartY(){
        return startY;
    }
    public boolean isVertical(){
        return vertical;
    }

}
