package org.example.schiffuntergang;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
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
            Stage sliderStage = new Stage();
            sliderStage.setTitle("Slider-Fenster");
            //sliderStage.setFullScreen(true);

            Slider slider1 = new Slider(0, 30, 10);
            slider1.setShowTickLabels(true);
            slider1.setShowTickMarks(true);

            Slider slider2 = new Slider(0, 30, 10);
            slider2.setShowTickLabels(true);
            slider2.setShowTickMarks(true);

            Label label1 = new Label("Boardbreite: 10");
            Label label2 = new Label("Boardlaenge: 10");

            slider1.valueProperty().addListener((obs, oldVal, newVal) ->
                    label1.setText("Boardbreite: " + String.format("%.0f", newVal.doubleValue()))
            );

            slider2.valueProperty().addListener((obs, oldVal, newVal) ->
                    label2.setText("Boardlaenge: " + String.format("%.0f", newVal.doubleValue()))
            );

            Button start = new Button("Start Game");

            start.setOnAction(e2->{
                clickSound.play();
                x = slider1.getValue();
                y = slider2.getValue();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                    Parent root = loader.load();
                    HelloController controller = loader.getController();
                    controller.setStage(stage);

                    // Optional: controller.buildGamefield(); falls Gamefield erst hier erzeugt wird
                    controller.setSize(x, y);
                    controller.setup();

                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    // stage.setFullScreen(true);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            Button backtostart = new Button("Back to Menu");
            backtostart.setOnAction(e3 -> {
                GameCreationScreen gameScreen = new GameCreationScreen(stage, isSinglePlayer);
                clickSound.play();
                gameScreen.show();
            });

            VBox layout = new VBox(15, label1, slider1, label2, slider2, start, backtostart);
            layout.setStyle("-fx-padding: 20px;");
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout, 300, 250);
            sliderStage.setScene(scene);
            sliderStage.show();



        });

        multiP.setOnAction(e -> {
            clickSound.play();
            HelloController controller = new HelloController();
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
            VBox gamebox = new VBox(15, gamefieldbox, backtogamescreen);
            gamebox.setAlignment(Pos.CENTER);

            Scene gamescene = new Scene(gamebox);
            stage.setScene(gamescene);
            stage.setFullScreen(true);
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


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
