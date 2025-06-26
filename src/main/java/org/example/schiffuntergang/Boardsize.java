package org.example.schiffuntergang;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.schiffuntergang.sounds.SoundEffect;

import java.io.IOException;

public class Boardsize {
    private final Stage stage;
    private final boolean isSinglePlayer;
    private double x;
    private double y;

    public Boardsize(Stage stage, boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.stage = stage;
    }

    public void show() {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        Slider slider1 = new Slider(0, 30, 10);
        slider1.setShowTickLabels(true);
        slider1.setShowTickMarks(true);

        Slider slider2 = new Slider(0, 30, 10);
        slider2.setShowTickLabels(true);
        slider2.setShowTickMarks(true);

        Label label1 = new Label("Boardbreite: 10");
        Label label2 = new Label("Boardsize: 10");

        slider1.valueProperty().addListener((obs, oldVal, newVal) ->
                label1.setText("Boardbreite: " + String.format("%.0f", newVal.doubleValue()))
        );

        slider2.valueProperty().addListener((obs, oldVal, newVal) ->
                label2.setText("Boardsize: " + String.format("%.0f", newVal.doubleValue()))
        );

        Button start = new Button("Start Game");

        start.setOnAction(e2 -> {
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
                stage.setFullScreen(true);

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
        layout.setStyle("-fx-padding: 30px;");
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 250);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(400);
                stage.setHeight(300);
            }
        });
        stage.setScene(scene);
        stage.setTitle("Boardsize");
        stage.setFullScreen(true);
        stage.show();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}

