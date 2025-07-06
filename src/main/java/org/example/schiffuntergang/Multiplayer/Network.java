package org.example.schiffuntergang.Multiplayer;

/**
 * Definiert den Vertrag für die Netzwerkkommunikation im Spiel.
 * Jede Klasse, die für die Übertragung von Spieldaten zuständig ist (wie {@link Server} und {@link Client}),
 * muss dieses Interface implementieren. Es stellt sicher, dass alle notwendigen
 * Sendemethoden für das Spielprotokoll vorhanden sind.
 */
public interface Network {

    /**
     * Sendet die Koordinaten eines Schusses an den Gegner.
     *
     * @param row Die Reihe (Y-Koordinate) des Schusses.
     * @param col Die Spalte (X-Koordinate) des Schusses.
     */
    void sendShot(int row, int col);

    /**
     * Sendet die Antwort auf einen gegnerischen Schuss.
     *
     * @param result Der Ergebnis-Code (z.B. 0 für Wasser, 1 für Treffer, 2 für versenkt).
     */
    void sendAnswer(int result);

    /**
     * Sendet eine Nachricht, um den Spielzug an den Gegner abzugeben.
     */
    void sendPass();

    /**
     * Sendet eine Nachricht, um zu signalisieren, dass man bereit für den Spielstart ist.
     */
    void sendReady();

    /**
     * Sendet eine "fertig"-Nachricht, um eine vorherige Aktion zu bestätigen
     * (z.B. das Empfangen von Daten).
     */
    void sendDone();

    /**
     * Sendet einen Befehl zum Speichern des Spiels an den Gegner.
     *
     * @param id Die einzigartige ID des zu erstellenden Spielstands.
     */
    void sendSave(long id);

    /**
     * Sendet einen Befehl zum Laden eines Spiels an den Gegner.
     *
     * @param id Die einzigartige ID des zu ladenden Spielstands.
     */
    void sendLoad(long id);

    /**
     * Sendet die Dimensionen des Spielfelds an den Gegner.
     *
     * @param rows Die Anzahl der Reihen (Höhe).
     * @param cols Die Anzahl der Spalten (Breite).
     */
    void sendSize(int rows, int cols);

    /**
     * Sendet die Längen der platzierten Schiffe an den Gegner.
     *
     * @param lengths Ein Array von Integern, das die Längen der Schiffe enthält.
     */
    void sendShips(int[] lengths);

    /**
     * Schließt die Netzwerkverbindung und gibt die Ressourcen frei.
     */
    void close();
}