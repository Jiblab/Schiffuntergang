package org.example.schiffuntergang;

import javafx.scene.Parent;

public class Ships extends Parent {
    private int length;
    private int health;
    boolean vertical = true;




    public Ships(int l, int h){
        length = l;
        health = h;
    }

    public void hit(){
        health--;
        if(health == 0){
            System.out.println("Schiff versenkt");
        }
    }

    public boolean isAlive(){
        if(health == 0){
            return false;
        }
        return true;
    }

    public int getLength(){
        return length;
    }

    public boolean getDirection(){
        return vertical;
    }

}
