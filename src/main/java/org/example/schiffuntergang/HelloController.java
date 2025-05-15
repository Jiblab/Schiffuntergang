package org.example.schiffuntergang;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.*;

public class HelloController {
    private int length;
    @FXML
    private AnchorPane anker;

    @FXML
    private VBox rootPane;

    @FXML
    private HBox boxen;

    @FXML
    VBox boxenV;

    @FXML
    public void initialize() {
        Gamefield enemy = new Gamefield(true, this);
        Gamefield player = new Gamefield(false, this);

        rootPane.getChildren().add(enemy);
        rootPane.getChildren().add(player);

        Button b1 = new Button("1");
        Button b2 = new Button("2");
        Button b3 = new Button("3");
        Button b4 = new Button("4");
        Button b5 = new Button("5");

        b1.setOnAction(e -> length = 1);
        b2.setOnAction(e -> length = 2);
        b3.setOnAction(e -> length = 3);
        b4.setOnAction(e -> length = 4);
        b5.setOnAction(e -> length = 5);

        boxenV.getChildren().add(b1);
        boxenV.getChildren().add(b2);
        boxenV.getChildren().add(b3);
        boxenV.getChildren().add(b4);
        boxenV.getChildren().add(b5);


    }
    public int getLength(){
        return length;
    }


}
