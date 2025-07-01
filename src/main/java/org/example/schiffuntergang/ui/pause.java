package org.example.schiffuntergang.ui;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;

public class pause {
    private final Stage stage;
    private static BackgroundMusic bgMusic;

    public pause(Stage stage) {
        this.stage = stage;
        bgMusic = BackgroundMusic.getInstance();
    }

    public void show() {
        SoundEffect clickSound = new SoundEffect("/music/ButtonBeepmp3.mp3");
        //Save game
        //Musik an aus
        ToggleSwitch musictoggle = new ToggleSwitch("MUTE MUSIC");

        Button exit = new Button("Exit Game");
        exit.setOnAction(e -> {
            System.exit(0);
        });

        Button saveGame = new Button("Save Game");

    }

    void adjustFontSize(Button button, double baseWidth) {
        double size = stage.getWidth() / baseWidth;
        button.setStyle("-fx-font-size:" + size + "px");
    }
}
