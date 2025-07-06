package org.example.schiffuntergang.filemanagement;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;


public class FileManager {
    JFileChooser fileChooser = new JFileChooser();
    private final boolean newSave;
    private SaveDataClass saveData;

    public FileManager(boolean newFile){
        newSave = newFile;
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
    public GameState loadfromid(long id){
        String userHome = System.getProperty("user.home");
        File saveDir = new File(userHome, "SchiffUntergangSaves");
        if(!saveDir.exists()){
            return null;
        }
        String filename = "mp_save_" + id + ".save";
        File filetoLoad = new File(saveDir, filename);
        if(!filetoLoad.exists()){
            return null;
        }
        GameState gameState = null;
        try {
            FileReader fileReader = new FileReader(filetoLoad.getAbsolutePath());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            SaveDataClass save = new SaveDataClass();
            gameState = save.loadData(jsonString);
        }catch(IOException e){
            System.err.println(e);
        }
        return gameState;
    }
    //multiplayer
    public void saveGameData(String jsonData, String filename) throws IOException {
        String userHome = System.getProperty("user.home");
        File saveDir = new File(userHome, "SchiffUntergangSaves");

        // erstellt verzeichnis
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