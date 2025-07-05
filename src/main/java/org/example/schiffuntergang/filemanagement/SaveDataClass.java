package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.schiffuntergang.components.Gamefield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import java.io.IOException;

public class SaveDataClass {

    private Gamefield spieler, gegner;
    private Map<String, Object> datenzumspeichern = new HashMap<String, Object>();

    //multiplayer
    private MultiplayerLogic logic;

    //Fürs Speichern
    public SaveDataClass(Gamefield spielergamefield, Gamefield gegnergamefield) {
        spieler = spielergamefield;
        gegner = gegnergamefield;
    }

    //Fürs Laden
    public SaveDataClass(){

    }

    //multiplayer
    public SaveDataClass(Gamefield spielergamefield, Gamefield gegnergamefield, MultiplayerLogic logic) {
        this.spieler = spielergamefield;
        this.gegner = gegnergamefield;
        this.logic = logic;
    }

    /*public void prepareData() {
        datenzumspeichern.put("breit", spieler.getBreit());
        datenzumspeichern.put("lang", spieler.getLang());
        datenzumspeichern.put("shipsspieler", spieler.getShips());
        datenzumspeichern.put("shipsgegner",gegner.getShips());

    }*/

    public GameState loadData(String json){
        GameState completeGameState;

        Gson gson = new Gson();
        completeGameState = gson.fromJson(json, GameState.class);

        return completeGameState;
    }

    public String getDatenzumspeichern() {
        GameState completeGameState = createGameState();
        completeGameState.setMultiplayer(false); // Explizit als Singleplayer markieren

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(completeGameState);
    }

    public void saveMultiplayerGame() {
        if (this.logic == null) {
            System.err.println("[SaveDataClass] FEHLER: Multiplayer-Spiel kann nicht gespeichert werden. MultiplayerLogic fehlt.");
            return;
        }

        System.out.println("\n[SaveDataClass] Multiplayer-Speichervorgang wird gestartet...");

        long saveId = System.currentTimeMillis();
        String filename = "mp_save_" + saveId + ".save";
        System.out.println("[SaveDataClass] Generierte Speicher-ID: " + saveId);

        GameState completeGameState = createGameState();
        completeGameState.setMultiplayer(true);
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
