package org.example.schiffuntergang.filemanagement;

/**
 * Repräsentiert den vollständigen, speicherbaren Zustand eines Spiels zu einem bestimmten Zeitpunkt.
 * Diese Klasse bündelt alle relevanten Informationen, wie den Zustand beider Spielfelder,
 * Audio-Einstellungen und den aktuellen Spielzug. Sie dient als Hauptcontainer für die
 * Serialisierung des Spiels in eine Datei (z.B. im JSON-Format).
 */
public class GameState {

    /** Die Daten des Spielfelds des Hauptspielers. */
    private GamefieldData playerBoardData;
    /** Die Daten des gegnerischen Spielfelds. */
    private GamefieldData enemyBoardData;

    /** Die eingestellte Lautstärke der Hintergrundmusik. */
    private double musikVolume;
    /** Gibt an, ob die Hintergrundmusik aktiviert ist. */
    private boolean musikAktiv;
    /** Gibt an, ob Soundeffekte aktiviert sind. */
    private boolean soundEffekteAktiv;
    /** Gibt an, ob der Hauptspieler (der speichert) am Zug ist. */
    private boolean isPlayerTurn;
    /** Eine einzigartige ID für Multiplayer-Spielstände. */
    private long id;
    /** Gibt an, ob es sich um einen Multiplayer-Spielstand handelt. */
    private boolean isMultiplayer;

    /**
     * Standard-Konstruktor, der eine leere GameState-Instanz erstellt.
     */
    public GameState() {
    }

    /**
     * Erstellt eine neue GameState-Instanz mit allen relevanten Spieldaten.
     *
     * @param playerBoardData    Die Daten des Spieler-Spielfelds.
     * @param enemyBoardData     Die Daten des Gegner-Spielfelds.
     * @param musikVolume        Die aktuelle Musiklautstärke.
     * @param musikAktiv         Der Aktivierungsstatus der Musik.
     * @param soundEffekteAktiv  Der Aktivierungsstatus der Soundeffekte.
     * @param isPlayerTurn       Gibt an, ob der Spieler am Zug ist.
     */
    public GameState(GamefieldData playerBoardData, GamefieldData enemyBoardData, double musikVolume, boolean musikAktiv, boolean soundEffekteAktiv, boolean isPlayerTurn) {
        this.playerBoardData = playerBoardData;
        this.enemyBoardData = enemyBoardData;
        this.musikVolume = musikVolume;
        this.musikAktiv = musikAktiv;
        this.soundEffekteAktiv = soundEffekteAktiv;
        this.isPlayerTurn = isPlayerTurn;
    }

    /**
     * Gibt die Daten des Spieler-Spielfelds zurück.
     * @return die {@link GamefieldData} des Spielers.
     */
    public GamefieldData getPlayerBoardData() {
        return playerBoardData;
    }

    /**
     * Setzt die Daten des Spieler-Spielfelds.
     * @param playerBoardData die zu setzenden Spielfelddaten.
     */
    public void setPlayerBoardData(GamefieldData playerBoardData) {
        this.playerBoardData = playerBoardData;
    }

    /**
     * Gibt die Daten des Gegner-Spielfelds zurück.
     * @return die {@link GamefieldData} des Gegners.
     */
    public GamefieldData getEnemyBoardData() {
        return enemyBoardData;
    }

    /**
     * Setzt die Daten des Gegner-Spielfelds.
     * @param enemyBoardData die zu setzenden Spielfelddaten.
     */
    public void setEnemyBoardData(GamefieldData enemyBoardData) {
        this.enemyBoardData = enemyBoardData;
    }

    /**
     * Gibt die Lautstärke der Musik zurück.
     * @return die Musiklautstärke.
     */
    public double getMusikVolume() {
        return musikVolume;
    }

    /**
     * Setzt die Lautstärke der Musik.
     * @param musikVolume die zu setzende Lautstärke.
     */
    public void setMusikVolume(double musikVolume) {
        this.musikVolume = musikVolume;
    }

    /**
     * Prüft, ob die Musik aktiv ist.
     * @return true, wenn die Musik aktiv ist.
     */
    public boolean isMusikAktiv() {
        return musikAktiv;
    }

    /**
     * Setzt den Aktivierungsstatus der Musik.
     * @param musikAktiv der zu setzende Status.
     */
    public void setMusikAktiv(boolean musikAktiv) {
        this.musikAktiv = musikAktiv;
    }

    /**
     * Prüft, ob der Spieler am Zug ist.
     * @return true, wenn der Spieler am Zug ist.
     */
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    /**
     * Setzt, ob der Spieler am Zug ist.
     * @param playerTurn der zu setzende Zugstatus.
     */
    public void setPlayerTurn(boolean playerTurn) {
        isPlayerTurn = playerTurn;
    }

    /**
     * Prüft, ob dies ein Multiplayer-Spielstand ist.
     * @return true, wenn es ein Multiplayer-Spielstand ist.
     */
    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    /**
     * Gibt die ID des Multiplayer-Spielstands zurück.
     * @return die Spielstands-ID.
     */
    public long getId(){
        return id;
    }

    /**
     * Markiert diesen Spielstand als Multiplayer-Spielstand und weist ihm eine ID zu.
     * @param multiplayer true, um das Spiel als Multiplayer zu markieren.
     * @param createdid   Die einzigartige ID für diesen Spielstand.
     */
    public void setMultiplayer(boolean multiplayer, long createdid) {
        isMultiplayer = multiplayer;
        if(isMultiplayer){
            id = createdid;
        }
    }
}