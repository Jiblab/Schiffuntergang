package org.example.schiffuntergang;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.schiffuntergang.Multiplayer.Client;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.Multiplayer.Server;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.components.Ships;
import org.example.schiffuntergang.filemanagement.FileManager;
import org.example.schiffuntergang.filemanagement.GameState;
import org.example.schiffuntergang.filemanagement.SaveDataClass;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;
import javafx.geometry.Insets;

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
    private boolean ishost = false;
    private boolean readyToSendShips = false;
    private Gamefield enemy;
    private Gamefield player;
    private boolean temp = true;
    private String ipa;
    private int porta;
    private int[] shipsAllowed = new int[6];

    MultiplayerLogic mlp;
    private final Label[] availableShipCounters = new Label[6];
    private final Button[] shipLengthButtons = new Button[6];

    @FXML
    private AnchorPane anker;

    @FXML
    private Label messageLabel;

    @FXML
    private BorderPane rootPane;

    @FXML
    private HBox shipControlBox;
    private Label remainingCell;
    //@FXML
    //VBox boxenV;

    private SaveDataClass savedata;

    @FXML
    private ImageView backgroundImage;
    @FXML
    public void initialize() {
        Image img = new Image(getClass().getResource("/images/gamebg.png").toExternalForm());
        backgroundImage.setImage(img);

        backgroundImage.setPreserveRatio(false);

        backgroundImage.setFitWidth(anker.getPrefWidth());
        backgroundImage.setFitHeight(anker.getPrefHeight());

        anker.widthProperty().addListener((obs, oldVal, newVal) -> {
            backgroundImage.setFitWidth(newVal.doubleValue());
        });
        anker.heightProperty().addListener((obs, oldVal, newVal) -> {
            backgroundImage.setFitHeight(newVal.doubleValue());
        });
        anker.getStylesheets().add(getClass().getResource("/button.css").toExternalForm());
    }


    public void setMessage(String msg) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
        }
    }

    public void updateRemainingCellsDisplay() {
        if (player == null || remainingCell == null) {
            return;
        }

        int maxCells = (int) player.maxShipsC();
        int usedCells = player.getUsedCells();
        int remainingCells = maxCells - usedCells;

        remainingCell.setText("Verbleibende Bau-Punkte: " + remainingCells);
    }
    public void setup(){
        // Create the game fields
        remainingCell = new Label();
        remainingCell.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: #0013b3;");

        player = new Gamefield(false, this, (int) x, (int) y);
        EnemyPlayer en = new EnemyPlayer(player);
        enemy = new Gamefield(true, this, (int) x, (int) y, en);



        updateRemainingCellsDisplay();


        if (shipControlBox != null) {
            shipControlBox.getChildren().add(0, remainingCell);
        }

        // VBox für die Spielfelder mit label
        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox enemyBox = new VBox(10, enemyLabel, enemy);
        enemyBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("You");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox playerBox = new VBox(10, playerLabel, player);
        playerBox.setAlignment(Pos.CENTER);


        HBox spielefeldbox = new HBox(20, enemyBox, playerBox);
        spielefeldbox.setAlignment(Pos.CENTER);
        spielefeldbox.setPadding(new Insets(20));
        rootPane.setCenter(spielefeldbox);

        // enemy ships random setzen
        while (enemy.getUsedCells() <= enemy.maxShipsC()) {
            int shipLength = 2 + rand.nextInt(4); // Creates ships of length 2, 3, 4, or 5
            boolean vertical = rand.nextBoolean();

            // Correctly calculate max coordinates to prevent ships from going out of bounds
            int xMax = (int) x - (vertical ? 1 : shipLength);
            int yMax = (int) y - (vertical ? shipLength : 1);

            if (xMax < 0 || yMax < 0) continue; // Skip if a ship can't fit at all

            int x2 = rand.nextInt(xMax + 1);
            int y2 = rand.nextInt(yMax + 1);

            Ships ship = new Ships(shipLength, shipLength);
            if (enemy.placeShip(ship, x2, y2, vertical)) {
                enemy.increaseCells(shipLength);
            }
        }
        setButtons();
            /*// Ensure game fields resize properly
            VBox.setVgrow(enemy, Priority.ALWAYS);
            VBox.setVgrow(player, Priority.ALWAYS);
            enemy.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            player.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
*/

    }

    public void setup(Gamefield loadedplayer, Gamefield loadedenemy){
        // Create the game fields
        remainingCell = new Label();
        remainingCell.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: #0013b3;");

        player = loadedplayer;
        EnemyPlayer en = new EnemyPlayer(player);
        enemy = loadedenemy;
        enemy.setEnemy(en);



        updateRemainingCellsDisplay();


        if (shipControlBox != null) {
            shipControlBox.getChildren().add(0, remainingCell);
        }

        // VBox für die Spielfelder mit label
        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox enemyBox = new VBox(10, enemyLabel, enemy);
        enemyBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("You");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox playerBox = new VBox(10, playerLabel, player);
        playerBox.setAlignment(Pos.CENTER);


        HBox spielefeldbox = new HBox(20, enemyBox, playerBox);
        spielefeldbox.setAlignment(Pos.CENTER);
        spielefeldbox.setPadding(new Insets(20));
        rootPane.setCenter(spielefeldbox);


        setButtons();
            /*// Ensure game fields resize properly
            VBox.setVgrow(enemy, Priority.ALWAYS);
            VBox.setVgrow(player, Priority.ALWAYS);
            enemy.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            player.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
*/

    }

    public void setupMultiC(String ip, int port){
        ishost = false;
        ipa = ip;
        porta = port;
        Client ce = new Client();
        mlp = new MultiplayerLogic(ce, true, null, null);
        mlp.setController(this);
        new Thread(() -> {
            try {
                mlp.start(); // alles Netzwerk-Zeug → eigener Thread
            } catch(IOException e){
                System.out.println("IOException");
            }
        }).start();


        //while(temp){

        //}

      /*  rootPane.getChildren().add(enemy);
        rootPane.getChildren().add(player);


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
        }
        try{
            mlp.start();
        } catch(IOException e){
            System.out.println("IOException");
        }*/

    }

    public void setupMultiS(){
        ishost = true;

        Server se = new Server();
        mlp = new MultiplayerLogic(se, false, null, null);
        player = new Gamefield(false, this, (int) x, (int) y, mlp);
        enemy = new Gamefield(true, this, (int) x, (int) y, mlp);
        mlp.setEn(enemy);
        mlp.setPl(player);

        remainingCell = new Label();
        remainingCell.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: #0013b3;");

        // 2. Rufe die Update-Methode einmal auf, um den Startwert anzuzeigen
        updateRemainingCellsDisplay();

        // 3. Füge das Label zur shipControlBox hinzu
        if (shipControlBox != null) {
            shipControlBox.getChildren().clear(); // Entfernt alle alten Buttons, falls welche da sind
            shipControlBox.getChildren().add(remainingCell);
        }

        setButtons();

        /*for (int i = 2; i <= 5; i++) {
            final int len = i; // final für die Verwendung im Lambda
            Button b = new Button("Länge " + len);
            b.getStyleClass().add("option-button");
            b.setOnAction(e -> {
                length = len;
            });

            if (shipControlBox != null) {
                shipControlBox.getChildren().add(b);
            }
        }*/
        // rootPane.getChildren().add(player);
        //rootPane.getChildren().add(enemy);
        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox enemyBox = new VBox(10, enemyLabel, enemy);
        enemyBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("You");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox playerBox = new VBox(10, playerLabel, player);
        playerBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Spiel speichern");
        saveBtn.setOnAction(e -> {
            //StorageManager.saveFullGame(player, enemy, 0.8, true, true, "spielstand1");
            FileManager file = new FileManager(true);

        });
        shipControlBox.getChildren().add(saveBtn);

        HBox spielefeldbox = new HBox(20, enemyBox, playerBox);
        spielefeldbox.setAlignment(Pos.CENTER);
        spielefeldbox.setPadding(new Insets(20));
        rootPane.setCenter(spielefeldbox);
        new Thread(() ->{
            try {
                mlp.start(); // alles Netzwerk-Zeug → eigener Thread
            } catch(IOException e){
                System.out.println("IOException");
            }
        }).start();
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

    /*public boolean canPlaceShipOfLength(int len) {
        return shipsPlaced[len] < maxPerShipLength;
    }*/

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
    public boolean getHost(){
        return ishost;
    }

    public void setShipCountsFromNetwork(int[] lengths) {
        for (int len = 1; len < lengths.length ; len++) {
            shipsAllowed[len] = lengths[len];
            final int lengleng = len;
            Platform.runLater(() -> updateAllowedCounter(lengleng));
        }
    }
    private void updateAllowedCounter(int len) {
        //shipCounters[len].setText(shipsPlaced[len] + " / " + shipsAllowed[len]);
    }
    public boolean getReady(){
        return readyToSendShips;
    }

    @FXML
    private void onReadyClicked() throws IOException {
    /*if (enemy != null && enemy.hasShip()){
        this.readyToSendShips = true;
        mlp.sendShips();
    }

    if (player != null && player.hasShip()) {
        // Nur wenn mindestens ein Schiff platziert wurde:
        this.readyToSendShips = true;

        if (mlp != null) {
            mlp.sendShips();
            System.out.println("Fertig! Schiffe werden gesendet.");
        }

        // Optional: Nachricht für den Spieler ändern
        if (messageLabel != null) {
            messageLabel.setText("Warte auf Gegner...");
        }

    } else {
        // Wenn keine Schiffe platziert wurden:
        System.out.println("Fehler: Bitte platziere zuerst mindestens ein Schiff!");

        // Visuelles Feedback für den Spieler geben
        if (messageLabel != null) {
            messageLabel.setText("Platziere zuerst ein Schiff!");
        }

    }*/
        readyToSendShips = true;
        mlp.sendShips();
    }

    public void loadGameFromSave(GameState loadedstate){
        player = Gamefield.fromData(loadedstate.getPlayerBoardData(), this, this.mlp);
        enemy = Gamefield.fromData(loadedstate.getEnemyBoardData(), this, this.mlp);
        loadGame(player, enemy);
        setup();
    }

    public void loadGame(Gamefield playerBoard, Gamefield enemyBoard) {
        this.rootPane.getChildren().clear();

        // Boards zur Anzeige hinzufügen
        rootPane.getChildren().add(enemyBoard);
        rootPane.getChildren().add(playerBoard);


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

    private void setButtons(){
        Button b2 = new Button("Länge 2");
        Button b3 = new Button("Länge 3");
        Button b4 = new Button("Länge 4");
        Button b5 = new Button("Länge 5");
        Button d = new Button("Horizontal");
        Button d2 = new Button("Vertikal");
        Button back = new Button("Back to Start");
        Button speichern = new Button("Speichern");
        Button laden = new Button("Laden");

        b2.getStyleClass().add("option-button");
        b3.getStyleClass().add("option-button");
        b4.getStyleClass().add("option-button");
        b5.getStyleClass().add("option-button");
        d.getStyleClass().add("option-button");
        d2.getStyleClass().add("option-button");
        speichern.getStyleClass().add("control-button");
        laden.getStyleClass().add("control-button");


        back.getStyleClass().add("control-button");

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
        speichern.setOnAction(e ->{
            savedata = new SaveDataClass(player, enemy);
            savedata.prepareData();
            FileManager fileManager = new FileManager(true);
            fileManager.save(savedata);
        });
        laden.setOnAction(e->{
            FileManager fileManager = new FileManager(true);
            try {
                GameState loadedstate = fileManager.load();
                loadGameFromSave(loadedstate);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        shipControlBox.getChildren().addAll(b2, b3, b4, b5, d, d2, back, speichern, laden);
        shipControlBox.setSpacing(10);
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
        return c != null;
    }

    public String getIP(){
        return ipa;
    }
    public int getPort(){
        return porta;
    }

    public void setupGameMult(Gamefield pl, Gamefield en){

        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox enemyBox = new VBox(10, enemyLabel, en);
        enemyBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("You");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox playerBox = new VBox(10, playerLabel, pl);
        playerBox.setAlignment(Pos.CENTER);



        HBox spielefeldbox = new HBox(20, enemyBox, playerBox);
        spielefeldbox.setAlignment(Pos.CENTER);
        spielefeldbox.setPadding(new Insets(20));
        rootPane.setCenter(spielefeldbox);

        setButtons();


        for (int i = 2; i <= 5; i++) {
            Label counter = new Label("Empfangen: 0");
            shipCounters[i] = counter;

            HBox row = new HBox(10, new Label("Länge " + i + ":"), counter);
            row.setAlignment(Pos.CENTER);
            shipControlBox.getChildren().add(row);
        }
    }

    public void setupClientPlacementUI(int[] shipCounts) {
        // Lösche die alte Button-Leiste, um sie neu aufzubauen
        if (shipControlBox != null) {
            shipControlBox.getChildren().clear();
            shipControlBox.setSpacing(10);
        } else {
            // Fallback, falls die FXML-Komponente nicht geladen wurde
            System.err.println("shipControlBox ist null. UI kann nicht aufgebaut werden.");
            return;
        }

        // Gehe durch die möglichen Schiffslängen (2 bis 5)
        for (int len = 2; len <= 5; len++) {
            int count = shipCounts[len]; // Hole die erlaubte Anzahl für diese Länge

            // Wenn von dieser Länge 0 Schiffe erlaubt sind, erstelle gar keine UI dafür
            if (count == 0) {
                continue;
            }

            // --- UI-Komponenten erstellen ---

            // 1. Das "Verfügbar"-Label
            Label availableLabel = new Label("Verfügbar: " + count);
            availableLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");
            this.availableShipCounters[len] = availableLabel; // Speichere das Label für spätere Updates

            // 2. Der Button zur Auswahl der Schiffslänge
            Button lengthButton = new Button("Länge " + len);
            lengthButton.getStyleClass().add("option-button");
            final int currentLength = len; // Finale Kopie für Lambda
            lengthButton.setOnAction(e -> {
                length = currentLength;
            });
            this.shipLengthButtons[len] = lengthButton; // Speichere den Button zum Deaktivieren

            // 3. Eine VBox, um Label und Button übereinander zu stapeln
            VBox shipControlGroup = new VBox(5, availableLabel, lengthButton);
            shipControlGroup.setAlignment(Pos.CENTER);

            // Füge die Gruppe zur Haupt-Control-Box hinzu
            shipControlBox.getChildren().add(shipControlGroup);
        }

        // Füge die restlichen, allgemeinen Buttons hinzu (Richtung, Zurück etc.)
        Button d = new Button("Horizontal");
        Button d2 = new Button("Vertikal");
        Button back = new Button("Back to Start");
        d.getStyleClass().add("option-button");
        d2.getStyleClass().add("option-button");
        back.getStyleClass().add("control-button");
        d.setOnAction(e -> direction = false);
        d2.setOnAction(e -> direction = true);
        back.setOnAction(e -> { new StartScreen(stage).show(); });

        shipControlBox.getChildren().addAll(d, d2, back);
    }

    public void clientPlacedShip(int len) {
        if (len <= 0 || len >= shipsPlaced.length) return;

        // Zähle das platzierte Schiff
        shipsPlaced[len]++;

        // Berechne, wie viele von dieser Sorte noch übrig sind
        int allowedCount = shipsAllowed[len]; // Diese Info kommt vom Server
        int remaining = allowedCount - shipsPlaced[len];

        // Aktualisiere das "Verfügbar"-Label
        if (availableShipCounters[len] != null) {
            availableShipCounters[len].setText("Verfügbar: " + remaining);
        }

        // Wenn keine mehr übrig sind, deaktiviere den Button
        if (remaining <= 0) {
            if (shipLengthButtons[len] != null) {
                shipLengthButtons[len].setDisable(true);
            }
        }
    }
    public void setShipRules(int[] shipCounts) {
        this.shipsAllowed = shipCounts;
    }

    public boolean canClientPlaceShip(int len) {
        if (len <= 0 || len >= shipsAllowed.length) {
            return false;
        }
        // Ist die Anzahl der bereits platzierten Schiffe dieser Länge
        // kleiner als die erlaubte Anzahl?
        return shipsPlaced[len] < shipsAllowed[len];
    }
}
