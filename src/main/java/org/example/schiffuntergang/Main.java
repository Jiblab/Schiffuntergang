package org.example.schiffuntergang;
import javafx.application.Application;
import javafx.stage.Stage;
import org.example.schiffuntergang.StartScreen;
import javafx.scene.input.KeyCombination;


public class Main extends Application {
    private static BackgroundMusic bgMusic;
    public static void main(String[] args) {
        bgMusic = new BackgroundMusic("/music/BGmusic1.mp3");
        bgMusic.play(0.5);
        SoundEffect.setVolume(50);
        launch(args);
    }

    public static BackgroundMusic getBGmusic() {
        return bgMusic;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        StartScreen startScreen = new StartScreen(primaryStage);
        startScreen.show();
    }
}
