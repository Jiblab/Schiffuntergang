package org.example.schiffuntergang.components;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.schiffuntergang.HelloController;

/**
 * Repräsentiert eine einzelne Zelle auf dem Spielfeld.
 * Diese Klasse erweitert {@link Rectangle} und ist somit eine visuelle Komponente.
 * Sie speichert Informationen über ihren Zustand, wie z.B. ob sie beschossen wurde
 * oder ob sie Teil eines Schiffs ist.
 */
public class Cell extends Rectangle {
    /** Gibt an, ob auf diese Zelle bereits geschossen wurde. */
    private boolean shot = false;
    /** Referenz auf das Schiff, das sich auf dieser Zelle befindet. Ist null, wenn die Zelle leer ist. */
    private Ships ship = null;
    /** Die X-Koordinate der Zelle auf dem Spielfeld (Spalte). */
    public int x;
    /** Die Y-Koordinate der Zelle auf dem Spielfeld (Reihe). */
    public int y;
    /** Eine Referenz auf das {@link Gamefield}, zu dem diese Zelle gehört. */
    private final Gamefield board;
    /** Eine Referenz zum Haupt-Controller für UI-Interaktionen. */
    private final HelloController control;

    /** Speichert im Multiplayer, ob ein Schiff auf dieser Zelle getroffen wurde. */
    private boolean shipHit = false;

    /**
     * Erstellt eine neue Zelle für das Spielfeld.
     *
     * @param x         Die X-Koordinate (Spalte) der Zelle.
     * @param y         Die Y-Koordinate (Reihe) der Zelle.
     * @param board     Das {@link Gamefield}-Objekt, zu dem diese Zelle gehört.
     * @param h         Die Höhe der Zelle in Pixel.
     * @param w         Die Breite der Zelle in Pixel.
     * @param controler Der {@link HelloController} für die Spielsteuerung.
     */
    public Cell(int x, int y, Gamefield board, int h, int w, HelloController controler){
        super(h, w);
        this.x = x;
        this.y = y;
        this.board = board;
        this.control = controler;
    }

    /**
     * Gibt das Spielfeld zurück, zu dem diese Zelle gehört.
     *
     * @return Das {@link Gamefield}-Objekt.
     */
    public Gamefield getBoard(){
        return board;
    }

    /**
     * Weist dieser Zelle ein Schiff zu.
     *
     * @param ships Das {@link Ships}-Objekt, das auf dieser Zelle platziert wird.
     */
    public void setShip(Ships ships){
        ship = ships;
        System.out.println("[Cell] auf "+x+"und "+y+" liegt ein schiff");
    }

    /**
     * Gibt das Schiff zurück, das sich auf dieser Zelle befindet.
     *
     * @return Das {@link Ships}-Objekt oder null, wenn kein Schiff vorhanden ist.
     */
    public Ships getShip(){
        return ship;
    }

    /**
     * Setzt den Status, ob ein Schiff auf dieser Zelle getroffen wurde (für Multiplayer).
     *
     * @param hit true, wenn ein Treffer registriert wurde, sonst false.
     */
    public void setShipHit(boolean hit){
        shipHit = hit;
    }

    /**
     * Gibt zurück, ob ein Schiff auf dieser Zelle getroffen wurde.
     *
     * @return true, wenn ein Treffer registriert wurde, sonst false.
     */
    public boolean getShipHit(){
        return shipHit;
    }

    /**
     * Setzt den "beschossen"-Status dieser Zelle.
     * Ändert auch die Füllfarbe entsprechend, um das Ergebnis visuell darzustellen.
     *
     * @param s true, wenn die Zelle als beschossen markiert werden soll, sonst false.
     */
    public void setShot(boolean s){
        this.shot = s;
        if (s) {
            setFill(ship != null ? Color.RED : Color.LIGHTGREEN);
        }
    }

    /**
     * Prüft, ob auf diese Zelle bereits geschossen wurde.
     *
     * @return true, wenn die Zelle beschossen wurde, sonst false.
     */
    public boolean isShot(){
        return shot;
    }
}