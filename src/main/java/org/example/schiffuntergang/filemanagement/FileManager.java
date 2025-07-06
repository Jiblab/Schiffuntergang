package org.example.schiffuntergang.filemanagement;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

/**
 * Verwaltet alle Dateizugriffe für das Speichern und Laden von Spielständen.
 * Diese Klasse nutzt {@link JFileChooser} für die manuelle Auswahl von Dateien durch den Benutzer
 * und bietet auch Methoden für das automatisierte Speichern/Laden von Multiplayer-Spielständen
 * in einem vordefinierten Verzeichnis.
 */
public class FileManager {
    private final JFileChooser fileChooser = new JFileChooser();
    private final boolean newSave;
    private SaveDataClass saveData;

    /**
     * Erstellt eine neue Instanz des FileManagers.
     *
     * @param newFile Ein Flag, dessen Zweck im aktuellen Code nicht vollständig ersichtlich ist.
     */
    public FileManager(boolean newFile){
        newSave = newFile;
    }

    /**
     * Lädt einen Spielzustand von einem gegebenen Dateipfad, ohne einen Dialog anzuzeigen.
     *
     * @param filepath Der absolute Pfad zur Spielstandsdatei.
     * @return Der geladene {@link GameState}, oder null, wenn die Datei nicht existiert.
     * @throws IOException wenn ein Fehler beim Lesen der Datei auftritt.
     */
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

    /**
     * Öffnet einen "Öffnen"-Dialog, damit der Benutzer eine Spielstandsdatei manuell auswählen kann.
     * Liest die ausgewählte Datei und konvertiert den JSON-Inhalt in ein {@link GameState}-Objekt.
     *
     * @return Der geladene {@link GameState}, oder null, wenn der Benutzer den Dialog abbricht.
     * @throws IOException wenn ein Fehler beim Lesen der Datei auftritt.
     */
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

    /**
     * Öffnet einen "Speichern"-Dialog, damit der Benutzer einen Speicherort und Dateinamen auswählen kann.
     * Speichert die Daten aus dem übergebenen {@link SaveDataClass}-Objekt in der ausgewählten Datei.
     *
     * @param saveData Das Objekt, das die zu speichernden Daten enthält.
     */
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
     * Lädt einen Multiplayer-Spielstand automatisch anhand seiner ID.
     * Sucht im vordefinierten Spielstandsverzeichnis nach einer Datei mit dem entsprechenden Namen.
     *
     * @param id Die ID des zu ladenden Multiplayer-Spielstands.
     * @return Der geladene {@link GameState}, oder null, wenn keine passende Datei gefunden wird.
     */
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

    /**
     * Speichert einen Spielstand (typischerweise Multiplayer) automatisch in einem vordefinierten Verzeichnis.
     * Das Verzeichnis wird bei Bedarf erstellt.
     *
     * @param jsonData Der Spielzustand als JSON-formatierter String.
     * @param filename Der gewünschte Dateiname für die Speicherdatei.
     * @throws IOException wenn ein Fehler beim Schreiben der Datei auftritt.
     */
    public void saveGameData(String jsonData, String filename) throws IOException {
        String userHome = System.getProperty("user.home");
        File saveDir = new File(userHome, "SchiffUntergangSaves");

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