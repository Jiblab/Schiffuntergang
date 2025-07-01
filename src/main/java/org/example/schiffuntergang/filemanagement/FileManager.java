package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.schiffuntergang.components.Gamefield;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Spielstand laden/speichern");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Save-Dateien", "save"));

    }

    /** Hier werden die Aktionen des Spielers und des Gegners temporär gespeichert
     * @param action Hier übergeben, was gemacht wurde
     * @deprecated Es würde aber mehr Sinn machen, einen Snapshot vom Spiel zu speichern, als jeden Schritt
     */
    public void getAction(String action) {

    }


    public void sampleDataAndSave(Gamefield spieler, Gamefield gegner){
        saveData = new SaveDataClass();
    }


    /**
     * Falls online-Spieler "save XXXX" schickt, soll bei uns lokal das Speichern ausgelöst werden
     * @param id Die Dateiname vom remote
     */
    public void saveFromRemote(String id){

    }

    /**
     * Das normale Speichern mit Chooser etc
     */
    private void save(){
        int returnValue = fileChooser.showOpenDialog(null);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            Gson gson = new GsonBuilder().create();
            try{
                FileReader fileReader = new FileReader(filePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
