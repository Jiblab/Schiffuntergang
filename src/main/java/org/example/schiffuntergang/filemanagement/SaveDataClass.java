package org.example.schiffuntergang.filemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.schiffuntergang.components.Gamefield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveDataClass {

    private Gamefield spieler, gegner;

    private Map<String, Object> datenzumspeichern = new HashMap<String, Object>();

    public SaveDataClass(Gamefield spielergamefield, Gamefield gegnergamefield) {
        spieler = spielergamefield;
        gegner = gegnergamefield;
    }

    public void prepareData() {
        datenzumspeichern.put("breit", spieler.getBreit());
        datenzumspeichern.put("lang", spieler.getLang());
        datenzumspeichern.put("shipsspieler", spieler.getShips());
        datenzumspeichern.put("shipsgegner",gegner.getShips());

    }

    public void setData(){

    }

    public String getDatenzumspeichern() {
        // 1. Convert your LIVE Gamefield objects into DATA objects.
        GamefieldData playerData = spieler.toData();
        GamefieldData enemyData = gegner.toData();

        // 2. Create a top-level GameState object to hold everything.
        GameState completeGameState = new GameState();
        completeGameState.setPlayerBoardData(playerData); // Assumes GameState has these fields
        completeGameState.setEnemyBoardData(enemyData);
        // ... save other game state like whose turn it is, etc.

        // 3. Serialize the top-level DATA object. THIS IS SAFE!
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(completeGameState);
        return json;
    }

}
