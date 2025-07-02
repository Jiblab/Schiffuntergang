package org.example.schiffuntergang;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import java.io.IOException;
import java.util.Random;

public class HelloController {
    private final int[] shipsPlaced = new int[6];
    private boolean direction;
    private double x;
    private double y;
    private Stage stage;
    Random rand = new Random();
    private boolean playerturn = true;
    private Client c;
    private Server s;
    private final int maxPerShipLength = 3;
    // Member-Variablen (unverändert)
    private int length;
    private final Label[] shipCounters = new Label[6];
    private boolean ishost = false;
    private boolean readyToSendShips = false;
    private Gamefield enemy;
    private Gamefield player;
    private final boolean temp = true;
    private String ipa;
    private int porta;
    private int[] shipsAllowed = new int[6];
    private Label remainingCell;

    private Label notificationLabel;
    private PauseTransition notificationTimer;
    MultiplayerLogic mlp;
    private final Label[] availableShipCounters = new Label[6];
    private final Button[] shipLengthButtons = new Button[6];

    private SaveDataClass savedata;

    // FXML-Referenzen
    @FXML
    private AnchorPane anker;
    @FXML
    private BorderPane rootPane;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Label messageLabel; // Falls Sie dieses Label noch verwenden

    @FXML
    public void initialize() {
        Image img = new Image(getClass().getResource("/images/gamebg.png").toExternalForm());
        backgroundImage.setImage(img);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.fitWidthProperty().bind(anker.widthProperty());
        backgroundImage.fitHeightProperty().bind(anker.heightProperty());
        anker.getStylesheets().add(getClass().getResource("/button.css").toExternalForm());
    }

    /* Diese Methode wird vom Gamefield aufgerufen, wenn ein Schiff platziert wird.
     */
    public void updateRemainingCellsDisplay() {
        // Sicherheitsprüfung, falls die Methode zu früh aufgerufen wird
        if (player == null || remainingCell == null) {
            return;
        }

        int maxCells = (int) player.maxShipsC();
        int usedCells = player.getUsedCells();
        int remainingCells = maxCells - usedCells;

        // Den Text im UI-Thread aktualisieren, um Fehler zu vermeiden
        Platform.runLater(() -> {
            remainingCell.setText("Bau-Punkte: " + remainingCells);
        });
    }

    // --- NEUE LAYOUT-STRUKTUR ---

    /**
     * Baut die gesamte Benutzeroberfläche mit den übergebenen Spielfeldern auf.
     * Diese Methode ist der zentrale Punkt für den UI-Aufbau.
     *
     * @param playerBoard Das Spielfeld des Spielers.
     * @param enemyBoard  Das Spielfeld des Gegners.
     * @param controlNode Das linke Steuerpanel (kann je nach Spielmodus variieren).
     */
    private void buildUI(Node playerBoard, Node enemyBoard, Node controlNode) {
        // 1. Header (oben)
        rootPane.setTop(createHeader());

        // 2. Spielfelder (Mitte)
        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox enemyBox = new VBox(10, enemyLabel, enemyBoard);
        enemyBox.setAlignment(Pos.CENTER);

        Label playerLabel = new Label("You");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 16px; -fx-text-fill: #0013b3;");
        VBox playerBox = new VBox(10, playerLabel, playerBoard);
        playerBox.setAlignment(Pos.CENTER);

        HBox gameFieldsBox = new HBox(30, playerBox, enemyBox); // Player links, Enemy rechts
        gameFieldsBox.setAlignment(Pos.CENTER);
        gameFieldsBox.setPadding(new Insets(20));
        rootPane.setCenter(gameFieldsBox);

        // 3. Steuerleiste (links)
        rootPane.setLeft(controlNode);

        //Messages für treffer, verfehlt, etc
        notificationLabel = new Label(); // Label initialisieren
        notificationLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 18px; -fx-padding: 10px;");

        HBox notificationBox = new HBox(notificationLabel);
        notificationBox.setAlignment(Pos.CENTER);
        notificationBox.setMinHeight(50); // Gibt der Leiste eine feste Höhe

        rootPane.setBottom(notificationBox);
    }

    public void showNotification(String message, String type) {
        Platform.runLater(() -> {
            // Stoppt einen laufenden Timer, falls eine neue Nachricht schnell hinterherkommt
            if (notificationTimer != null) {
                notificationTimer.stop();
            }

            // Setzt Text und Farbe basierend auf dem Typ
            notificationLabel.setText(message);
            switch (type.toLowerCase()) {
                case "hit":
                    notificationLabel.setStyle("-fx-text-fill: #ffbf00; -fx-font-family: 'Press Start 2P'; -fx-font-size: 18px;"); // Gelb/Orange für Treffer
                    break;
                case "sunk":
                    notificationLabel.setStyle("-fx-text-fill: #ff4d4d; -fx-font-family: 'Press Start 2P'; -fx-font-size: 18px; -fx-font-weight: bold;"); // Rot für Versenkt
                    break;
                case "miss":
                    notificationLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-family: 'Press Start 2P'; -fx-font-size: 18px;"); // Grau für Fehlschuss
                    break;
                default: // "info" oder unbekannt
                    notificationLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Press Start 2P'; -fx-font-size: 18px;"); // Weiß für allgemeine Infos
                    break;
            }

            // Timer starten, um die Nachricht auszublenden
            notificationTimer = new PauseTransition(Duration.seconds(3));
            notificationTimer.setOnFinished(event -> notificationLabel.setText(""));
            notificationTimer.play();
        });
    }

    /**
     * Erstellt den Header mit "Player vs Enemy" und Icons.
     *
     * @return Eine HBox, die als Header dient.
     */
    private HBox createHeader() {
        // WICHTIG: Ersetzen Sie die Pfade durch Ihre tatsächlichen Icon-Pfade!
       /* ImageView playerIcon = new ImageView(new Image(getClass().getResource("/images/player_icon.png").toExternalForm()));
        playerIcon.setFitHeight(40);
        playerIcon.setFitWidth(40);*/

        Label playerLabel = new Label("Player");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");

        Label vsLabel = new Label("vs");
        vsLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: #ffbf00;");

        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");

        /*ImageView enemyIcon = new ImageView(new Image(getClass().getResource("/images/enemy_icon.png").toExternalForm()));
        enemyIcon.setFitHeight(40);
        enemyIcon.setFitWidth(40);*/

        HBox headerBox = new HBox(15, /*playerIcon,*/ playerLabel, vsLabel, enemyLabel/*, enemyIcon*/);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10));
        headerBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        return headerBox;
    }

    /**
     * Erstellt das Standard-Steuerpanel für den Host und den Offline-Modus.
     *
     * @return Eine VBox mit allen Steuerelementen.
     */
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setMinWidth(200);
        controlPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        remainingCell = new Label();
        remainingCell.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px; -fx-text-fill: #a0ff9e; -fx-padding: 0 0 10 0;");
        controlPanel.getChildren().add(remainingCell);


        // Label für die Schiffslängen
        Label shipLengthLabel = new Label("Schiffslänge");
        shipLengthLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: white;");

        // Buttons für die Schiffslängen (unverändert)
        Button b2 = new Button("Länge 2");
        b2.setOnAction(e -> length = 2);
        Button b3 = new Button("Länge 3");
        b3.setOnAction(e -> length = 3);
        Button b4 = new Button("Länge 4");
        b4.setOnAction(e -> length = 4);
        Button b5 = new Button("Länge 5");
        b5.setOnAction(e -> length = 5);

        // Buttons für die Ausrichtung (unverändert)
        Label directionLabel = new Label("Ausrichtung");
        directionLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: white;");
        Button d_horizontal = new Button("Horizontal");
        d_horizontal.setOnAction(e -> direction = false);
        Button d_vertical = new Button("Vertikal");
        d_vertical.setOnAction(e -> direction = true);

        // CSS-Klassen und Breite zuweisen (unverändert)
        for (Button btn : new Button[]{b2, b3, b4, b5, d_horizontal, d_vertical}) {
            btn.getStyleClass().add("option-button");
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        // Spielsteuerungs-Buttons (unverändert)
        Button speichern = new Button("Speichern");
        speichern.getStyleClass().add("control-button");
        speichern.setOnAction(e -> {
            savedata = new SaveDataClass(player, enemy);
            savedata.prepareData();
            FileManager fileManager = new FileManager(true);
            fileManager.save(savedata);
        });

        Button fertigButton = new Button("Fertig");
        fertigButton.getStyleClass().add("control-button");
        fertigButton.setOnAction(e -> {
            try {
                onReadyClicked();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                // Hier wäre ein Alert für den User gut
            }
        });

        Button laden = new Button("Laden");
        laden.getStyleClass().add("control-button");
        laden.setOnAction(e -> {
            FileManager fileManager = new FileManager(true);
            try {
                GameState loadedstate = fileManager.load();
                loadGameFromSave(loadedstate);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button back = new Button("Back to Start");
        back.getStyleClass().add("control-button");
        back.setOnAction(e -> new StartScreen(stage).show());

        for (Button btn : new Button[]{fertigButton, speichern, laden, back}) {
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Elemente zur VBox hinzufügen (Reihenfolge mit neuem Label)
        controlPanel.getChildren().addAll(
                shipLengthLabel, b2, b3, b4, b5,
                directionLabel, d_horizontal, d_vertical,
                spacer,
                fertigButton,
                speichern, laden, back
        );

        return controlPanel;
    }


    // --- ÜBERARBEITETE SETUP-METHODEN ---

    /**
     * Setup für ein Standard-Offline-Spiel.
     */
    public void setup() {
        player = new Gamefield(false, this, (int) x, (int) y);
        EnemyPlayer en = new EnemyPlayer(player);
        enemy = new Gamefield(true, this, (int) x, (int) y, en);
        placeEnemyShipsRandomly();
        buildUI(player, enemy, createControlPanel());
        updateRemainingCellsDisplay();
    }

    /**
     * Setup nach dem Laden eines Spielstands.
     */
    public void setup(Gamefield loadedPlayer, Gamefield loadedEnemy) {
        this.player = loadedPlayer;
        this.enemy = loadedEnemy;
        // Wichtig: Die Controller-Referenz in den geladenen Feldern aktualisieren!
        this.player.setController(this);
        this.enemy.setController(this);
        buildUI(player, enemy, createControlPanel());
        updateRemainingCellsDisplay();
    }

    /**
     * Setup für einen Multiplayer-Client.
     */
    public void setupMultiC(String ip, int port) {
        ishost = false;
        ipa = ip;
        porta = port;
        Client ce = new Client();
        mlp = new MultiplayerLogic(ce, true, null, null);
        mlp.setController(this);

        // Leeres UI initialisieren, während auf Server-Daten gewartet wird
        player = new Gamefield(false, this, (int) x, (int) y, mlp);
        enemy = new Gamefield(true, this, (int) x, (int) y, mlp);
        Label waitingLabel = new Label("Warte auf Server...");
        waitingLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: white;");
        VBox placeholderControls = new VBox(waitingLabel);
        placeholderControls.setPadding(new Insets(20));
        placeholderControls.setAlignment(Pos.CENTER);
        buildUI(player, enemy, placeholderControls);
        updateRemainingCellsDisplay();

        new Thread(() -> {
            try {
                mlp.start();
            } catch (IOException e) {
                System.out.println("IOException im Client-Thread: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Setup für einen Multiplayer-Server (Host).
     */
    public void setupMultiS() {
        ishost = true;

        Server se = new Server();
        mlp = new MultiplayerLogic(se, false, null, null);
        player = new Gamefield(false, this, (int) x, (int) y, mlp);
        enemy = new Gamefield(true, this, (int) x, (int) y, mlp);
        mlp.setEn(enemy);
        mlp.setPl(player);

        buildUI(player, enemy, createControlPanel());
        updateRemainingCellsDisplay();
        new Thread(() -> {
            try {
                mlp.start();
            } catch (IOException e) {
                System.out.println("IOException im Server-Thread: " + e.getMessage());
            }
        }).start();
    }

    public void setupMultiplayerBoards(Gamefield playerBoard, Gamefield enemyBoard) {
        // Die Spielfelder des Controllers auf die neuen Objekte setzen
        this.player = playerBoard;
        this.enemy = enemyBoard;

        // Ein Platzhalter-Panel für die linke Seite erstellen,
        // während wir auf die Schiffsregeln vom Server warten.
        Label waitingLabel = new Label("Warte auf Schiffsregeln...");
        waitingLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px; -fx-text-fill: white;");
        VBox placeholderControls = new VBox(waitingLabel);
        placeholderControls.setPadding(new Insets(20));
        placeholderControls.setAlignment(Pos.CENTER);
        placeholderControls.setMinWidth(200);
        placeholderControls.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        // Die zentrale UI-Aufbau-Methode mit den neuen Spielfeldern und dem Platzhalter aufrufen
        buildUI(this.player, this.enemy, placeholderControls);
    }

    /**
     * Baut das Steuerpanel für den Client, nachdem die Schiffsregeln vom Server empfangen wurden.
     */
    public void setupClientPlacementUI(int[] shipCounts) {
        this.shipsAllowed = shipCounts;

        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setMinWidth(200);
        controlPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        for (int len = 2; len <= 5; len++) {
            if (shipCounts[len] > 0) {
                Label availableLabel = new Label("Verfügbar: " + shipCounts[len]);
                availableLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px; -fx-text-fill: white;");
                this.availableShipCounters[len] = availableLabel;

                Button lengthButton = new Button("Länge " + len);
                lengthButton.getStyleClass().add("option-button");
                final int currentLength = len;
                lengthButton.setOnAction(e -> this.length = currentLength);
                lengthButton.setMaxWidth(Double.MAX_VALUE);
                this.shipLengthButtons[len] = lengthButton;

                VBox shipGroup = new VBox(5, new Label("Schiffslänge " + len), availableLabel, lengthButton);
                shipGroup.setAlignment(Pos.CENTER);
                controlPanel.getChildren().add(shipGroup);
            }
        }

        // Ausrichtungs-Buttons
        Button d_horizontal = new Button("Horizontal");
        d_horizontal.setOnAction(e -> direction = false);
        Button d_vertical = new Button("Vertikal");
        d_vertical.setOnAction(e -> direction = true);
        d_horizontal.getStyleClass().add("option-button");
        d_vertical.getStyleClass().add("option-button");
        d_horizontal.setMaxWidth(Double.MAX_VALUE);
        d_vertical.setMaxWidth(Double.MAX_VALUE);

        // Ready-Button
        Button readyButton = new Button("Fertig");
        readyButton.getStyleClass().add("control-button");
        readyButton.setMaxWidth(Double.MAX_VALUE);
        readyButton.setOnAction(e -> {
            try {
                onReadyClicked();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        Button fertigButton = new Button("Fertig");
        fertigButton.getStyleClass().add("control-button");
        fertigButton.setMaxWidth(Double.MAX_VALUE);
        fertigButton.setOnAction(e -> {
            try {
                onReadyClicked();
                fertigButton.setDisable(true); // Verhindert doppeltes Klicken
                fertigButton.setText("Warte auf Host...");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        controlPanel.getChildren().addAll(spacer, d_horizontal, d_vertical, readyButton);

        // Ersetze das "Warte..."-Panel durch das richtige Steuerpanel
        Platform.runLater(() -> rootPane.setLeft(controlPanel));
    }


    // --- RESTLICHER CODE (größtenteils unverändert) ---

    private void placeEnemyShipsRandomly() {
        while (enemy.getUsedCells() <= enemy.maxShipsC()) {
            int shipLength = 2 + rand.nextInt(4);
            boolean vertical = rand.nextBoolean();
            int xMax = (int) x - (vertical ? 1 : shipLength);
            int yMax = (int) y - (vertical ? shipLength : 1);
            if (xMax < 0 || yMax < 0) continue;
            int x2 = rand.nextInt(xMax + 1);
            int y2 = rand.nextInt(yMax + 1);
            Ships ship = new Ships(shipLength, shipLength);
            if (enemy.placeShip(ship, x2, y2, vertical)) {
                enemy.increaseCells(shipLength);
            }
        }
    }

    public void loadGameFromSave(GameState loadedState) {
        Gamefield loadedPlayer = Gamefield.fromData(loadedState.getPlayerBoardData(), this, this.mlp);
        Gamefield loadedEnemy = Gamefield.fromData(loadedState.getEnemyBoardData(), this, this.mlp);

        // Benutze die dedizierte Setup-Methode für geladene Spiele
        setup(loadedPlayer, loadedEnemy);

        // Optional: Musik/Sound-Einstellungen anwenden
        BackgroundMusic.getInstance().setVolume(loadedPlayer.getMusicVolume());
        SoundEffect.setVolume(loadedPlayer.getMusicVolume());
        if (!loadedPlayer.isMusicEnabled()) {
            BackgroundMusic.getInstance().stop();
        }
    }

    @FXML
    private void onReadyClicked() throws IOException {
        readyToSendShips = true;
        if (mlp != null) {
            mlp.sendShips();
            System.out.println("Fertig! Schiffe werden gesendet.");
            if (messageLabel != null) messageLabel.setText("Warte auf Gegner...");
        }
    }

    public void clientPlacedShip(int len) {
        if (len <= 0 || len >= shipsPlaced.length) return;
        shipsPlaced[len]++;
        int remaining = shipsAllowed[len] - shipsPlaced[len];
        if (availableShipCounters[len] != null) {
            availableShipCounters[len].setText("Verfügbar: " + remaining);
        }
        if (remaining <= 0 && shipLengthButtons[len] != null) {
            shipLengthButtons[len].setDisable(true);
        }
    }

    public boolean canClientPlaceShip(int len) {
        if (len <= 0 || len >= shipsAllowed.length) return false;
        return shipsPlaced[len] < shipsAllowed[len];
    }

    // --- Getters und Setters ---

    public int getLength() {
        return length;
    }

    public boolean getDirection() {
        return direction;
    }

    public void setSize(double x1, double y1) {
        x = x1;
        y = y1;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean getPlayerturn() {
        return playerturn;
    }

    public void setPlayerturn() {
        playerturn = !playerturn;
    }

    public void shipPlaced(int len) {
        shipsPlaced[len]++;
    }

    public boolean getHost() {
        return ishost;
    }

    public void setShipCountsFromNetwork(int[] lengths) {
        // Diese Methode könnte jetzt setupClientPlacementUI aufrufen
        Platform.runLater(() -> setupClientPlacementUI(lengths));
    }

    public boolean getReady() {
        return readyToSendShips;
    }

    public String getIP() {
        return ipa;
    }

    public int getPort() {
        return porta;
    }

    public void setShipRules(int[] shipCounts) {
        this.shipsAllowed = shipCounts;
    }
}