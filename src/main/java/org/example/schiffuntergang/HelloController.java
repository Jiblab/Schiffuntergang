package org.example.schiffuntergang;

import javafx.scene.control.Slider;
import org.controlsfx.control.ToggleSwitch;
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
import org.example.schiffuntergang.Multiplayer.KiPlayerController;
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
    private VBox pauseMenu;
    private double previousVolume;
    private KiPlayerController kiController;
    private boolean ki = false;

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
            remainingCell.setText("Build Points: " + remainingCells);
        });
    }

    // --- NEUE LAYOUT-STRUKTUR ---

    private void buildUI(Node playerBoard, Node enemyBoard, Node controlNode) {
        // 1. Header (oben)
        rootPane.setTop(createHeader());

        // 2. Spielfelder (Mitte)

        VBox enemyBox = new VBox(10, enemyBoard);
        enemyBox.setAlignment(Pos.CENTER);


        VBox playerBox = new VBox(10, playerBoard);
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
        notificationBox.setMinHeight(80); // Gibt der Leiste eine feste Höhe

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
                    notificationLabel.setStyle("-fx-text-fill: #029c58; -fx-font-family: 'Press Start 2P'; -fx-font-size: 50px;"); // grün für Treffer
                    break;
                case "sunk":
                    notificationLabel.setStyle("-fx-text-fill: #ea2020; -fx-font-family: 'Press Start 2P'; -fx-font-size: 50px; -fx-font-weight: bold;"); // Rot für Versenkt
                    break;
                case "miss":
                    notificationLabel.setStyle("-fx-text-fill: #ea7b20; -fx-font-family: 'Press Start 2P'; -fx-font-size: 50px;"); // orange für Fehlschuss
                    break;
                default: // "info" oder unbekannt
                    notificationLabel.setStyle("-fx-text-fill: #f3070b; -fx-font-family: 'Press Start 2P'; -fx-font-size: 30px;");
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
    private BorderPane createHeader() {
        // 1. The main container for the header is now a BorderPane.
        BorderPane headerPane = new BorderPane();
        headerPane.setPadding(new Insets(10));
        headerPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // 2. Create the title group (Player vs Enemy) and place it in the CENTER.
        // We can still use an HBox for this little group to get the spacing right.
        Label playerLabel = new Label("Player");
        playerLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");

        Label vsLabel = new Label("vs");
        vsLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: #ffbf00;");

        Label enemyLabel = new Label("Enemy");
        enemyLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");

        HBox titleBox = new HBox(15, playerLabel, vsLabel, enemyLabel);
        titleBox.setAlignment(Pos.CENTER); // Center the labels within their own HBox.

        // Set the titleBox as the center element of the BorderPane. It will be perfectly centered.
        headerPane.setCenter(titleBox);


        // 3. Create the Pause Button and place it on the RIGHT.
        Button pauseButton = new Button("Pause");
        pauseButton.getStyleClass().add("option-button");
        pauseButton.setOnAction(e -> togglePauseMenu());


        // Set the pauseButton as the right element of the BorderPane.
        // We can add padding to the button itself to give it some space from the edge.
        BorderPane.setMargin(pauseButton, new Insets(0, 10, 0, 0)); // Right margin of 10px
        headerPane.setRight(pauseButton);


        return headerPane;
    }

    private VBox createPauseMenu() {
        // --- Buttons and Controls ---
        Button resumeButton = new Button("Resume Game");
        Button saveButton = new Button("Save Game");
        Button exitButton = new Button("Exit to Menu");

        // Volume Controls (reused from your Options class)
        ToggleSwitch musicToggle = new ToggleSwitch("MUTE MUSIC");
        Slider volumeSlider = new Slider(0, 100, SoundEffect.getVolume() > 0 ? SoundEffect.getVolume() : 50);
        volumeSlider.setPrefWidth(250);

        // --- Styling ---
        for (Button btn : new Button[]{resumeButton, saveButton, exitButton}) {
            btn.getStyleClass().add("control-button");
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        // --- Event Handlers ---
        resumeButton.setOnAction(e -> togglePauseMenu()); // The same method hides the menu

        saveButton.setOnAction(e -> {
            stage.setFullScreen(false);
            if (mlp != null) {
                // Multiplayer-Speicherlogik
                SaveDataClass multiplayerSaveData = new SaveDataClass(player, enemy, mlp);
                multiplayerSaveData.saveMultiplayerGame();
            } else {
                // Singleplayer-Speicherlogik (unverändert)
                SaveDataClass singleplayerSaveData = new SaveDataClass(player, enemy);
                FileManager fileManager = new FileManager(true);
                fileManager.save(singleplayerSaveData);
                showNotification("Spiel gespeichert!", "info");
            }

        });

        exitButton.setOnAction(e -> new StartScreen(stage).show());

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            SoundEffect.setVolume(vol);
            BackgroundMusic.getInstance().setVolume(vol / 100.0);
            musicToggle.setSelected(vol == 0);
        });

        musicToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) { // Muting
                previousVolume = volumeSlider.getValue();
                if (previousVolume > 0) volumeSlider.setValue(0);
            } else { // Unmuting
                if (volumeSlider.getValue() == 0) {
                    volumeSlider.setValue(previousVolume > 0 ? previousVolume : 50);
                }
            }
        });

        // --- Layout ---
        VBox menuLayout = new VBox(20, resumeButton, saveButton, musicToggle, volumeSlider, exitButton);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 10; -fx-border-color: #ffbf00; -fx-border-width: 2; -fx-border-radius: 10;");
        menuLayout.setMaxSize(400, 500); // Give the menu a max size

        return menuLayout;
    }

    private void togglePauseMenu() {
        if (pauseMenu == null) {
            pauseMenu = createPauseMenu();
        }

        boolean isPaused = anker.getChildren().contains(pauseMenu);

        if (isPaused) {
            // Unpause the game
            anker.getChildren().remove(pauseMenu);
            player.setDisable(false);
            enemy.setDisable(false);
        } else {
            // Pause the game
            anker.getChildren().add(pauseMenu);
            // Center the menu inside the AnchorPane
            AnchorPane.setTopAnchor(pauseMenu, (anker.getHeight() - pauseMenu.getMaxHeight()) / 2);
            AnchorPane.setLeftAnchor(pauseMenu, (anker.getWidth() - pauseMenu.getMaxWidth()) / 2);

            player.setDisable(true);
            enemy.setDisable(true);
        }
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
        Label shipLengthLabel = new Label("Shiplength");
        shipLengthLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: white;");

        // Buttons für die Schiffslängen (unverändert)
        Button b2 = new Button("Length 2");
        b2.setOnAction(e -> length = 2);
        Button b3 = new Button("Length 3");
        b3.setOnAction(e -> length = 3);
        Button b4 = new Button("Length 4");
        b4.setOnAction(e -> length = 4);
        Button b5 = new Button("Length 5");
        b5.setOnAction(e -> length = 5);

        // Buttons für die Ausrichtung (unverändert)
        Label directionLabel = new Label("Alignment");
        directionLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px; -fx-text-fill: white;");
        Button d_horizontal = new Button("Vertical");
        d_horizontal.setOnAction(e -> direction = false);
        Button d_vertical = new Button("Horizontal");
        d_vertical.setOnAction(e -> direction = true);

        // CSS-Klassen und Breite zuweisen (unverändert)
        for (Button btn : new Button[]{b2, b3, b4, b5, d_horizontal, d_vertical}) {
            btn.getStyleClass().add("option-button");
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        Button fertigButton = new Button("Done Placing");
        fertigButton.getStyleClass().add("control-button");
        fertigButton.setOnAction(e -> {
            try {
                onReadyClicked();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                // Hier wäre ein Alert für den User gut
            }
        });


        for (Button btn : new Button[]{fertigButton}) {
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        controlPanel.getChildren().addAll(
                shipLengthLabel, b2, b3, b4, b5,
                directionLabel, d_horizontal, d_vertical,
                spacer,
                fertigButton
        );

        return controlPanel;
    }

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
        waitingLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");
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
        if (!ki){
            this.player = playerBoard;
            this.enemy = enemyBoard;

            // Ein Platzhalter-Panel für die linke Seite erstellen,
            // während wir auf die Schiffsregeln vom Server warten.
            Label waitingLabel = new Label("Warte auf Schiffsregeln...");
            waitingLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 20px; -fx-text-fill: white;");
            VBox placeholderControls = new VBox(waitingLabel);
            placeholderControls.setPadding(new Insets(20));
            placeholderControls.setAlignment(Pos.CENTER);
            placeholderControls.setMinWidth(200);
            placeholderControls.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

            // Die zentrale UI-Aufbau-Methode mit den neuen Spielfeldern und dem Platzhalter aufrufen
            buildUI(this.player, this.enemy, placeholderControls);
        }
        else{
            this.player = playerBoard;
            this.enemy = enemyBoard;

            // Erstelle ein Platzhalter-Panel oder das finale Control-Panel
            VBox controlPanel = createControlPanel(); // Oder ein "Warte..."-Label

            buildUI(this.player, this.enemy, controlPanel);
            player.setDisable(true);
            enemy.setDisable(true);

            // *** DER ENTSCHEIDENDE PUNKT FÜR DEN CLIENT ***
            // Wenn die Anwendung im KI-Modus läuft, starte hier den KIPlayerController
            if (kiController == null && !ishost) { // Prüfen, ob der Controller noch nicht existiert
                EnemyPlayer ki = new EnemyPlayer(enemyBoard);
                kiController = new KiPlayerController(mlp, ki, playerBoard, enemyBoard, this);
                kiController.start();
            }
        }


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
                Label availableLabel = new Label("Available: " + shipCounts[len]);
                availableLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px; -fx-text-fill: white;");
                this.availableShipCounters[len] = availableLabel;

                Button lengthButton = new Button("Length " + len);
                lengthButton.getStyleClass().add("option-button");
                final int currentLength = len;
                lengthButton.setOnAction(e -> this.length = currentLength);
                lengthButton.setMaxWidth(Double.MAX_VALUE);
                this.shipLengthButtons[len] = lengthButton;

                VBox shipGroup = new VBox(5, new Label("Shiplength" + len), availableLabel, lengthButton);
                shipGroup.setAlignment(Pos.CENTER);
                controlPanel.getChildren().add(shipGroup);
            }
        }

        // Ausrichtungs-Buttons
        Button d_horizontal = new Button("Vertical");
        d_horizontal.setOnAction(e -> direction = false);
        Button d_vertical = new Button("Horizontal");
        d_vertical.setOnAction(e -> direction = true);
        d_horizontal.getStyleClass().add("option-button");
        d_vertical.getStyleClass().add("option-button");
        d_horizontal.setMaxWidth(Double.MAX_VALUE);
        d_vertical.setMaxWidth(Double.MAX_VALUE);

        // Ready-Button
        Button readyButton = new Button("Done Placing");
        readyButton.getStyleClass().add("control-button");
        readyButton.setMaxWidth(Double.MAX_VALUE);
        readyButton.setOnAction(e -> {
            try {
                onReadyClicked();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        Button fertigButton = new Button("Done Placing");
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

    public void setupKivsKi(boolean asHost, String ip, int port) {
        this.ishost = asHost;
        this.ki = true; // Wichtig: Markiere, dass es ein KI-Spiel ist

        // Erstelle die notwendigen Objekte für die Logik
        if (asHost) {
            s = new Server();
            // Spielfelder für den Host sofort erstellen
            player = new Gamefield(false, this, (int) x, (int) y);
            enemy = new Gamefield(true, this, (int) x, (int) y);
            mlp = new MultiplayerLogic(s, false, enemy, player);
        } else {
            ipa = ip; // IP für den Client speichern
            porta = port;
            c = new Client();
            // Spielfelder für den Client sind noch null, das ist korrekt
            mlp = new MultiplayerLogic(c, true, null, null);
        }

        mlp.setController(this);

        // Der zentrale Thread, der die gesamte Initialisierung steuert
        Thread gameSetupThread = new Thread(() -> {
            try {
                // Schritt 1: Führe den blockierenden Start-Handshake aus (size/done)
                // Dieser Aufruf blockiert, bis der Handshake für Host ODER Client fertig ist.
                System.out.println("[" + (asHost ? "Host" : "Client") + "] gameSetupThread: Rufe mlp.start() auf...");
                mlp.start();
                System.out.println("[" + (asHost ? "Host" : "Client") + "] gameSetupThread: mlp.start() ist beendet.");

                // Schritt 2: Starte den KI-Controller, falls er existiert.
                // Für den Host wurde er vorher erstellt.
                // Für den Client wurde er während mlp.start() in setupMultiplayerBoards erstellt.
                if (this.kiController != null) {
                    System.out.println("[" + (asHost ? "Host" : "Client") + "] gameSetupThread: kiController gefunden. Starte ihn jetzt.");
                    this.kiController.start();
                } else {
                    System.err.println("[" + (asHost ? "Host" : "Client") + "] FEHLER: kiController ist nach mlp.start() immer noch null!");
                }

            } catch (IOException e) {
                System.err.println("Fehler im Haupt-Setup-Thread: " + e.getMessage());
                e.printStackTrace();
            }
        });
        gameSetupThread.setDaemon(true);

        // Erstelle den KI-Controller für den Host, BEVOR der Thread gestartet wird.
        if (asHost) {
            System.out.println("[Host] setupKivsKi: Erstelle Host KI-Controller.");
            EnemyPlayer ki = new EnemyPlayer(enemy);
            this.kiController = new KiPlayerController(mlp, ki, player, enemy, this);
            mlp.setKicontroler(this.kiController);

            // UI sofort aufbauen
            buildUI(player, enemy, createControlPanel());
            player.setDisable(true);
            enemy.setDisable(true);
        }
        // Für den Client wird der kiController erst in setupMultiplayerBoards erstellt.
        // Das ist richtig so.

        System.out.println("[" + (asHost ? "Host" : "Client") + "] setupKivsKi: Starte gameSetupThread.");
        gameSetupThread.start();

    }






    private void placeShipsRandomlyOnBoard(Gamefield board) {
        Random rand = new Random();

        double maxCells = board.maxShipsC();
        if (maxCells <= 0) maxCells = 17; // Standardwert für ein 10x10 Feld

        while (board.getUsedCells() < maxCells) {
            int shipLength = 2 + rand.nextInt(4); // Längen 2-5
            boolean vertical = rand.nextBoolean();

            int xMax = board.getLang() - (vertical ? 1 : shipLength);
            int yMax = board.getBreit() - (vertical ? shipLength : 1);

            if (xMax < 0 || yMax < 0) continue;

            int xPos = rand.nextInt(xMax + 1);
            int yPos = rand.nextInt(yMax + 1);

            Ships ship = new Ships(shipLength, shipLength); // Annahme: Konstruktor ist (length, health)
            if (board.placeShip(ship, xPos, yPos, vertical)) {

            }
        }
        System.out.println("Schiffe auf Board platziert. Anzahl Zellen: " + board.getUsedCells());
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
        //Felder erstellen
        this.player = Gamefield.fromData(loadedState.getPlayerBoardData(), this, this.mlp);
        this.enemy = Gamefield.fromData(loadedState.getEnemyBoardData(), this, this.mlp);

        //Logik zuweisen
        if (this.mlp != null) { //Multiplayer
            this.player.setLogic(this.mlp);
            this.enemy.setLogic(this.mlp);
            this.mlp.setPl(this.player);
            this.mlp.setEn(this.enemy);
            this.mlp.setTurn(loadedState.isPlayerTurn()); //Welcher Spieler ist am Zug
        } else { // Singleplayer
            EnemyPlayer ki = new EnemyPlayer(this.player);
            this.enemy.setEnemy(ki);
        }
        this.readyToSendShips = true;

        setup(this.player, this.enemy);

        BackgroundMusic.getInstance().setVolume(loadedState.getMusikVolume());
        if (!loadedState.isMusikAktiv()) {
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

    public boolean getKi(){
        return ki;
    }
}