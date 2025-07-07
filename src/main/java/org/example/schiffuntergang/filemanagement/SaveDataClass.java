package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.schiffuntergang.components.Gamefield;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

/**
 * Verantwortlich für das Sammeln, Formatieren und Initiieren des Speicher- und Ladevorgangs.
 * Diese Klasse arbeitet als Brücke zwischen dem aktuellen Spielzustand (repräsentiert durch
 * die {@link Gamefield}-Objekte) und dem serialisierbaren {@link GameState}-Format.
 * Sie nutzt Gson für die Konvertierung von und zu JSON.
 */
public class SaveDataClass {
    private Gamefield spieler, gegner;
    private Map<String, Object> datenzumspeichern = new HashMap<String, Object>();
    private MultiplayerLogic logic;

    /**
     * Konstruktor für das Speichern eines Einzelspieler-Spiels.
     *
     * @param spielergamefield Das Spielfeld des Spielers.
     * @param gegnergamefield  Das Spielfeld des Gegners.
     */
    public SaveDataClass(Gamefield spielergamefield, Gamefield gegnergamefield) {
        spieler = spielergamefield;
        gegner = gegnergamefield;
    }

    /**
     * Leerer Standardkonstruktor.
     * Nützlich für Instanzen, die hauptsächlich zum Laden von Daten verwendet werden.
     */
    public SaveDataClass(){

    }

    /**
     * Konstruktor für das Speichern eines Multiplayer-Spiels.
     *
     * @param spielergamefield Das Spielfeld des Spielers.
     * @param gegnergamefield  Das Spielfeld des Gegners.
     * @param logic            Die Instanz der {@link MultiplayerLogic}, die für den Spielzustand benötigt wird.
     */
    public SaveDataClass(Gamefield spielergamefield, Gamefield gegnergamefield, MultiplayerLogic logic) {
        this.spieler = spielergamefield;
        this.gegner = gegnergamefield;
        this.logic = logic;
    }

    /**
     * Deserialisiert einen JSON-String in ein {@link GameState}-Objekt.
     *
     * @param json Der JSON-String, der den vollständigen Spielzustand repräsentiert.
     * @return Ein {@link GameState}-Objekt, das aus dem JSON-String erstellt wurde.
     */
    public GameState loadData(String json){
        GameState completeGameState;

        Gson gson = new Gson();
        completeGameState = gson.fromJson(json, GameState.class);

        return completeGameState;
    }

    /**
     * Erstellt einen JSON-String aus dem aktuellen Spielzustand für ein Einzelspieler-Spiel.
     *
     * @return Ein JSON-formatierter String, der den Spielzustand repräsentiert.
     */
    public String getDatenzumspeichern() {
        GameState completeGameState = createGameState();
        completeGameState.setMultiplayer(false, 0); // Explizit als Singleplayer markieren

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(completeGameState);
    }

    /**
     * Koordiniert den vollständigen Speichervorgang für ein Multiplayer-Spiel.
     * Dies beinhaltet das Erstellen eines lokalen Spielstands und das Senden eines
     * Speicherbefehls an den gegnerischen Spieler über das Netzwerk.
     */
    public void saveMultiplayerGame() {
        if (this.logic == null) {
            System.err.println("[SaveDataClass] FEHLER: Multiplayer-Spiel kann nicht gespeichert werden. MultiplayerLogic fehlt.");
            return;
        }

        System.out.println("[SaveDataClass] Multiplayer-Speichervorgang wird gestartet...");

        long saveId = System.currentTimeMillis();
        String filename = "mp_save_" + saveId + ".save";
        System.out.println("[SaveDataClass] Generierte Speicher-ID: " + saveId);

        GameState completeGameState = createGameState();
        completeGameState.setMultiplayer(true, saveId);
        completeGameState.setPlayerTurn(logic.getTurn());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(completeGameState);

        System.out.println("[SaveDataClass] Versuche, das Spiel lokal zu speichern...");
        FileManager fileManager = new FileManager(false);
        try {
            fileManager.saveGameData(json, filename);
            if (spieler != null && spieler.getControl() != null) {
                spieler.getControl().showNotification("Spiel gespeichert: " + filename, "info");
            }
        } catch (IOException e) {
            System.err.println("[SaveDataClass] FEHLER: Lokales Speichern des Multiplayer-Spiels fehlgeschlagen: " + e.getMessage());
            return;
        }

        try {
            System.out.println("[SaveDataClass] Sende Speicherbefehl an den Gegner mit der ID: " + saveId);
            logic.sendSaveCommand(saveId);
        } catch (IOException e) {
            System.err.println("[SaveDataClass] FEHLER: Senden des Speicherbefehls an den Gegner fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Private Hilfsmethode, die ein {@link GameState}-Objekt aus den aktuellen
     * Spielfeld- und Audio-Daten erstellt.
     *
     * @return Ein neues {@link GameState}-Objekt.
     */
    private GameState createGameState() {
        GamefieldData playerData = spieler.toData();
        GamefieldData enemyData = gegner.toData();

        GameState state = new GameState();
        state.setPlayerBoardData(playerData);
        state.setEnemyBoardData(enemyData);

        if (spieler != null) {
            state.setMusikAktiv(spieler.isMusicEnabled());
            state.setMusikVolume(spieler.getMusicVolume());
        }
        return state;
    }
}