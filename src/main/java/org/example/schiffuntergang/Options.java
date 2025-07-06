package org.example.schiffuntergang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;
import org.example.schiffuntergang.ui.ParallaxLayer;

import java.util.ArrayList;
import java.util.List;

public class Options {
    private final Stage stage;
    private static BackgroundMusic bgMusic;
    private double previousVolume;
    private Timeline parallaxTimeline;

    static {
        try {
            Font.loadFont(Options.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("[Options] Pixel-Schriftart konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    public Options(Stage stage) {
        this.stage = stage;
        bgMusic = BackgroundMusic.getInstance();
    }

    public void show() {
        VBox controlsLayout = createControls();

        StackPane parallaxRoot = new StackPane();
        parallaxRoot.setAlignment(Pos.CENTER);
        List<ParallaxLayer> animatedLayers = createAndAddBackgroundLayers(parallaxRoot);
        parallaxRoot.getChildren().add(controlsLayout);

        Scene scene = new Scene(parallaxRoot, 400, 300);

        scene.getStylesheets().add(getClass().getResource("/slider.css").toExternalForm());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(800);
                stage.setHeight(600);
            }
        });

        stage.setScene(scene);
        stage.setTitle("Options");
        stage.setFullScreen(true);
        stage.show();
        startParallaxAnimation(animatedLayers);
    }

    private VBox createControls() {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
        ToggleSwitch musictoggle = new ToggleSwitch("MUTE MUSIC");
        double initialVolume = SoundEffect.getVolume() > 0 ? SoundEffect.getVolume() : 50;
        Slider volume = new Slider(0, 100, initialVolume);
        Button back = new Button("BACK TO START");

        SoundEffect.setVolume(volume.getValue());
        bgMusic.setVolume(volume.getValue() / 100.0);
        back.setOnAction(e -> {
            clickSound.play();
            if (parallaxTimeline != null) parallaxTimeline.stop();
            StartScreen startScreen = new StartScreen(stage);
            startScreen.show();
        });
        volume.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            SoundEffect.setVolume(vol);
            bgMusic.setVolume(vol / 100.0);
            musictoggle.setSelected(vol == 0);
        });
        musictoggle.selectedProperty().addListener((obs, before, after) -> {
            if (after) {
                previousVolume = volume.getValue();
                if (previousVolume > 0) volume.setValue(0);
            } else {
                if (volume.getValue() == 0) volume.setValue(previousVolume > 0 ? previousVolume : 50);
            }
        });

        VBox layout = new VBox(25, musictoggle, volume, back);
        layout.setAlignment(Pos.CENTER);
        layout.maxWidthProperty().bind(stage.widthProperty().multiply(0.6));
        layout.setPadding(new Insets(30));

        adjustFontSize(back, 40);
        back.prefWidthProperty().bind(stage.widthProperty().multiply(0.8));
        back.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));

        return layout;
    }

    private List<ParallaxLayer> createAndAddBackgroundLayers(StackPane root) {
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

        List<ParallaxLayer> animatedLayers = List.of(ocean1, ocean2, cloud1, cloud2, cloud3);

        root.getChildren().addAll(
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
                rocks
        );

        return new ArrayList<>(animatedLayers);
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