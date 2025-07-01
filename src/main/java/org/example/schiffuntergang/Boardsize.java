package org.example.schiffuntergang;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.schiffuntergang.sounds.SoundEffect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Boardsize {
    private final Stage stage;
    private final boolean isSinglePlayer;
    private double x;
    private double y;

    public Boardsize(Stage stage, boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.stage = stage;
    }

    static {
        try {
            Font.loadFont(Boardsize.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Pixel-Schriftart konnte nicht geladen werden");
            e.printStackTrace();
        }
    }

    public void show() {
        createAndShowScene(false);
    }

    public void showMulti() {
        createAndShowScene(true);
    }

    private void createAndShowScene(boolean isMultiplayerSetup) {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        //slider
        Slider slider1 = new Slider(0, 30, 10);
        // slider1.setShowTickLabels(true);
        //slider1.setShowTickMarks(true);

        Slider slider2 = new Slider(0, 30, 10);
        // slider2.setShowTickLabels(true);
        //  slider2.setShowTickMarks(true);

        Label label1 = new Label("Boardbreite: 10");
        Label label2 = new Label("Boardhöhe: 10"); // "Boardsize" war doppelt, ich habe es in "Boardhöhe" geändert für Klarheit

        slider1.valueProperty().addListener((obs, oldVal, newVal) ->
                label1.setText("Boardbreite: " + String.format("%.0f", newVal.doubleValue()))
        );
        slider2.valueProperty().addListener((obs, oldVal, newVal) ->
                label2.setText("Boardhöhe: " + String.format("%.0f", newVal.doubleValue()))
        );

        Button start = new Button("Start Game");
        start.getStyleClass().add("control-button");
        start.setOnAction(e -> {
            clickSound.play();
            x = slider1.getValue();
            y = slider2.getValue();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent root = loader.load();
                HelloController controller = loader.getController();
                controller.setStage(stage);
                controller.setSize(x, y);

                if (isMultiplayerSetup) {
                    controller.setupMultiS();
                } else {
                    controller.setup();
                }

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setFullScreen(true);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Button backtostart = new Button("Back to Menu");
        backtostart.getStyleClass().add("control-button");
        backtostart.setOnAction(e -> {

            GameCreationScreen gameScreen = new GameCreationScreen(stage);
            clickSound.play();
            gameScreen.show();
        });

//Layout

        VBox controlsLayout = new VBox(15, label1, slider1, label2, slider2, start, backtostart);
        controlsLayout.setPadding(new Insets(50));
        controlsLayout.setAlignment(Pos.CENTER);
        controlsLayout.maxWidthProperty().bind(stage.widthProperty().multiply(0.5));

        StackPane rootPane = new StackPane();
        rootPane.getStyleClass().add("background");
        rootPane.getChildren().add(controlsLayout);

        Scene scene = new Scene(rootPane);

        VBox.setMargin(start, new Insets(60, 0, 0, 0));

        //css
        scene.getStylesheets().add(getClass().getResource("/background.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/slider.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/button.css").toExternalForm());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                stage.setWidth(800);
                stage.setHeight(600);
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

 
    public void showMulti(){
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        String ipAddress = "Unbekannt";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            ipAddress = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        TextField ipField = new TextField(ipAddress);
        ipField.setEditable(false);
        ipField.setFocusTraversable(false);
        ipField.setStyle("-fx-opacity: 1.0;");

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
                controller.setupMultiS();

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

        VBox layout = new VBox(15,
                new Label("Deine IP-Adresse:"), ipField,
                label1, slider1,
                label2, slider2,
                start, backtostart
        );
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
}

=======
}
