package org.example.schiffuntergang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.schiffuntergang.ui.ParallaxLayer;
import org.example.schiffuntergang.sounds.SoundEffect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameCreationScreen {
    private final Stage stage;
    private double x;
    private double y;

    private final SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
    static {
        try {
            Font.loadFont(Options.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Pixel-Schriftart konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    public GameCreationScreen(Stage stage) {
        this.stage = stage;
    }

    private Timeline parallaxTimeline;

    public void show() {
        // Aufteilung in Helfermethoden für bessere Lesbarkeit
        List<ParallaxLayer> parallaxLayers = new ArrayList<>();
        StackPane parallaxRoot = createParallaxBackground(parallaxLayers);

        VBox buttonBox = createMenuButtons();
        parallaxRoot.getChildren().add(buttonBox);

        Scene scene = new Scene(parallaxRoot);
        setupEscapeKey(scene);

        stage.setScene(scene);
        stage.setTitle("Game Mode Selection");
        stage.setFullScreen(true);
        stage.show();

        startParallaxAnimation(parallaxLayers);
    }

    private VBox createMenuButtons() {
        Button singleP = new Button("SINGLE PLAYER");
        Button multiP = new Button("MULTIPLAYER");
        Button back = new Button("BACK TO START");

        singleP.setOnAction(e -> {
            clickSound.play();
            if (parallaxTimeline != null) parallaxTimeline.stop();
            Boardsize boardsize = new Boardsize(stage, true);
            boardsize.show();
        });

        multiP.setOnAction(e -> {
            clickSound.play();
            if (parallaxTimeline != null) parallaxTimeline.stop();
            showMultiplayerMenuScene();
        });

        back.setOnAction(e -> {
            clickSound.play();
            if (parallaxTimeline != null) parallaxTimeline.stop();
            StartScreen startScreen = new StartScreen(stage);
            startScreen.show();
        });

        for (Button b : new Button[]{singleP, multiP, back}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.4));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
            stage.widthProperty().addListener((obs, oldVal, newVal) -> adjustFontSize(b, 40));
        }

        VBox buttonBox = new VBox(15, singleP, multiP, back);
        buttonBox.setAlignment(Pos.CENTER);
        return buttonBox;
    }

    // --- NEUE METHODEN FÜR MULTIPLAYER-NAVIGATION ---

    private void showMultiplayerMenuScene() {
        Button createGame = new Button("Host Game");
        Button findGame = new Button("Join Game");
        Button backButton = new Button("Back");

        createGame.setOnAction(e -> {
            clickSound.play();
            // Leitet direkt zum Boardsize-Screen im Multiplayer-Modus
            Boardsize boardsize = new Boardsize(stage, false);
            boardsize.showMulti();
        });

        findGame.setOnAction(e -> {
            clickSound.play();
            showJoinGameScene(); // Wechselt zur nächsten Szene
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            this.show(); // Kehrt zum vorherigen Bildschirm zurück (GameCreationScreen)
        });

        VBox layout = new VBox(20, createGame, findGame, backButton);
        layout.setAlignment(Pos.CENTER);
        // Hintergrundklasse für einheitliches Aussehen hinzufügen
        layout.getStyleClass().add("background");

        Scene scene = new Scene(layout);
        // Stylesheet für den Hintergrund laden
        scene.getStylesheets().add(getClass().getResource("/background.css").toExternalForm());
        setupEscapeKey(scene);

        stage.setScene(scene);
        stage.setTitle("Multiplayer");
        stage.setFullScreen(true);
    }

    private void showJoinGameScene() {
        Stage connectStage = new Stage();
        connectStage.setTitle("Mit Server verbinden");
        Label ipLabel = new Label("Server-IP:");
        TextField ipField = new TextField();
        ipField.setPromptText("e.g., 192.168.0.10");
        ipField.setMaxWidth(200);

        Button connectButton = new Button("Connect");
        Button backButton = new Button("Back");
        Label statusLabel = new Label();

        connectButton.setOnAction(e -> {
            clickSound.play();
            String ip = ipField.getText();
            if (ip == null || ip.trim().isEmpty()) {
                statusLabel.setText("Please enter a valid IP address!");
                return; // Beendet die Aktion, wenn keine IP eingegeben wurde
            }
            int port = 5000;
            statusLabel.setText("Connecting to " + ip + "...");

            try {
                // 1. Lade die FXML-Datei
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent gameRoot = loader.load();

                // 2. Hole den Controller, NACHDEM die FXML geladen wurde
                HelloController gameController = loader.getController();

                // 3. Konfiguriere den Controller mit den notwendigen Informationen
                gameController.setStage(this.stage); // Übergib die HAUPT-Stage
                gameController.setSize(10, 10);      // Übergib Dummy-Werte für die Größe
                gameController.setupMultiC(ip, port);// Starte das Client-Setup

                // 4. Erstelle eine neue Szene mit der geladenen Spielansicht
                Scene gameScene = new Scene(gameRoot);

                // 5. SETZE die neue Szene auf der HAUPT-Stage. Das tauscht den Inhalt aus.
                this.stage.setScene(gameScene);
                this.stage.setFullScreen(true); // Sicherstellen, dass das Fenster im Vollbild bleibt

            } catch (IOException ex) {
                statusLabel.setText("Error: Connection failed or game view could not be loaded.");
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            showMultiplayerMenuScene(); // Zurück zum Multiplayer-Menü
        });

        VBox layout = new VBox(15, ipLabel, ipField, connectButton, backButton, statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("background");

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/background.css").toExternalForm());
        setupEscapeKey(scene);

        stage.setScene(scene);
        stage.setTitle("Join Game");
        stage.setFullScreen(true);
    }

    // --- Helfermethoden (unverändert, aber jetzt besser strukturiert) ---

    private void startParallaxAnimation(List<ParallaxLayer> layers) {
        this.parallaxTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            for (ParallaxLayer layer : layers) {
                layer.update();
            }
        }));
        this.parallaxTimeline.setCycleCount(Timeline.INDEFINITE);
        this.parallaxTimeline.play();
    }

    private StackPane createParallaxBackground(List<ParallaxLayer> parallaxLayers) {
        StackPane parallaxRoot = new StackPane(
                createFullscreenImageView("/images/0.png"),
                createFullscreenImageView("/images/1.png")
        );

        ParallaxLayer ocean1 = new ParallaxLayer("/images/2.png", 0.3, stage);
        ParallaxLayer ocean2 = new ParallaxLayer("/images/3.png", 0.4, stage);
        ParallaxLayer cloud1 = new ParallaxLayer("/images/9.png", 0.6, stage);
        ParallaxLayer cloud2 = new ParallaxLayer("/images/10.png", 0.8, stage);
        ParallaxLayer cloud3 = new ParallaxLayer("/images/11.png", 1.2, stage);
        parallaxLayers.addAll(List.of(ocean1, ocean2, cloud1, cloud2, cloud3));

        parallaxRoot.getChildren().addAll(
                ocean1.getNode(), ocean2.getNode(),
                createFullscreenImageView("/images/4.png"),
                createFullscreenImageView("/images/5.png"),
                cloud1.getNode(), cloud2.getNode(), cloud3.getNode(),
                createFullscreenImageView("/images/12.png"),
                createFullscreenImageView("/images/7.png")
        );
        parallaxRoot.setAlignment(Pos.CENTER);
        return parallaxRoot;
    }

    private void setupEscapeKey(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(800);
                stage.setHeight(600);
            }
        });
    }

    private void adjustFontSize(Button button, double baseWidth) {
        double size = stage.getWidth() / baseWidth;
        button.setStyle("-fx-font-size:" + size + "px; -fx-font-family: 'Press Start 2P';" +
                "-fx-background-color: #8b6248; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #402d21; " +
                "-fx-border-width: 3px; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5;");
    }

    private ImageView createFullscreenImageView(String path) {
        // Hinweis: Es ist besser, hier eine Fehlerbehandlung einzubauen, falls das Bild nicht gefunden wird.
        Image image = new Image(getClass().getResourceAsStream(path));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(stage.widthProperty());
        imageView.fitHeightProperty().bind(stage.heightProperty());
        return imageView;

    }

    private void showMultiplayerWindowAndCloseCurrent() {
        // Neues Fenster (Stage) erstellen
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
        Stage multiplayerStage = new Stage();
        multiplayerStage.setTitle("Multiplayer");
        HelloController controller = new HelloController();
        Button findGame = new Button("Game finden");
        Button createGame = new Button("Spiel erstellen");
        Button close = new Button("Schließen");

        VBox vbox = new VBox(20, findGame, createGame, close);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(300);
        vbox.setPrefHeight(200);

        // Button-Logik
        findGame.setOnAction(e -> {
            //System.out.println("Game finden geklickt!");
            Stage connectStage = new Stage();
            connectStage.setTitle("Mit Server verbinden");

            Label ipLabel = new Label("Server-IP:");
            TextField ipField = new TextField();
            ipField.setPromptText("z.B. 192.168.0.10");
            Button connectButton = new Button("Verbinden");
            Label statusLabel = new Label();

            connectButton.setOnAction(ev -> {
                clickSound.play();
                String ip = ipField.getText();
                int port = 5000; // Passe ggf. den Port an

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                    Parent root = loader.load();
                    HelloController controller1 = loader.getController();
                    controller1.setStage(connectStage);

                    // Optional: controller.buildGamefield(); falls Gamefield erst hier erzeugt wird
                    controller1.setSize(x, y);
                    controller1.setupMultiC(ip, port);

                    Scene scene = new Scene(root);
                    connectStage.setScene(scene);
                    connectStage.setFullScreen(true);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                    //connectStage.close();

            });

            VBox vbox2 = new VBox(15, ipLabel, ipField, connectButton, statusLabel);
            vbox2.setAlignment(Pos.CENTER);
            vbox2.setPrefWidth(350);
            vbox2.setPrefHeight(200);

            Scene scene = new Scene(vbox2);
            connectStage.setScene(scene);
            connectStage.setFullScreen(true); // Optional: im Vollbild öffnen
            connectStage.show();

        });

        createGame.setOnAction(e -> {
            //System.out.println("Spiel erstellen geklickt!");
            clickSound.play();
            Boardsize boardsize = new Boardsize(stage, true);
            boardsize.showMulti();
            multiplayerStage.close();
        });

        close.setOnAction(e -> {
            StartScreen startScreen = new StartScreen(stage);

            clickSound.play();
            startScreen.show();
        });

        Scene scene = new Scene(vbox);
        multiplayerStage.setScene(scene);

        // Direkt im Vollbild öffnen
        multiplayerStage.setFullScreen(true);

        // Multiplayer-Fenster öffnen
        multiplayerStage.show();

        // Altes Fenster schließen
        stage.close();

    }
}