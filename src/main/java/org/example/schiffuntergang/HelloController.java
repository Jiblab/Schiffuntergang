package org.example.schiffuntergang;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.sounds.*;

import java.util.Random;

public class HelloController {
    private int length;
    private boolean direction;
    private double x;
    private double y;
    private Stage stage;
    Random rand = new Random();
    private boolean playerturn = true;

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

    }
    public void setup(){
        Gamefield player = new Gamefield(false, this, (int) x, (int) y);
        EnemyPlayer en = new EnemyPlayer(player, this);
        Gamefield enemy = new Gamefield(true, this, (int) x, (int) y, en);




        rootPane.getChildren().add(enemy);
        rootPane.getChildren().add(player);
        rootPane.setAlignment(Pos.CENTER);
       //random platzieren der gegnerschiffe
        while(enemy.getUsedCells() <= enemy.maxShipsC()){
            int shipLength = 2 + rand.nextInt(4);
            boolean vertical = rand.nextBoolean();

// ACHTUNG: Breite = x, Höhe = y
            int xMax = (int) x - (vertical ? 1 : shipLength);
            int yMax = (int) y - (vertical ? shipLength : 1);

            int x2 = rand.nextInt(xMax + 1);
            int y2 = rand.nextInt(yMax + 1);

            Ships ship = new Ships(shipLength, shipLength);

            if (enemy.placeShip(ship, x2, y2, vertical )){
                enemy.increaseCells(shipLength);
            }

        }


        VBox.setVgrow(enemy, Priority.ALWAYS);
        VBox.setVgrow(player, Priority.ALWAYS);
        enemy.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        player.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        Button b2 = new Button("Länge 2");
        Button b3 = new Button("Länge 3");
        Button b4 = new Button("Länge 4");
        Button b5 = new Button("Länge 5");
        Button d = new Button("Vertikal");
        Button d2 = new Button("Horizental");

        b2.setOnAction(e -> length = 2);
        b3.setOnAction(e -> length = 3);
        b4.setOnAction(e -> length = 4);
        b5.setOnAction(e -> length = 5);
        d.setOnAction(e->direction = false);
        d2.setOnAction(e->direction = true);

        boxenV.getChildren().add(b2);
        boxenV.getChildren().add(b3);
        boxenV.getChildren().add(b4);
        boxenV.getChildren().add(b5);
        boxenV.getChildren().add(d);
        boxenV.getChildren().add(d2);
        boxenV.setAlignment(Pos.CENTER);


    }

    public int getLength(){
        return length;
    }
    public boolean getDirection(){
        return direction;
    }

    public void setSize(double x1, double y1){
        x = x1;
        y = y1;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                stage.setFullScreen(false);
            }
        });
    }

    public boolean getPlayerturn(){
        return playerturn;
    }

    public void setPlayerturn(){
        if (!playerturn){
            playerturn = true;
        }
        else {
            playerturn = false;
        }
    }
    public void loadGame(Gamefield playerBoard, Gamefield aiBoard) {
        this.rootPane.getChildren().clear();

        // Boards zur Anzeige hinzufügen
        rootPane.getChildren().add(aiBoard);
        rootPane.getChildren().add(playerBoard);
        rootPane.setAlignment(Pos.CENTER);

        VBox.setVgrow(aiBoard, Priority.ALWAYS);
        VBox.setVgrow(playerBoard, Priority.ALWAYS);
        aiBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        playerBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Optional: Musik- und Soundeinstellungen anwenden
        BackgroundMusic.getInstance().setVolume(playerBoard.getMusicVolume());
        SoundEffect.setVolume(playerBoard.getMusicVolume());

        if (!playerBoard.isMusicEnabled()) {
            BackgroundMusic.getInstance().stop();
        }
    }
}