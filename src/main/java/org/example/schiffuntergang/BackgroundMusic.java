package org.example.schiffuntergang;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import java.net.URI;

import java.io.File;
import java.net.URL;

public class BackgroundMusic {
    private MediaPlayer mediaPlayer;
    private Timeline fadeInTimeline;
    private Timeline fadeOutTimeline;

    public BackgroundMusic(String rPath) {
        try {
            URL resourceUrl = getClass().getResource(rPath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Resource not found" + rPath);
            }
            Media media = new Media(resourceUrl.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Cannot load Resource" + rPath, e);
        }
    }
    public void play(double targetVol){
        mediaPlayer.setVolume(0.0);
        mediaPlayer.play();
        fadeIn(targetVol);
    }
    public void stop(){
        if (fadeInTimeline != null){
            fadeInTimeline.stop();
        }
        if (fadeOutTimeline != null){
            fadeOutTimeline.stop();
        } mediaPlayer.stop();
    }
    // Fade-in effect to gradually increase volume
    public void fadeIn(double targetVolume) {
        fadeInTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    double currentVolume = mediaPlayer.getVolume();
                    if (currentVolume < targetVolume) {
                        mediaPlayer.setVolume(Math.min(targetVolume, currentVolume + 0.01));
                    } else {
                        fadeInTimeline.stop();
                    }
                })
        );
        fadeInTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeInTimeline.play();
    }

    // Fade-out effect to gradually decrease volume
    public void fadeOut() {
        fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    double currentVolume = mediaPlayer.getVolume();
                    if (currentVolume > 0.0) {
                        mediaPlayer.setVolume(Math.max(0.0, currentVolume - 0.01));
                    } else {
                        fadeOutTimeline.stop();
                        mediaPlayer.stop();
                    }
                })
        );
        fadeOutTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeOutTimeline.play();
    }

    // Set a specific volume
    public void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }

    // Get the current volume
    public double getVolume() {
        return mediaPlayer.getVolume();
    }

    // Check if music is currently playing
    public boolean isPlaying() {
        return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
}

