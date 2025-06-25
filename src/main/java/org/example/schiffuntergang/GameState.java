package org.example.schiffuntergang;

import org.example.schiffuntergang.components.Position;

import java.util.List;

public class GameState {
    private int boardWidth;
    private int boardHeight;

    private double musikVolume;
    private boolean musikAktiv;
    private boolean soundEffekteAktiv;

    private List<SerializableShip> schiffe;
    private List<Position> bereitsGeschossen;

    public GameState(int boardWidth, int boardHeight, double musikVolume,
                     boolean musikAktiv, boolean soundEffekteAktiv,
                     List<SerializableShip> schiffe, List<Position> bereitsGeschossen) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.musikVolume = musikVolume;
        this.musikAktiv = musikAktiv;
        this.soundEffekteAktiv = soundEffekteAktiv;
        this.schiffe = schiffe;
        this.bereitsGeschossen = bereitsGeschossen;
    }

    // Getters
    public int getBoardWidth() {
        return boardWidth;
    }
    public int getBoardHeight() {
        return boardHeight;
    }
    public double getMusicVolume() {
        return musikVolume;
    }
    public List<SerializableShip> getShips(){
        return schiffe;
    }
    public List<Position> getShotCells() {
        return bereitsGeschossen;
    }

    // Setters
    public void setBoardWidth(int boardWidth) {
        this.boardWidth = boardWidth;
    }

    public void setBoardHeight(int boardHeight) {
        this.boardHeight = boardHeight;
    }

    public void setMusicVolume(double musicVolume){
        this.musikVolume = musicVolume;
    }

    public Position[] getHitPositions() {
        return new Position[1]; //unfertig
    }
}

