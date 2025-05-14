package org.example.schiffuntergang;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.*;

public class HelloController {
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
        Gamefield enemy = new Gamefield(true);
        Gamefield player = new Gamefield(false);

        rootPane.getChildren().add(enemy);
        rootPane.getChildren().add(player);
        for (int i = 0; i < 5; i++){
            Button b = new Button("hund");
            boxenV.getChildren().add(b);
        }



    }


}
