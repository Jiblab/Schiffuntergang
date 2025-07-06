package org.example.schiffuntergang.filemanagement;

public class GameState {

    private GamefieldData playerBoardData;
    private GamefieldData enemyBoardData;

    private double musikVolume;
    private boolean musikAktiv;
    private boolean soundEffekteAktiv;
    private boolean isPlayerTurn;
    private long id;
    private boolean isMultiplayer;

    public GameState() {
    }

    public GameState(GamefieldData playerBoardData, GamefieldData enemyBoardData, double musikVolume, boolean musikAktiv, boolean soundEffekteAktiv, boolean isPlayerTurn) {
        this.playerBoardData = playerBoardData;
        this.enemyBoardData = enemyBoardData;
        this.musikVolume = musikVolume;
        this.musikAktiv = musikAktiv;
        this.soundEffekteAktiv = soundEffekteAktiv;
        this.isPlayerTurn = isPlayerTurn;
    }

    public GamefieldData getPlayerBoardData() {
        return playerBoardData;
    }

    public void setPlayerBoardData(GamefieldData playerBoardData) {
        this.playerBoardData = playerBoardData;
    }

    public GamefieldData getEnemyBoardData() {
        return enemyBoardData;
    }

    public void setEnemyBoardData(GamefieldData enemyBoardData) {
        this.enemyBoardData = enemyBoardData;
    }

    public double getMusikVolume() {
        return musikVolume;
    }

    public void setMusikVolume(double musikVolume) {
        this.musikVolume = musikVolume;
    }

    public boolean isMusikAktiv() {
        return musikAktiv;
    }

    public void setMusikAktiv(boolean musikAktiv) {
        this.musikAktiv = musikAktiv;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        isPlayerTurn = playerTurn;
    }

    //multiplayer
    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public long getId(){
        return id;
    }

    public void setMultiplayer(boolean multiplayer, long createdid) {
        isMultiplayer = multiplayer;
        if(isMultiplayer){
            id = createdid;
        }
    }
}