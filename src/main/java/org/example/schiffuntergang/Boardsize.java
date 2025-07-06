package org.example.schiffuntergang;

import org.example.schiffuntergang.sounds.SoundEffect;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Verantwortlich für die Anzeige und Verwaltung des Bildschirms,
 * auf dem der Spieler die Größe des Spielfelds festlegen kann.
 * Diese Klasse erstellt die UI-Komponenten (Slider, Buttons) und leitet
 * basierend auf der Auswahl des Spielmodus zum eigentlichen Spiel weiter.
 */
public class Boardsize {
    /** Die Haupt-Stage der Anwendung, auf der die Szenen angezeigt werden. */
    private final Stage stage;
    /** Gibt an, ob der vorherige Bildschirm ein Einzelspieler-Setup war. */
    private final boolean isSinglePlayer;

    static {
        try {
            // Lädt die benutzerdefinierte Schriftart für die UI.
            Font.loadFont(Boardsize.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("[Boardsize] Pixel-Schriftart konnte nicht geladen werden");
            e.printStackTrace();
        }
    }

    /**
     * Erstellt eine neue Instanz des Boardsize-Bildschirms.
     *
     * @param stage          Die Haupt-Stage der Anwendung.
     * @param isSinglePlayer True, wenn der vorherige Modus Einzelspieler war, sonst false.
     */
    public Boardsize(Stage stage, boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.stage = stage;
    }

    /**
     * Zeigt den Einstellungsbildschirm für ein Standardspiel (Einzelspieler) an.
     */
    public void show() {
        createAndShowScene(false, false);
    }

    /**
     * Zeigt den Einstellungsbildschirm für das Hosten eines Multiplayer-Spiels an.
     * Kann zwischen einem menschlichen Host und einem KI-Host unterscheiden.
     *
     * @param ki True, wenn ein KI-gesteuertes Spiel gehostet wird, sonst false.
     */
    public void showMulti(boolean ki) {
        createAndShowScene(true, ki);
    }

    /**
     * Die zentrale Methode, die die Benutzeroberfläche für die Spielfeldgröße erstellt, konfiguriert und anzeigt.
     *
     * @param isMultiplayerHost True, wenn ein Multiplayer-Spiel gehostet wird (zeigt die IP an).
     * @param ki                True, wenn das Spiel KI-gesteuert ist.
     */
    private void createAndShowScene(boolean isMultiplayerHost, boolean ki) {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        // --- UI-Komponenten erstellen ---
        Label widthLabel = new Label("Width: 10");
        Slider widthSlider = new Slider(5, 30, 10); // Mindestgröße 5 für sinnvolles Spiel

        Label heightLabel = new Label("Height: 10");
        Slider heightSlider = new Slider(5, 30, 10);

        // Slider mit Labels verbinden, um die aktuelle Größe anzuzeigen.
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

        // --- Layout erstellen ---
        VBox controlsLayout = new VBox(15, widthLabel, widthSlider, heightLabel, heightSlider, startButton, backButton);
        controlsLayout.setPadding(new Insets(50));
        controlsLayout.setAlignment(Pos.CENTER);
        controlsLayout.maxWidthProperty().bind(stage.widthProperty().multiply(0.5));
        VBox.setMargin(startButton, new Insets(40, 0, 0, 0)); // Abstand nach oben

        // --- Bedingte Logik für Multiplayer-Host ---
        // Zeigt die lokale IP-Adresse an, wenn ein Multiplayer-Spiel gehostet wird.
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

            // Füge die IP-Anzeige ganz oben im Layout ein
            controlsLayout.getChildren().add(0, ipInfoLabel);
            controlsLayout.getChildren().add(1, ipField);
        }

        // --- Button-Aktionen definieren ---
        startButton.setOnAction(e -> {
            clickSound.play();
            try {
                // Lädt die FXML-Datei für die Hauptspielansicht.
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/schiffuntergang/hello-view.fxml"));
                Parent root = loader.load();
                HelloController controller = loader.getController();

                // Konfiguriert den Controller mit den gewählten Werten.
                controller.setStage(stage);
                controller.setSize(widthSlider.getValue(), heightSlider.getValue());

                // Ruft die korrekte Setup-Methode im Controller basierend auf dem Spielmodus auf.
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

                // Zeigt die neue Spielszene an.
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

        // Aktion für den "Zurück"-Button.
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

        // Escape-Key-Handler für die Szene.
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