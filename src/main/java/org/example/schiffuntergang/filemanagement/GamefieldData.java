package org.example.schiffuntergang.filemanagement;

import org.example.schiffuntergang.components.Position;

import java.util.List;
import java.util.ArrayList;

/**
 * Ein Datenübertragungsobjekt (Data Transfer Object, DTO), das alle notwendigen Informationen
 * zur Rekonstruktion eines einzelnen {@link org.example.schiffuntergang.components.Gamefield} speichert.
 * Diese Klasse ist so konzipiert, dass sie einfach in ein JSON-Format serialisiert und
 * daraus deserialisiert werden kann.
 */
public class GamefieldData {
    /** Die Breite des Spielfelds (Anzahl der Spalten). */
    private int width;
    /** Die Höhe des Spielfelds (Anzahl der Reihen). */
    private int height;
    /** Gibt an, ob es sich um das Spielfeld des Gegners handelt. */
    private boolean isEnemy;
    /** Eine Liste der auf dem Feld platzierten Schiffe in einem serialisierbaren Format. */
    private List<SerializableShip> ships;
    /** Eine Liste aller Positionen, auf die bereits geschossen wurde. */
    private List<Position> shotPositions;
    /** Eine Liste von Positionen, die im Multiplayer als getroffen markiert wurden. */
    private List<Position> hitVariables;

    /**
     * Erstellt eine neue, leere Instanz von GamefieldData.
     * Initialisiert die Listen, um NullPointerExceptions zu vermeiden.
     */
    public GamefieldData() {
        this.ships = new ArrayList<>();
        this.shotPositions = new ArrayList<>();
        this.hitVariables = new ArrayList<>();
    }

    /**
     * Gibt die Breite des Spielfelds zurück.
     * @return die Breite.
     */
    public int getWidth() { return width; }

    /**
     * Setzt die Breite des Spielfelds.
     * @param width die zu setzende Breite.
     */
    public void setWidth(int width) { this.width = width; }

    /**
     * Gibt die Höhe des Spielfelds zurück.
     * @return die Höhe.
     */
    public int getHeight() { return height; }

    /**
     * Setzt die Höhe des Spielfelds.
     * @param height die zu setzende Höhe.
     */
    public void setHeight(int height) { this.height = height; }

    /**
     * Prüft, ob dies die Daten eines Gegnerfeldes sind.
     * @return true, wenn es ein Gegnerfeld ist.
     */
    public boolean isEnemy() { return isEnemy; }

    /**
     * Setzt, ob dies die Daten eines Gegnerfeldes sind.
     * @param enemy der zu setzende boolean-Wert.
     */
    public void setEnemy(boolean enemy) { isEnemy = enemy; }

    /**
     * Gibt die Liste der serialisierbaren Schiffe zurück.
     * @return eine Liste von {@link SerializableShip}-Objekten.
     */
    public List<SerializableShip> getShips() { return ships; }

    /**
     * Setzt die Liste der serialisierbaren Schiffe.
     * @param ships die zu setzende Liste.
     */
    public void setShips(List<SerializableShip> ships) { this.ships = ships; }

    /**
     * Gibt die Liste der Positionen zurück, auf die geschossen wurde.
     * @return eine Liste von {@link Position}-Objekten.
     */
    public List<Position> getShotPositions() { return shotPositions; }

    /**
     * Setzt die Liste der Positionen, auf die geschossen wurde.
     * @param shotPositions die zu setzende Liste.
     */
    public void setShotPositions(List<Position> shotPositions) { this.shotPositions = shotPositions; }

    /**
     * Gibt die Liste der als getroffen markierten Positionen zurück.
     * @return eine Liste von {@link Position}-Objekten.
     */
    public List<Position> getHitVariables() { return hitVariables; }

    /**
     * Setzt die Liste der als getroffen markierten Positionen.
     * @param hitVariables die zu setzende Liste.
     */
    public void setHitVariables(List<Position> hitVariables) { this.hitVariables = hitVariables; }
}