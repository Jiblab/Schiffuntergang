package org.example.schiffuntergang.filemanagement;

/**
 * Ein Datenübertragungsobjekt (DTO), das die essenziellen Daten eines
 * {@link org.example.schiffuntergang.components.Ships}-Objekts in einer einfachen,
 * serialisierbaren Form darstellt.
 * Diese Klasse wird verwendet, um den Zustand von Schiffen in Spielständen zu speichern
 * und zu laden, da sie leicht in Formate wie JSON konvertiert werden kann.
 */
public class SerializableShip {
    /** Die X-Koordinate (Spalte) der Startzelle des Schiffs. */
    private int startX;
    /** Die Y-Koordinate (Reihe) der Startzelle des Schiffs. */
    private int startY;
    /** Die Länge des Schiffs in Zellen. */
    private int length;
    /** Die Ausrichtung des Schiffs. true für vertikal, false für horizontal. */
    private boolean vertical;
    /** Die aktuelle Gesundheit (Anzahl der ungetroffenen Teile) des Schiffs. */
    private int health;

    /**
     * Leerer Standardkonstruktor.
     * Wird von Serialisierungs-Bibliotheken wie Gson benötigt.
     */
    public SerializableShip() {
    }

    /**
     * Erstellt ein neues serialisierbares Schiff mit allen notwendigen Daten.
     *
     * @param startX   Die Start-Spalte des Schiffs.
     * @param startY   Die Start-Reihe des Schiffs.
     * @param length   Die Länge des Schiffs.
     * @param vertical Die Ausrichtung des Schiffs.
     * @param health   Die aktuelle Gesundheit des Schiffs.
     */
    public SerializableShip(int startX, int startY, int length, boolean vertical, int health) {
        this.startX = startX;
        this.startY = startY;
        this.length = length;
        this.vertical = vertical;
        this.health = health;
    }

    /**
     * Gibt die Start-X-Koordinate (Spalte) zurück.
     * @return die Start-Spalte.
     */
    public int getStartX() { return startX; }

    /**
     * Gibt die Start-Y-Koordinate (Reihe) zurück.
     * @return die Start-Reihe.
     */
    public int getStartY() { return startY; }

    /**
     * Gibt die Länge des Schiffs zurück.
     * @return die Länge.
     */
    public int getLength() { return length; }

    /**
     * Gibt die aktuelle Gesundheit des Schiffs zurück.
     * @return die Gesundheit.
     */
    public int getHealth() { return health; }

    /**
     * Prüft, ob das Schiff vertikal ausgerichtet ist.
     * @return true, wenn vertikal, sonst false.
     */
    public boolean isVertical() { return vertical; }

    /**
     * Setzt die Start-X-Koordinate (Spalte).
     * @param startX die zu setzende Spalte.
     */
    public void setStartX(int startX) { this.startX = startX; }

    /**
     * Setzt die Start-Y-Koordinate (Reihe).
     * @param startY die zu setzende Reihe.
     */
    public void setStartY(int startY) { this.startY = startY; }

    /**
     * Setzt die Länge des Schiffs.
     * @param length die zu setzende Länge.
     */
    public void setLength(int length) { this.length = length; }

    /**
     * Setzt die Ausrichtung des Schiffs.
     * @param vertical die zu setzende Ausrichtung.
     */
    public void setVertical(boolean vertical) { this.vertical = vertical; }

    /**
     * Setzt die Gesundheit des Schiffs.
     * @param health die zu setzende Gesundheit.
     */
    public void setHealth(int health) { this.health = health; }
}