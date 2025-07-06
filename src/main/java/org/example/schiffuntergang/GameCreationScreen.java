package org.example.schiffuntergang;

import javafx.geometry.Insets;
import org.example.schiffuntergang.ui.ParallaxLayer;
import org.example.schiffuntergang.sounds.SoundEffect;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GameCreationScreen {
    private final Stage stage;
    private double x;
    private double y;
    private final SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
    private Timeline parallaxTimeline;


    static {
        try {
            Font.loadFont(Options.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("[GameCreationScreen] Pixel-Schriftart konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    public GameCreationScreen(Stage stage) {
        this.stage = stage;
    }
    public void show() {
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
    private void showMultiplayerMenuScene() {
        Button createGame = new Button("Host Game");
        Button findGame = new Button("Join Game");
        Button backButton = new Button("Back");
        Button createAI = new Button("Host Game Computer");
        Button joinAi = new Button("Join Game Computer");

        for (Button btn : List.of(createGame, findGame, backButton, createAI, joinAi)) {
            btn.getStyleClass().add("option-button");
            adjustFontSize(btn, 60);
        }

        joinAi.setOnAction(e->{
            clickSound.play();
            showJoinGameScene(true);
        });

        createAI.setOnAction(e->{
            clickSound.play();
            Boardsize boardsize = new Boardsize(stage, false);
            boardsize.showMulti(true);
        });

        createGame.setOnAction(e -> {
            clickSound.play();
            Boardsize boardsize = new Boardsize(stage, false);
            boardsize.showMulti(false);
        });

        findGame.setOnAction(e -> {
            clickSound.play();
            showJoinGameScene(false);
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            this.show();
        });

        VBox layout = new VBox(20, createGame, findGame, createAI, joinAi, backButton);
        layout.setAlignment(Pos.CENTER);

        layout.getStyleClass().add("background");


        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/background.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/button.css").toExternalForm());
        setupEscapeKey(scene);

        stage.setScene(scene);
        stage.setTitle("Multiplayer");
        stage.setFullScreen(true);
    }
    private void showJoinGameScene(boolean ki) {

        Label ipLabel = new Label("Server-IP from Host:");
        TextField ipField = new TextField();
        ipField.setPromptText("e.g., 192.168.0.10");
        ipField.setMaxWidth(200);
        Label portInfoLabel = new Label("Enter Port:");
        TextField portField = new TextField("5000");
        Button connectButton = new Button("Connect");
        Button backButton = new Button("Back");
        Label statusLabel = new Label();

        for (Label label : List.of(ipLabel, portInfoLabel)) {
            label.setStyle("-fx-font-family: 'Press Start 2P'; -fx-text-fill: white;");
        }
        for (TextField field : List.of(ipField, portField)) {
            field.setStyle("-fx-font-family: 'Press Start 2P';");
            field.setMaxWidth(300);
        }
        statusLabel.setStyle("-fx-text-fill: #ff6347; -fx-font-family: 'Press Start 2P';");

        for (Button btn : List.of(connectButton, backButton)) {
            btn.getStyleClass().add("control-button");
        }
        connectButton.setOnAction(e -> {
            clickSound.play();
            String ip = ipField.getText();
            if (ip == null || ip.trim().isEmpty()) {
                statusLabel.setText("Please enter a valid IP address!");
                return;
            }
            int port = 5000;
            statusLabel.setText("Connecting to " + ip + "...");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent gameRoot = loader.load();

                HelloController gameController = loader.getController();

                gameController.setStage(this.stage);
                gameController.setSize(10, 10);

                if (ki){
                    gameController.setupKivsKi(false, ip, port);
                }else{
                    gameController.setupMultiC(ip, port);
                }

                Scene gameScene = new Scene(gameRoot);

                gameScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage.setIconified(true);
                    }
                });
                this.stage.setScene(gameScene);
                this.stage.setFullScreen(true);

            } catch (IOException ex) {
                statusLabel.setText("Error: Connection failed or game view could not be loaded.");
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            showMultiplayerMenuScene();
        });

        VBox layout = new VBox(15, ipLabel, ipField, connectButton, backButton, statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.maxWidthProperty().bind(stage.widthProperty().multiply(0.5));

        VBox.setMargin(connectButton, new Insets(40, 0, 0, 0));

        StackPane rootPane = new StackPane(layout);
        rootPane.getStyleClass().add("background");

        Scene scene = new Scene(rootPane);
        scene.getStylesheets().addAll(
                getClass().getResource("/background.css").toExternalForm(),
                getClass().getResource("/button.css").toExternalForm(),
                getClass().getResource("/slider.css").toExternalForm()
        );
        setupEscapeKey(scene);

        stage.setScene(scene);
        stage.setTitle("Join Game");
        stage.setFullScreen(true);
    }
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
        Image image = new Image(getClass().getResourceAsStream(path));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(stage.widthProperty());
        imageView.fitHeightProperty().bind(stage.heightProperty());
        return imageView;

    }
    private void showMultiplayerWindowAndCloseCurrent() {
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

        findGame.setOnAction(e -> {

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
                int port = 5000;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                    Parent root = loader.load();
                    HelloController controller1 = loader.getController();
                    controller1.setStage(connectStage);

                    controller1.setSize(x, y);
                    controller1.setupMultiC(ip, port);

                    Scene scene = new Scene(root);
                    connectStage.setScene(scene);
                    connectStage.setFullScreen(true);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            });

            VBox vbox2 = new VBox(15, ipLabel, ipField, connectButton, statusLabel);
            vbox2.setAlignment(Pos.CENTER);
            vbox2.setPrefWidth(350);
            vbox2.setPrefHeight(200);

            Scene scene = new Scene(vbox2);
            connectStage.setScene(scene);
            connectStage.setFullScreen(true);
            connectStage.show();

        });

        createGame.setOnAction(e -> {

            clickSound.play();
            Boardsize boardsize = new Boardsize(stage, true);
            boardsize.showMulti(false);
            multiplayerStage.close();
        });

        close.setOnAction(e -> {
            StartScreen startScreen = new StartScreen(stage);

            clickSound.play();
            startScreen.show();
        });

        Scene scene = new Scene(vbox);
        multiplayerStage.setScene(scene);

        multiplayerStage.setFullScreen(true);

        multiplayerStage.show();

        stage.close();
    }
}