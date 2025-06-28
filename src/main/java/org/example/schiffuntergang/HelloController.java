package org.example.schiffuntergang;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.schiffuntergang.Multiplayer.Client;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.Multiplayer.Server;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;

import java.io.IOException;
import java.util.Random;

public class HelloController {
    private int length;
    private boolean direction;
    private double x;
    private double y;
    private Stage stage;
    Random rand = new Random();
    private boolean playerturn = true;
    private Client c;
    private Server s;
    private final int maxPerShipLength = 3;
    private final int[] shipsPlaced = new int[6]; // Index = Schiffslänge
    private final Label[] shipCounters = new Label[6];
    private boolean isClientMode = false;
    private boolean readyToSendShips = false;
    private Gamefield enemy;
    private Gamefield player;
    private boolean temp;
    private String ipa;
    private int porta;

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

        player = new Gamefield(false, this, (int) x, (int) y);
        EnemyPlayer en = new EnemyPlayer(player);
        enemy = new Gamefield(true, this, (int) x, (int) y, en);





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


       setButtons();

    }

    public void setupMultiC(String ip, int port){
        ipa = ip;
        porta = port;
        Client ce = new Client();
        MultiplayerLogic mlp = new MultiplayerLogic(ce, true, null, null);
        mlp.setController(this);
        while(temp){

        }

        rootPane.getChildren().add(enemy);
        rootPane.getChildren().add(player);
        rootPane.setAlignment(Pos.CENTER);

        VBox.setVgrow(enemy, Priority.ALWAYS);
        VBox.setVgrow(player, Priority.ALWAYS);
        enemy.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        player.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        setButtons();


        for (int i = 2; i <= 5; i++) {
            Label counter = new Label("Empfangen: 0");
            shipCounters[i] = counter;

            HBox row = new HBox(10, new Label("Länge " + i + ":"), counter);
            row.setAlignment(Pos.CENTER);
            boxenV.getChildren().add(row);
        }
        try{
            mlp.start();
        } catch(IOException e){
            System.out.println("IOException");
        }

    }

    public void setupMultiS(){
        isClientMode = true;

        Server se = new Server();
        MultiplayerLogic mlp = new MultiplayerLogic(se, false, null, null);
        player = new Gamefield(false, this, (int) x, (int) y, mlp);
        enemy = new Gamefield(true, this, (int) x, (int) y, mlp);
        mlp.setEn(enemy);
        mlp.setPl(player);


        setButtons();

        for (int i = 2; i <= 5; i++) {
            int len = i;
            Button b = new Button("Länge " + len);
            b.setOnAction(e -> {
                if (canPlaceShipOfLength(len)) {
                    length = len;
                }
            });

            Label counter = new Label("Verbleibend: " + maxPerShipLength);
            shipCounters[len] = counter;

            HBox row = new HBox(10, b, counter);
            row.setAlignment(Pos.CENTER);
            boxenV.getChildren().add(row);
            try{
                mlp.start();
            } catch(IOException e){
                System.out.println("IOException");
            }
        }




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
        playerturn = !playerturn;
    }

    public boolean canPlaceShipOfLength(int len) {
        return shipsPlaced[len] < maxPerShipLength;
    }

    public void shipPlaced(int len) {
        shipsPlaced[len]++;
        updateCounter(len);
    }

    private void updateCounter(int len) {
        if (shipCounters[len] != null) {
            int remaining = maxPerShipLength - shipsPlaced[len];
            shipCounters[len].setText("Verbleibend: " + remaining);

        }
    }
    public boolean isClientMode(){
        return isClientMode;
    }

    public void setShipCountsFromNetwork(int[] lengths) {
        for (int len : lengths) {
            if (len >= 1 && len < shipsPlaced.length) {
                shipsPlaced[len]++;
                updateCounter(len);
            }
        }
    }

    public boolean getReady(){
        return readyToSendShips;
    }

    @FXML
    private void onReadyClicked() {
        readyToSendShips = true;
        System.out.println("Fertig gedrückt – bereit zum Senden der Schiffe");
    }
    public void loadGame(Gamefield playerBoard, Gamefield enemyBoard) {
        this.rootPane.getChildren().clear();

        // Boards zur Anzeige hinzufügen
        rootPane.getChildren().add(enemyBoard);
        rootPane.getChildren().add(playerBoard);
        rootPane.setAlignment(Pos.CENTER);

        VBox.setVgrow(enemyBoard, Priority.ALWAYS);
        VBox.setVgrow(playerBoard, Priority.ALWAYS);
        enemyBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        playerBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Optional: Musik- und Soundeinstellungen anwenden
        BackgroundMusic.getInstance().setVolume(playerBoard.getMusicVolume());
        SoundEffect.setVolume(playerBoard.getMusicVolume());

        if (!playerBoard.isMusicEnabled())
            BackgroundMusic.getInstance().stop();
    }

    public void setBoard(Gamefield e, Gamefield p){
        player = p;
        enemy = e;
    }

    private void setButtons(){
        Button b2 = new Button("Länge 2");
        Button b3 = new Button("Länge 3");
        Button b4 = new Button("Länge 4");
        Button b5 = new Button("Länge 5");
        Button d = new Button("Vertikal");
        Button d2 = new Button("Horizental");
        Button back = new Button("Back to Start");

        b2.setOnAction(e -> length = 2);
        b3.setOnAction(e -> length = 3);
        b4.setOnAction(e -> length = 4);
        b5.setOnAction(e -> length = 5);
        d.setOnAction(e->direction = false);
        d2.setOnAction(e->direction = true);
        back.setOnAction(e -> {
                    StartScreen startScreen = new StartScreen(stage);
                    startScreen.show();
                }
        );

        boxenV.getChildren().add(b2);
        boxenV.getChildren().add(b3);
        boxenV.getChildren().add(b4);
        boxenV.getChildren().add(b5);
        boxenV.getChildren().add(d);
        boxenV.getChildren().add(d2);
        boxenV.getChildren().add(back);
        boxenV.setAlignment(Pos.CENTER);
    }

    public void temp(){
        temp = false;
    }

    public boolean checkIfAllShipsPlaced() {
        for (int i = 2; i <= 5; i++) {
            if (shipCounters[i] != null) {
                String text = shipCounters[i].getText();
                // Extrahiere die Zahl aus dem Text (z. B. "Verbleibend: 1")
                String numberStr = text.replaceAll("[^0-9]", "");
                int number = numberStr.isEmpty() ? 0 : Integer.parseInt(numberStr);

                if (number > 0) {
                    return false; // Noch nicht alle auf 0
                }
            }
        }

        // Wenn wir hier sind, sind alle bei 0 → Nachricht senden
        if (c != null) {
            return true;

        }
        return false;
    }

    public String getIP(){
        return ipa;
    }
    public int getPort(){
        return porta;
    }
}
