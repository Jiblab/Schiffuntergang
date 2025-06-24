package org.example.schiffuntergang.components;

/**
 * Repräsentiert eine einfache Koordinate (x, y) auf dem Spielfeld.
 * Wird verwendet für Schiffspositionen, Trefferpunkte, etc.
 */
public class Position {

    private int x;
    private int y;

    /**
     * Konstruktor zur Initialisierung einer Position mit x- und y-Wert.
     *
     * @param x horizontale Koordinate
     * @param y vertikale Koordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getter
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Setter
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
