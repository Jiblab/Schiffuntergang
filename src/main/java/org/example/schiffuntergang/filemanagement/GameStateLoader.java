package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;

public class GameStateLoader {

    public GameStateLoader loadFromFile(String filePath) throws IOException {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            GameStateLoader state = gson.fromJson(reader, GameStateLoader.class);
            if (state == null) {
                throw new IOException("Spielstand ist leer oder ungültig.");
            }
            return state;
        } catch (JsonSyntaxException e) {
            throw new IOException("Fehler beim Parsen des Spielstands: " + e.getMessage(), e);
        }
    }
}

