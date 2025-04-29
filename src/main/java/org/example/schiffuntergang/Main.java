package org.example.schiffuntergang;
import javafx.application.Application;
import javafx.stage.Stage;
import org.example.schiffuntergang.StartScreen;
import javafx.scene.input.KeyCombination;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // verhindert JavaFX-ESC-Auslösung

        StartScreen startScreen = new StartScreen(primaryStage);
        startScreen.show();
    }


    public static void main(String[] args) {
        BackgroundMusic bgMusic = new BackgroundMusic("/music/01.TitleScreen.mp3");
        bgMusic.play(0.5);
        launch(args);
    }
}
