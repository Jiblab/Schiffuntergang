/*package org.example.schiffuntergang;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.w3c.dom.events.MouseEvent;
import javafx.geometry.Point2D;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;

public class Gamefield extends Parent {
    //private Ships ship [] = new Ships[];
    private int schiffe;
    private int lang;
    private int breit;



    public Gamefield(int l, int b, EventHandler< ? super MouseEvent> handler){
        for (int i = 0; i < l; i++){
            HBox row = new HBox();
            for(int j = 0; j < b; j++){
              Cell c = new Cell(j, i, this, lang, breit);
              c.setOnMouseClicked(handler);
              row.getChildren().add(c);

            }
        }


    }


}
*/