package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.sounds.BackgroundMusic;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class FileManager {

    JFileChooser fileChooser = new JFileChooser();
    private boolean newSave;
    private SaveDataClass saveData;

    /** Konstruktor des FileManagers
     * Hier wird die Datei vorbereitet
     * @param newFile Hiermit bestimmt man, ob ein neues Save vorbereitet werden soll
     */
    public FileManager(boolean newFile){
        newSave = newFile;

    }

    /** Hier werden die Aktionen des Spielers und des Gegners temporär gespeichert
     * @param action Hier übergeben, was gemacht wurde
     * @deprecated Es würde aber mehr Sinn machen, einen Snapshot vom Spiel zu speichern, als jeden Schritt
     */
    public void getAction(String action) {

    }

    public GameState loadFromURI(String filepath) throws IOException {

            File file = new File(filepath);
            if(!file.exists()){
                return null;
            }
            FileReader fileReader = new FileReader(file.getAbsolutePath());

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            SaveDataClass save = new SaveDataClass();
            GameState gameState = save.loadData(jsonString);
            return gameState;

    }


    public GameState load() throws IOException {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Spielstand laden");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Spielstand", "save"));
        int returnvalue = fileChooser.showOpenDialog(null);
        if (returnvalue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            FileReader fileReader = new FileReader(file.getAbsolutePath());

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            SaveDataClass save = new SaveDataClass();
            GameState gameState = save.loadData(jsonString);
            return gameState;
        }
        return null;
    }


    public void save(SaveDataClass saveData){
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Spielstand laden/speichern");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Spielstand", "save"));

        int returnValue = fileChooser.showSaveDialog(null);
        if(returnValue == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            String filepath = file.getAbsolutePath();
            if(!filepath.endsWith(".save")){
                file = new File(filepath + ".save");
            }
            try{
                FileWriter writer = new FileWriter(file.getAbsoluteFile());
                writer.write(saveData.getDatenzumspeichern());
                writer.close();
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Falls online-Spieler "save XXXX" schickt, soll bei uns lokal das Speichern ausgelöst werden
     * @param id Die Dateiname vom remote
     */
    public void saveFromRemote(String id){

    }

    //multiplayer
    public void saveGameData(String jsonData, String filename) throws IOException {
        String userHome = System.getProperty("user.home");
        File saveDir = new File(userHome, "SchiffUntergangSaves");

        // Erstellt Verzeichnis
        if (!saveDir.exists()) {
            if (saveDir.mkdirs()) {
                System.out.println("[FileManager] Verzeichnis für Spielstände erstellt unter: " + saveDir.getAbsolutePath());
            } else {
                System.err.println("[FileManager] FEHLER: Konnte Verzeichnis für Spielstände nicht erstellen.");
                saveDir = new File(userHome);
            }
        }

        File fileToSave = new File(saveDir, filename);

        try (FileWriter writer = new FileWriter(fileToSave)) {
            writer.write(jsonData);
            System.out.println("--------------------------------------------------");
            System.out.println("[FileManager] Spielstand erfolgreich gespeichert!");
            System.out.println("[FileManager] Dateiname: " + filename);
            System.out.println("[FileManager] Speicherort: " + fileToSave.getAbsolutePath());
            System.out.println("--------------------------------------------------");
        } catch (IOException e) {
            System.err.println("[FileManager] FEHLER beim Schreiben des Spielstands: " + fileToSave.getAbsolutePath());
            throw e;
        }
    }

}
