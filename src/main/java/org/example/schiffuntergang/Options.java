package org.example.schiffuntergang;

import com.almasb.fxgl.audio.Sound;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;

import javax.swing.*;

public class Options {
    private final Stage stage;

    public Options(Stage stage) {
        this.stage = stage;

    }

    public void show() {
        //Buttons
        //Save game
        //Musik an aus
        ToggleSwitch musictoggle = new ToggleSwitch("MUTE");
        //Lautstärke
        Slider volume = new Slider(0,100,100);
        volume.setBlockIncrement(1);
        volume.valueProperty().addListener(((observableValue, number, t1) -> {
            SoundEffect.setVolume(t1.doubleValue());
        }));
        //BacktoStart
        Button back = new Button("BACK TO START");
        //Exitgame

        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (Button b: new Button[]{back}) {
                adjustFontSize(b, 30);
            }
        });

        VBox buttonBox = new VBox(15,volume,musictoggle,back);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        back.setOnAction(e ->{
            StartScreen startScreen = new StartScreen(stage);
            clickSound.play();
            startScreen.show();
        });
        for (Button b : new Button[]{back}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        }

        volume.setMaxWidth(200);
        volume.setValue(SoundEffect.getVolume());
        musictoggle.prefHeightProperty().bind(stage.heightProperty().multiply(0.7));
        //musictoggle.prefWidthProperty().bind(stage.widthProperty().multiply(0.1));
        musictoggle.setStyle("-fx-font-size: 40px;");
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
        ParallaxLayer ocean1 = new ParallaxLayer("/images/2.png", 0.3, stage);
        ParallaxLayer ocean2 = new ParallaxLayer("/images/3.png", 0.4, stage);
        ParallaxLayer cloud1 = new ParallaxLayer("/images/9.png", 0.6, stage);
        ParallaxLayer cloud2 = new ParallaxLayer("/images/10.png", 0.8, stage);
        ParallaxLayer cloud3 = new ParallaxLayer("/images/11.png", 1.2, stage);

        for (ImageView x : new ImageView[]{background, beach, ocean, white, rocks, palm}) {
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

        Scene scene = new Scene(parallaxRoot, 400, 300);
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
