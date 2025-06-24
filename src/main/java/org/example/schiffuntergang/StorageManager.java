package org.example.schiffuntergang;

import javafx.util.Pair;
import org.example.schiffuntergang.components.*;
import org.example.schiffuntergang.sounds.*;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String basePath = "data";

    public static void saveFullGame(Gamefield playerBoard, Gamefield aiBoard,
                                    double musicVolume, boolean musicEnabled,
                                    boolean soundEnabled, String filename)
    {
        GameState playerState = toGameState(playerBoard, musicVolume, musicEnabled, soundEnabled);
        GameState aiState = toGameState(aiBoard, musicVolume, musicEnabled, soundEnabled);

        FullGameState fullState = new FullGameState(playerState, aiState);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fullPath = Paths.get(basePath, filename + ".json").toString();

        try (FileWriter writer = new FileWriter(fullPath)) {
            gson.toJson(fullState, writer);
            System.out.println("Game successfully saved to: " + fullPath);
        } catch (IOException e) {
            System.err.println("Error while saving game: " + e.getMessage());
        }
    }

    public static Pair<Gamefield, Gamefield> loadFullGame(String filename,
                                                          HelloController controller,
                                                          EnemyPlayer enemyPlayer)
    {
        Gson gson = new Gson();
        String fullPath = Paths.get(basePath, filename + ".json").toString();

        try (FileReader reader = new FileReader(fullPath)) {
            FullGameState fullState = gson.fromJson(reader, FullGameState.class);

            Gamefield playerBoard = fromGameState(fullState.getPlayer(), controller, false, null);
            Gamefield aiBoard = fromGameState(fullState.getEnemyPlayer(), controller, true, enemyPlayer);

            //Restore audio settings
            SoundEffect.setVolume(fullState.getPlayer().getMusicVolume());
            BackgroundMusic.getInstance().setVolume(fullState.getPlayer().getMusicVolume());

            return new Pair<>(playerBoard, aiBoard);

        } catch (IOException e) {
            System.err.println("Error while loading game: " + e.getMessage());
            return null;
        }
    }
    private static GameState toGameState(Gamefield board, double volume, boolean music, boolean sound) {
        int width = (int)board.getWidth();
        int height = (int)board.getHeight();

        List<SerializableShip> shipList = new ArrayList<>();
        for (Ships ship : board.getShips()) {
            outer:
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (board.getCell(row, col).getShip() == ship) {
                        shipList.add(new SerializableShip(row, col, ship.getLength(), ship.getDirection(), ship.isAlive() ? ship.getLength() : 0));
                        break outer;
                    }
                }
            }
        }

        List<Position> hits = new ArrayList<>();
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++)
                if (board.getCell(row, col).isShot())
                    hits.add(new Position(row, col));

        return new GameState(width, height, volume, music, sound, shipList, hits);
    }
    private static Gamefield fromGameState(GameState state,
                                           HelloController controller,
                                           boolean isEnemy,
                                           EnemyPlayer aiLogic)
    {
        Gamefield board = isEnemy
                ? new Gamefield(true, controller, state.getBoardHeight(), state.getBoardWidth(), aiLogic)
                : new Gamefield(false, controller, state.getBoardHeight(), state.getBoardWidth());

        for (SerializableShip s : state.getShips()) {
            Ships ship = new Ships(s.getLength(), s.getHealth());
            if (board.placeShip(ship, s.getStartX(), s.getStartY(), s.isVertical())) {
                board.addShip(ship);
                board.increaseUsedCells(ship.getLength());
            }
        }

        for (Position p : state.getHitPositions()) {
            Cell cell = board.getCell(p.getX(), p.getY());
            cell.setShot(true);
            cell.setFill(cell.getShip() != null ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.BLACK);
        }
        return board;
    }

    public static Pair<Gamefield, Gamefield> loadFullGame(String absolutePath) {
        Gson gson = new GsonBuilder().create();

        try (FileReader reader = new FileReader(absolutePath)) {
            FullGameState fullState = gson.fromJson(reader, FullGameState.class);

            GameState playerState = fullState.getPlayerState();
            GameState enemyState = fullState.getEnemyState();

            Gamefield playerBoard = Gamefield.fromGameState(playerState, false); // false → ist Spieler
            Gamefield enemyBoard = Gamefield.fromGameState(enemyState, true);    // true  → ist Gegner

            return new Pair<>(playerBoard, enemyBoard);

        } catch (IOException e) {
            System.err.println("Fehler beim Laden des Spielstands: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}