package org.example.schiffuntergang.sounds;
import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundEffect{
    private final AudioClip clip;
    private static double volume;

    public SoundEffect(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("sound nicht gefunden: " + path);
            }
            clip = new AudioClip(resource.toString());
            clip.setVolume(volume);
        } catch (Exception e) {
            throw new RuntimeException("sound lädt nicht: " + path, e);
        }
    }

    public void play() {
        if (clip != null) {
            clip.setVolume(volume);
            clip.play();
        }
    }


    public static double getVolume(){
        return volume;
    }

    public static void setVolume(double setvolume){
        volume = setvolume;
    }
}

