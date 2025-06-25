package org.example.schiffuntergang;

public class FullGameState {

    public GameState player;
    public GameState enemyPlayer;

    // @param player the player's game state
    // @param ai     the AI's game state
    public FullGameState(GameState player, GameState enemyPlayer) {
        this.player = player;
        this.enemyPlayer = enemyPlayer;
    }

    public GameState getPlayer() {
        return player;
    }

    public GameState getEnemyPlayer() {
        return enemyPlayer;
    }

    public void setPlayer(GameState player) {
        this.player = player;
    }

    public void setEnemyPlayer(GameState enemyPlayer) {
        this.enemyPlayer = this.enemyPlayer;
    }

    public GameState getPlayerState() {
        return player;
    }
    public GameState getEnemyState() {
        return enemyPlayer;
    }

}
