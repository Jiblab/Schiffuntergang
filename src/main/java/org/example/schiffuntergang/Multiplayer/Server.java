package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Repräsentiert die Server-Seite der Netzwerkkommunikation für ein Multiplayer-Spiel.
 * Diese Klasse ist verantwortlich für das Öffnen eines Server-Sockets, das Warten auf
 * eine Client-Verbindung und das Senden und Empfangen von Spiel-Nachrichten
 * gemäß dem definierten Protokoll.
 */
public class Server implements org.example.schiffuntergang.Multiplayer.Network {
    private ServerSocket s;
    private Socket cl;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Standardkonstruktor für die Server-Klasse.
     */
    public Server(){

    }

    /**
     * Startet den Server, öffnet einen Port und wartet auf die Verbindung eines Clients.
     * Nach erfolgreicher Verbindung werden die Input- und Output-Streams initialisiert.
     * Diese Methode blockiert, bis ein Client sich verbunden hat.
     *
     * @param port Der Port, auf dem der Server lauschen soll.
     * @throws IOException wenn beim Öffnen des Sockets ein Fehler auftritt.
     */
    public void start(int port) throws IOException {
        s = new ServerSocket(port);
        System.out.println("[Server] Warte auf Verbindung...");
        cl = s.accept();
        System.out.println("[Server] Client verbunden.");
        in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
        out = new PrintWriter(cl.getOutputStream(), true);
    }

    /**
     * Sendet die Dimensionen des Spielfelds an den Client.
     * @param rows Die Anzahl der Reihen (Höhe).
     * @param cols Die Anzahl der Spalten (Breite).
     */
    public void sendSize(int rows, int cols) {
        out.write("size " +rows+ " " +cols+" \n");
        out.flush();
    }

    /**
     * Sendet die Längen der platzierten Schiffe an den Client.
     * @param lengths Ein Array von Integern, das die Längen der einzelnen Schiffe enthält.
     */
    public void sendShips(int[] lengths) {
        out.write("ships");
        out.flush();
        for (int l : lengths) {
            out.write(" " + l);
            out.flush();
        }
        out.println();
    }

    /**
     * Sendet die Koordinaten eines Schusses an den Client.
     * @param row Die Reihe (Y-Koordinate) des Schusses.
     * @param col Die Spalte (X-Koordinate) des Schusses.
     */
    public void sendShot(int row, int col) {
        out.write("shot " + row + " " + col+"\n");
        out.flush();
    }

    /**
     * Sendet das Ergebnis eines gegnerischen Schusses zurück an den Client.
     * @param result Der Ergebnis-Code (0=Fehlschuss, 1=Treffer, 2=Versenkt).
     */
    public void sendAnswer(int result) {
        out.write("answer " + result+"\n");
        out.flush();
    }

    /**
     * Sendet eine "pass"-Nachricht, um den Zug an den Client zu übergeben.
     */
    public void sendPass() {
        out.write("pass\n");
        out.flush();
    }

    /**
     * Sendet eine "ready"-Nachricht, um zu signalisieren, dass das Spiel beginnt.
     */
    public void sendReady() {
        out.write("ready\n");
        out.flush();
    }

    /**
     * Sendet eine "done"-Nachricht, um eine vorherige Aktion zu bestätigen.
     */
    public void sendDone() {
        out.write("done\n");
        out.flush();
    }

    /**
     * Sendet einen Befehl zum Speichern des Spiels an den Client.
     * @param id Die einzigartige ID des Spielstands.
     */
    public void sendSave(long id) {
        out.write("save " + id+"\n");
        out.flush();
    }

    /**
     * Sendet einen Befehl zum Laden eines Spiels an den Client.
     * @param id Die einzigartige ID des zu ladenden Spielstands.
     */
    public void sendLoad(long id) {
        out.write("load " + id+"\n");
        out.flush();
    }

    /**
     * Schließt die Client- und Server-Sockets und beendet die Verbindung.
     */
    public void close() {
        try {
            cl.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sendet eine generische Nachricht an den Client.
     * @param message Die zu sendende Nachricht als String.
     */
    public void send(String message){
        out.println(message+"\n");
        out.flush();
    }

    /**
     * Empfängt eine Nachricht vom Client.
     * Diese Methode blockiert, bis eine vollständige Zeile vom Client empfangen wurde.
     *
     * @return Die vom Client empfangene Nachricht als String.
     * @throws IOException wenn die Verbindung während des Lesens unterbrochen wird.
     */
    public String receiveMessage() throws IOException {
        return in.readLine();
    }
}