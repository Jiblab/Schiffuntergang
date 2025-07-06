package org.example.schiffuntergang.components;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;

/**
 * Repräsentiert ein einzelnes Schiff im Spiel.
 * Diese Klasse speichert die wesentlichen Eigenschaften eines Schiffs, wie seine Länge
 * und seinen Zustand (Gesundheit, Ausrichtung). Sie dient als Datenmodell für
 * die logischen und visuellen Schiffskomponenten auf dem Spielfeld.
 * Erweitert {@link Parent}, um potenziell als Container für visuelle Elemente zu dienen.
 */
public class Ships extends Parent {
    /** Die Länge des Schiffs, gemessen in der Anzahl der Zellen. */
    private final int length;
    /** Die aktuelle Gesundheit des Schiffs. Sinkt mit jedem Treffer. Wenn 0, ist das Schiff versenkt. */
    private int health;
    /** Die Ausrichtung des Schiffs. true für vertikal, false für horizontal. */
    private boolean vertical = true;
    /** Eine Referenz auf die visuelle Darstellung des Schiffs. */
    private ImageView shipImageView;

    /**
     * Erstellt ein neues Schiff mit einer bestimmten Länge und Gesundheit.
     *
     * @param l Die Länge des Schiffs.
     * @param h Die anfängliche Gesundheit des Schiffs (normalerweise identisch mit der Länge).
     */
    public Ships(int l, int h){
        this.length = l;
        this.health = h;
    }

    /**
     * Registriert einen Treffer auf dem Schiff.
     * Reduziert die Gesundheit um eins.
     */
    public void hit(){
        health--;
        if(health == 0){
            System.out.println("[Ships] Schiff versenkt");
        }
    }

    /**
     * Prüft, ob das Schiff noch schwimmt (nicht versenkt ist).
     *
     * @return true, wenn die Gesundheit größer als 0 ist, andernfalls false.
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Gibt die Länge des Schiffs zurück.
     *
     * @return Die Länge des Schiffs als Integer.
     */
    public int getLength(){
        return length;
    }

    /**
     * Gibt die Ausrichtung des Schiffs zurück.
     *
     * @return true, wenn das Schiff vertikal ausgerichtet ist, false bei horizontaler Ausrichtung.
     */
    public boolean getDirection(){
        return vertical;
    }

    /**
     * Legt die Ausrichtung des Schiffs fest.
     *
     * @param vertical true für eine vertikale Ausrichtung, false für eine horizontale.
     */
    public void setDirection(boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * Gibt die aktuelle Gesundheit des Schiffs zurück.
     *
     * @return Die verbleibende Gesundheit als Integer.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gibt die {@link ImageView}-Komponente zurück, die dieses Schiff visuell darstellt.
     *
     * @return Die ImageView des Schiffs.
     */
    public ImageView getShipImageView() {
        return shipImageView;
    }

    /**
     * Legt die {@link ImageView}-Komponente für die visuelle Darstellung dieses Schiffs fest.
     *
     * @param shipImageView Die ImageView, die das Schiff repräsentiert.
     */
    public void setShipImageView(ImageView shipImageView) {
        this.shipImageView = shipImageView;
    }
}