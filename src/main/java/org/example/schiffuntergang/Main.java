package org.example.schiffuntergang;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;
import org.example.schiffuntergang.sounds.BackgroundMusic;
import org.example.schiffuntergang.sounds.SoundEffect;


public class Main extends Application {
    private static BackgroundMusic bgMusic;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        SoundEffect.setVolume(50);
        bgMusic = BackgroundMusic.getInstance();
        bgMusic.play(0.5);

        StartScreen startScreen = new StartScreen(primaryStage);
        startScreen.show();
    }
}
