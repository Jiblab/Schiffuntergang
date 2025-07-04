package org.example.schiffuntergang.components;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;

public class Ships extends Parent {
    private final int length;
    private int health;
    private boolean vertical = true;
    private ImageView shipImageView; //

    public Ships(int l, int h){
        this.length = l;
        this.health = h;
    }
    public void hit(){
        health--;
        if(health == 0){
            System.out.println("Schiff versenkt");
        }
    }
    public boolean isAlive() {
        return health > 0;
    }
    public int getLength(){
        return length;
    }
    public boolean getDirection(){
        return vertical;
    }
    public void setDirection(boolean vertical) {
        this.vertical = vertical;
    }
    public int getHealth() {
        return health;
    }

    public ImageView getShipImageView() {
        return shipImageView;
    }

    public void setShipImageView(ImageView shipImageView) {
        this.shipImageView = shipImageView;
    }
}