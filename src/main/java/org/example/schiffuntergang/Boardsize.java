package org.example.schiffuntergang;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane; // NEU: Import für StackPane
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

    // Die öffentlichen Methoden rufen jetzt nur noch die private Helfermethode auf.
    public void show() {
        createAndShowScene(false);
    }

    public void showMulti() {
        createAndShowScene(true);
    }

    /**
     * Private Helfermethode, um Codeduplizierung zwischen Single- und Multiplayer-Ansicht zu vermeiden.
     *
     * @param isMultiplayerSetup Legt fest, ob setup() oder setupMultiS() aufgerufen wird.
     */
    private void createAndShowScene(boolean isMultiplayerSetup) {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");

        // --- Steuerelemente erstellen (unverändert) ---
        Slider slider1 = new Slider(0, 30, 10);
        slider1.setShowTickLabels(true);
        slider1.setShowTickMarks(true);

        Slider slider2 = new Slider(0, 30, 10);
        slider2.setShowTickLabels(true);
        slider2.setShowTickMarks(true);

        Label label1 = new Label("Boardbreite: 10");
        Label label2 = new Label("Boardhöhe: 10"); // "Boardsize" war doppelt, ich habe es in "Boardhöhe" geändert für Klarheit

        slider1.valueProperty().addListener((obs, oldVal, newVal) ->
                label1.setText("Boardbreite: " + String.format("%.0f", newVal.doubleValue()))
        );
        slider2.valueProperty().addListener((obs, oldVal, newVal) ->
                label2.setText("Boardhöhe: " + String.format("%.0f", newVal.doubleValue()))
        );

        Button start = new Button("Start Game");
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

                // Hier wird basierend auf dem Parameter die richtige Setup-Methode aufgerufen
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
        backtostart.setOnAction(e -> {
            // isSinglePlayer wird aus der Klasseninstanz wiederverwendet
            GameCreationScreen gameScreen = new GameCreationScreen(stage);
            clickSound.play();
            gameScreen.show();
        });

        // --- Layout-Struktur (GEÄNDERT) ---

        // 1. VBox für die Steuerelemente erstellen (wie zuvor, aber ohne Hintergrund)
        VBox controlsLayout = new VBox(15, label1, slider1, label2, slider2, start, backtostart);
        controlsLayout.setStyle("-fx-padding: 30px;");
        controlsLayout.setAlignment(Pos.CENTER);
        // WICHTIG: Die VBox sollte selbst keinen undurchsichtigen Hintergrund haben.
        // Meist ist das Standard, aber falls nicht, füge dies hinzu:
        controlsLayout.setStyle("-fx-background-color: transparent;");


        // 2. StackPane als neuer Hauptcontainer erstellen
        StackPane rootPane = new StackPane();
        rootPane.getStyleClass().add("background"); // Hintergrund auf das StackPane anwenden
        rootPane.getChildren().add(controlsLayout); // Die VBox mit den Controls in das StackPane legen

        // 3. Szene mit dem StackPane als Wurzel erstellen
        Scene scene = new Scene(rootPane); // Die Größe wird durch den Vollbildmodus automatisch angepasst
        scene.getStylesheets().add(getClass().getResource("/background.css").toExternalForm());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(false);
                stage.setResizable(true);
                // Du kannst hier optional eine feste Größe für den Fenstermodus setzen
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
}