package org.example.schiffuntergang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.schiffuntergang.filemanagement.FileManager;
import org.example.schiffuntergang.filemanagement.GameState;
import org.example.schiffuntergang.sounds.SoundEffect;
import org.example.schiffuntergang.ui.ParallaxLayer;

import javafx.stage.FileChooser;
import org.example.schiffuntergang.components.Gamefield;
import javafx.util.Pair;

import java.io.IOException;


public class StartScreen {
    private final Stage stage;
    private final Label modeLabel;
    private final boolean isSinglePlayer = true;

    static {
        try {
            Font.loadFont(Options.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("[StartScreen] Pixel-Schriftart konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    public StartScreen(Stage stage) {
        this.stage = stage;
        this.modeLabel = new Label();

    }

    public void show() {
        Button start = new Button("START GAME");
        Button load = new Button("LOAD GAME");
        Button options = new Button("OPTIONS");
        Button exit = new Button("EXIT");

        stage.setFullScreen(true);
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (Button b : new Button[]{start, load, options, exit}) {
                adjustFontSize(b, 40);
            }
        });

        start.setOnAction(e -> {
            GameCreationScreen gameScreen = new GameCreationScreen(stage);
            clickSound.play();
            gameScreen.show();
        });

        load.setOnAction(e -> {
            clickSound.play();
            stage.setFullScreen(false);

            FileManager fileManager = new FileManager(true);
            try {
                GameState loadedState = fileManager.load();
                if (loadedState == null) {
                    stage.setFullScreen(true);
                    return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent root = loader.load();
                HelloController controller = loader.getController();
                controller.setStage(stage);


                if (loadedState.isMultiplayer()) {
                    System.out.println("[StartScreen] Multiplayer-Spielstand wird geladen. Starte als Host...");
                    controller.setupMultiS();
                    controller.loadGameFromSave(loadedState);
                } else {
                    System.out.println("[StartScreen] Singleplayer-Spielstand wird geladen...");
                    controller.loadGameFromSave(loadedState); // Lädt die Daten und erstellt die Spielfelder
                }

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setFullScreen(true);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        options.setOnAction(e -> {
            Options options1 = new Options(stage);
            clickSound.play();
            options1.show();
        });
        exit.setOnAction(e -> {
            clickSound.play();
            System.exit(0);
        });

        VBox buttonBox = new VBox(15, modeLabel, start, load, options, exit);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        for (Button b : new Button[]{start, load, options, exit}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        }

        StackPane parallaxRoot = new StackPane();
        ImageView background = createFullscreenImageView("/images/0.png");
        ImageView ocean = createFullscreenImageView("/images/1.png");
        ImageView beach = createFullscreenImageView("/images/4.png");
        ImageView white = createFullscreenImageView("/images/5.png");
        ImageView rocks = createFullscreenImageView("/images/7.png");
        ImageView palm = createFullscreenImageView("/images/12.png");

        ParallaxLayer ocean1 = new ParallaxLayer("/images/2.png", 0.3, stage);
        ParallaxLayer ocean2 = new ParallaxLayer("/images/3.png", 0.4, stage);
        ParallaxLayer cloud1 = new ParallaxLayer("/images/9.png", 0.6, stage);
        ParallaxLayer cloud2 = new ParallaxLayer("/images/10.png", 0.8, stage);
        ParallaxLayer cloud3 = new ParallaxLayer("/images/11.png", 1.2, stage);

        for (ImageView x : new ImageView[]{background, ocean, beach, white, rocks, palm}) {
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
        stage.setFullScreen(true);
        stage.setTitle("Startbildschirm");
        stage.show();

        // Parallax
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
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
}
