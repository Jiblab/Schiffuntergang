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

    static {
        try {
            Font.loadFont(Boardsize.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("[Boardsize] Pixel-Schriftart konnte nicht geladen werden");
            e.printStackTrace();
        }
    }

    public Boardsize(Stage stage, boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.stage = stage;
    }

    public void show() {
        createAndShowScene(false, false);
    }

    //screen für multiplayer einstellungen
    public void showMulti(boolean ki) {
        createAndShowScene(true, ki);

    }

    private void createAndShowScene(boolean isMultiplayerHost, boolean ki) {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");


        Label widthLabel = new Label("Width: 10");
        Slider widthSlider = new Slider(5, 30, 10); // Mindestgröße 5 für sinnvolles Spiel

        Label heightLabel = new Label("Height: 10");
        Slider heightSlider = new Slider(5, 30, 10);


        widthSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                widthLabel.setText("Width: " + newVal.intValue())
        );
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                heightLabel.setText("Height: " + newVal.intValue())
        );

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("control-button");

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("control-button");


        VBox controlsLayout = new VBox(15, widthLabel, widthSlider, heightLabel, heightSlider, startButton, backButton);
        controlsLayout.setPadding(new Insets(50));
        controlsLayout.setAlignment(Pos.CENTER);
        controlsLayout.maxWidthProperty().bind(stage.widthProperty().multiply(0.5));
        VBox.setMargin(startButton, new Insets(40, 0, 0, 0)); // Abstand nach oben


        if (isMultiplayerHost) {
            Label ipInfoLabel = new Label("Your IP-Address (for your friends):");
            TextField ipField = new TextField();
            ipField.setEditable(false); // Nicht bearbeitbar
            ipField.setFocusTraversable(false); // Kann nicht mit Tab ausgewählt werden
            ipField.setStyle("-fx-opacity: 1.0; -fx-font-family: 'Press Start 2P';");

            try {
                ipField.setText(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                ipField.setText("IP-Address could not be determined");
            }


            controlsLayout.getChildren().add(0, ipInfoLabel);
            controlsLayout.getChildren().add(1, ipField);
        }


        startButton.setOnAction(e -> {
            clickSound.play();
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent root = loader.load();
                HelloController controller = loader.getController();

                controller.setStage(stage);
                controller.setSize(widthSlider.getValue(), heightSlider.getValue());


                if (isMultiplayerHost) {
                    if (ki){
                        controller.setupKivsKi(true, null, 0);
                    }
                    else {
                        controller.setupMultiS();
                    }

                } else {
                    controller.setup();
                }


                Scene gameScene = new Scene(root);

                gameScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage.setIconified(true); // This minimizes the window
                    }
                });

                stage.setScene(gameScene);
                stage.setFullScreen(true);

            } catch (IOException ex) {
                System.err.println("[Boardsize] Fehler beim Laden der Spielansicht (hello-view.fxml).");
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            new GameCreationScreen(stage).show();
        });

        StackPane rootPane = new StackPane(controlsLayout);
        rootPane.getStyleClass().add("background");

        Scene scene = new Scene(rootPane);
        scene.getStylesheets().addAll(
                getClass().getResource("/background.css").toExternalForm(),
                getClass().getResource("/slider.css").toExternalForm(),
                getClass().getResource("/button.css").toExternalForm()
        );

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
            }
        });

        stage.setScene(scene);
        stage.setTitle("Select Boardsize");
        stage.setFullScreen(true);
        stage.show();
    }
}