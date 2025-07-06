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

    // Schriftart wird nur einmal beim Laden der Klasse initialisiert
    static {
        try {
            Font.loadFont(Boardsize.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Pixel-Schriftart konnte nicht geladen werden");
            e.printStackTrace();
        }
    }

    public Boardsize(Stage stage, boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.stage = stage;
    }

    /**
     * Zeigt den Einstellungsbildschirm für ein Einzelspieler-Spiel an.
     */
    public void show() {
        createAndShowScene(false, false);
    }

    /**
     * Zeigt den Einstellungsbildschirm für das Hosten eines Multiplayer-Spiels an.
     */
    public void showMulti(boolean ki) {
        if(ki){
            createAndShowScene(true, true);
        }
        else {
            createAndShowScene(true, false);
        }

    }

    /**
     * Die zentrale Methode, die die Benutzeroberfläche für die Spielfeldgröße erstellt und anzeigt.
     *
     * @param isMultiplayerHost True, wenn ein Multiplayer-Spiel gehostet wird (zeigt die IP an), sonst false.
     */
    private void createAndShowScene(boolean isMultiplayerHost, boolean ki) {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        // --- UI-Komponenten erstellen ---
        Label widthLabel = new Label("Breite: 10");
        Slider widthSlider = new Slider(5, 30, 10); // Mindestgröße 5 für sinnvolles Spiel

        Label heightLabel = new Label("Höhe: 10");
        Slider heightSlider = new Slider(5, 30, 10);

        // Slider mit Labels verbinden
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                widthLabel.setText("Breite: " + newVal.intValue())
        );
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                heightLabel.setText("Höhe: " + newVal.intValue())
        );

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("control-button");

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("control-button");

        // --- Layout erstellen ---
        VBox controlsLayout = new VBox(15, widthLabel, widthSlider, heightLabel, heightSlider, startButton, backButton);
        controlsLayout.setPadding(new Insets(50));
        controlsLayout.setAlignment(Pos.CENTER);
        controlsLayout.maxWidthProperty().bind(stage.widthProperty().multiply(0.5));
        VBox.setMargin(startButton, new Insets(40, 0, 0, 0)); // Abstand nach oben

        // --- Bedingte Logik für Multiplayer-Host ---
        if (isMultiplayerHost) {
            Label ipInfoLabel = new Label("Deine IP-Adresse (für deine Freunde):");
            TextField ipField = new TextField();
            ipField.setEditable(false); // Nicht bearbeitbar
            ipField.setFocusTraversable(false); // Kann nicht mit Tab ausgewählt werden
            ipField.setStyle("-fx-opacity: 1.0; -fx-font-family: 'Press Start 2P';");

            try {
                ipField.setText(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                ipField.setText("IP-Adresse konnte nicht ermittelt werden");
            }

            // Füge die IP-Anzeige ganz oben im Layout ein
            controlsLayout.getChildren().add(0, ipInfoLabel);
            controlsLayout.getChildren().add(1, ipField);
        }

        // --- Button-Aktionen definieren ---
        startButton.setOnAction(e -> {
            clickSound.play();
            try {
                // Die "Goldene Regel" des FXML-Ladens
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent root = loader.load();
                HelloController controller = loader.getController();

                // Den Controller mit den gewählten Werten konfigurieren
                controller.setStage(stage);
                controller.setSize(widthSlider.getValue(), heightSlider.getValue());

                // Die korrekte Setup-Methode basierend auf dem Spielmodus aufrufen
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

                // Die neue Szene anzeigen
                Scene gameScene = new Scene(root);

                gameScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage.setIconified(true); // This minimizes the window
                    }
                });

                stage.setScene(gameScene);
                stage.setFullScreen(true);

            } catch (IOException ex) {
                System.err.println("Fehler beim Laden der Spielansicht (hello-view.fxml).");
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            clickSound.play();
            new GameCreationScreen(stage).show();
        });

        // --- Szene zusammenbauen und anzeigen ---
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
        stage.setTitle("Spielfeldgröße wählen");
        stage.setFullScreen(true);
        stage.show();
    }
}