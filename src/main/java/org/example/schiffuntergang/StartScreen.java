package org.example.schiffuntergang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.schiffuntergang.sounds.SoundEffect;
import org.example.schiffuntergang.ui.ParallaxLayer;

public class StartScreen {
    private final Stage stage;
    private final Label modeLabel;
    private final boolean isSinglePlayer = true;

    public StartScreen(Stage stage) {
        this.stage = stage;
        this.modeLabel = new Label();
    }

    public void show() {
        Button start = new Button("START GAME");
        Button load = new Button("LOAD GAME");
        Button options = new Button("OPTIONS");
        Button exit = new Button("EXIT");

        // Initial Vollbild
        stage.setFullScreen(true);
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (Button b : new Button[]{start, load, options, exit}) {
                adjustFontSize(b, 30);
            }
        });

        // Button-Verhalten
        start.setOnAction(e -> {
            GameCreationScreen gameScreen = new GameCreationScreen(stage, isSinglePlayer);
            clickSound.play();
            gameScreen.show();
        });
        load.setOnAction(e -> {
            clickSound.play();
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

        // Layout für Buttons
        VBox buttonBox = new VBox(15, modeLabel, start, load, options, exit);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        // Dynamische Buttongröße
        for (Button b : new Button[]{start, load, options, exit}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        }

        // Hintergrund + Parallax-Ebenen
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

        // ESC → Fenster verkleinern
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(400);
                stage.setHeight(300);
            }
        });

        // Szene setzen
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("Startbildschirm");
        stage.show();

        // Parallax-Aktualisierung
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
        button.setStyle("-fx-font-size:" + size + "px");
        // BackgroundImage image = new BackgroundImage(new Image(getClass().getResourceAsStream("/images/button_texture.png")),BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,new BackgroundSize(button.getWidth(), button.getHeight(), false, false, false, true));
        //button.setBackground(new Background(image));
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
