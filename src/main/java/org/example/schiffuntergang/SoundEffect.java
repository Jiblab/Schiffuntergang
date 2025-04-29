package org.example.schiffuntergang;
import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundEffect{
    private AudioClip clip;

    public SoundEffect(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("Sound file not found: " + path);
            }
            clip = new AudioClip(resource.toString());
        } catch (Exception e) {
            throw new RuntimeException("Could not load sound: " + path, e);
        }
    }

    public void play() {
        if (clip != null) {
            clip.play();
        }
    }
}

