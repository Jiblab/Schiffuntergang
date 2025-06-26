//TODO: DESIGN STARTET JEDES MAL NEU SOBALD MAN AUF OPTIONS DRÜCKT // --> fym?
package org.example.schiffuntergang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;
import org.example.schiffuntergang.ui.ParallaxLayer;

public class Options {
    private final Stage stage;
    private static BackgroundMusic bgMusic;
    private double previousVolume;
    public Options(Stage stage) {
        this.stage = stage;
        bgMusic = BackgroundMusic.getInstance();

    }

    public void show() {
        //Buttons
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
        //Save game
        //Musik an aus
        ToggleSwitch musictoggle = new ToggleSwitch("MUTE MUSIC");
        //Lautstärke
        double initialVolume = SoundEffect.getVolume() > 0 ? SoundEffect.getVolume() : 50;
        Slider volume = new Slider(0, 100, initialVolume);

        volume.setBlockIncrement(1);
        volume.setMaxWidth(200);
        volume.setMajorTickUnit(100.0);
        volume.setMinorTickCount(5);
        volume.setSnapToTicks(true);
        volume.setShowTickMarks(true);
        volume.setShowTickLabels(true);

        SoundEffect.setVolume(volume.getValue());
        bgMusic.setVolume(volume.getValue() / 100.0);
        //bgMusic.stop();
        //bgMusic.play(volume.getValue() / 100.0);

        //BacktoStart
        Button back = new Button("BACK TO START");
        back.setOnAction(e -> {
            StartScreen startScreen = new StartScreen(stage);
            clickSound.play();
            startScreen.show();
        });
        //exit brauchen wir hier doch nicht sorry


        volume.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            SoundEffect.setVolume(vol);
            bgMusic.setVolume(vol / 100.0);

            if (vol > 0) {
                previousVolume = vol;
                musictoggle.setSelected(false);
            } else {
                musictoggle.setSelected(true); // Stumm schalten
            }
        });

        //toggle

        musictoggle.selectedProperty().addListener((obs, before, after) -> {
            if (after) {
                previousVolume = volume.getValue();
                volume.setValue(0);
            } else {

                volume.setValue(previousVolume);
            }
        });
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (Button b: new Button[]{back}) {
                adjustFontSize(b, 30);
            }
        });
        VBox buttonBox = new VBox(15,volume,musictoggle,back);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);

        for (Button b : new Button[]{back}) {
            b.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
            b.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        }

        volume.setMaxWidth(200);
        musictoggle.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
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
