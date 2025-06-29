
package org.example.schiffuntergang;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.sounds.SoundEffect;
import org.example.schiffuntergang.ui.ParallaxLayer;

import java.io.IOException;

public class GameCreationScreen {
    private final Stage stage;
    private final boolean isSinglePlayer;
    private double x;
    private double y;


    public GameCreationScreen(Stage stage, boolean isSinglePlayer) {
        this.stage = stage;
        this.isSinglePlayer = isSinglePlayer;

    }

    public void show() {
        Button back = new Button("BACK TO START");
        Button singleP = new Button("SINGLE PLAYER");
        Button multiP = new Button("MULTIPLAYER");

        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
        //Sound Effect by <a href="https://pixabay.com/users/driken5482-45721595/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=236677">Driken Stan</a> from <a href="https://pixabay.com/sound-effects//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=236677">Pixabay</a>

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (Button b: new Button[]{singleP,multiP,back}) {
                adjustFontSize(b, 30);
            }
        });

        VBox buttonBox = new VBox(15,singleP,multiP,back);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        back.setOnAction(e ->{
            StartScreen startScreen = new StartScreen(stage);
            clickSound.play();
            startScreen.show();
        });

        singleP.setOnAction(e -> {
            clickSound.play();
            Boardsize boardsize = new Boardsize(stage, true);
            boardsize.show();
        });

        multiP.setOnAction(e -> {
            clickSound.play();
            showMultiplayerWindowAndCloseCurrent();
            /*HelloController controller = new HelloController();
            Gamefield playerfield = new Gamefield(false, controller, (int) x, (int) y);
            Gamefield enemyfield = new Gamefield(true, controller, (int) x, (int) y);

            HBox gamefieldbox = new HBox(50, playerfield, enemyfield);
            gamefieldbox.setAlignment(Pos.CENTER);

            Button backtogamescreen = new Button("Back to Menu");
            backtogamescreen.setOnAction(ev -> {
                GameCreationScreen gameScreen = new GameCreationScreen(stage, true);
                clickSound.play();
                gameScreen.show();
            });
            Button searchgame = new Button("Search Game");
            Button hostgame = new Button("Host Game");

            VBox gamebox = new VBox(15, gamefieldbox, backtogamescreen, searchgame, hostgame);
            gamebox.setAlignment(Pos.CENTER);

            Scene gamescene = new Scene(gamebox);
            stage.setScene(gamescene);
            stage.setFullScreen(true);*/

        });

        for (Button b : new Button[]{singleP,multiP,back}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        }

        StackPane parallaxRoot = new StackPane();
        parallaxRoot.setAlignment(Pos.CENTER);
        //HINTERGRUND:
        //Fest
        ImageView background = createFullscreenImageView("/images/0.png");
        ImageView ocean = createFullscreenImageView("/images/1.png");
        ImageView beach = createFullscreenImageView("/images/4.png");
        ImageView white = createFullscreenImageView("/images/5.png");
        ImageView rocks = createFullscreenImageView("/images/7.png");
        ImageView palm = createFullscreenImageView("/images/12.png");

        //bewegend
        ParallaxLayer ocean1 = new ParallaxLayer("/images/2.png",0.3, stage);
        ParallaxLayer ocean2 = new ParallaxLayer("/images/3.png", 0.4, stage);
        ParallaxLayer cloud1 = new ParallaxLayer("/images/9.png", 0.6, stage);
        ParallaxLayer cloud2 = new ParallaxLayer("/images/10.png",0.8, stage);
        ParallaxLayer cloud3 = new ParallaxLayer("/images/11.png", 1.2, stage);

        for (ImageView x : new ImageView[]{background, beach, ocean, white,rocks,palm}){
            x.fitWidthProperty().bind(stage.widthProperty());
            x.setPreserveRatio(false);
        }

        parallaxRoot.getChildren().addAll(
                background,
                ocean,
                ocean1.getNode(),
                ocean2.getNode(),
                beach,
                white,
                cloud1.getNode(),
                cloud2.getNode(),
                cloud3.getNode(),
                palm,
                rocks,
                buttonBox
                //label
        );

        Scene scene = new Scene(parallaxRoot);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(400);
                stage.setHeight(300);
            }
        });
        stage.setScene(scene);
        stage.setTitle("GameScreen");
        stage.setFullScreen(true);
        stage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e ->{
            ocean1.update();
            ocean2.update();
            cloud1.update();
            cloud2.update();
            cloud3.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    void adjustFontSize(Button button, double baseWidth) {
        double size = stage.getWidth() / baseWidth;
        button.setStyle("-fx-font-size:" + size + "px");
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
        // Neues Fenster (Stage) erstellen
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
                String ip = ipField.getText();
                int port = 5000; // Passe ggf. den Port an

                    controller.setupMultiC(ip, port); // Deine Methode zur Verbindung
                    connectStage.close();

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
            Boardsize boardsize = new Boardsize(stage, true);
            boardsize.showMulti();
        });

        close.setOnAction(e -> multiplayerStage.close());

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
