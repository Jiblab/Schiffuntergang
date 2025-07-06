package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Repräsentiert die Client-Seite der Netzwerkkommunikation für ein Multiplayer-Spiel.
 * Diese Klasse ist verantwortlich für die Verbindung zu einem Server und das Senden
 * und Empfangen von Spiel-Nachrichten gemäß dem im {@link Network}-Interface
 * definierten Protokoll.
 */
public class Client implements Network {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Stellt eine Verbindung zu einem Server unter der angegebenen IP-Adresse und Port her.
     * Nach erfolgreicher Verbindung werden die Input- und Output-Streams initialisiert.
     *
     * @param ip   Die IP-Adresse des Servers, zu dem eine Verbindung hergestellt werden soll.
     * @param port Der Port des Servers.
     * @throws IOException wenn die Verbindung fehlschlägt oder ein Fehler beim Einrichten der Streams auftritt.
     */
    public void connect(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        System.out.println("[Client] Verbunden mit Server.");
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /**
     * Sendet die Dimensionen des Spielfelds an den Server.
     * Hinweis: Normalerweise sendet der Server die Größe, diese Methode ist für ein symmetrisches Interface.
     * @param rows Die Anzahl der Reihen (Höhe).
     * @param cols Die Anzahl der Spalten (Breite).
     */
    public void sendSize(int rows, int cols) {
        out.println("[Client] size " + rows);
    }

    /**
     * Sendet die Längen der platzierten Schiffe an den Server.
     * @param lengths Ein Array von Integern, das die Längen der einzelnen Schiffe enthält.
     */
    public void sendShips(int[] lengths) {
        out.write("ships\n");
        out.flush();

        for (int l : lengths) {
            out.write(" " + l);
            out.flush();
        }
        out.println();
    }

    /**
     * Sendet die Koordinaten eines Schusses an den Server.
     * @param row Die Reihe (Y-Koordinate) des Schusses.
     * @param col Die Spalte (X-Koordinate) des Schusses.
     */
    public void sendShot(int row, int col) {
        out.write("shot " + row + " " + col+"\n");
        out.flush();
    }

    /**
     * Sendet das Ergebnis eines gegnerischen Schusses zurück an den Server.
     * @param result Der Ergebnis-Code (0=Fehlschuss, 1=Treffer, 2=Versenkt).
     */
    public void sendAnswer(int result) {
        out.write("answer " +result+"\n");
        out.flush();
    }

    /**
     * Sendet eine "pass"-Nachricht, um den Zug an den Server zu übergeben.
     */
    public void sendPass() {
        out.write("pass\n");
        out.flush();
    }

    /**
     * Sendet eine "ready"-Nachricht, um zu signalisieren, dass man bereit für den Spielstart ist.
     */
    public void sendReady() {
        out.write("ready\n");
        out.flush();
    }

    /**
     * Sendet eine "done"-Nachricht, um eine vorherige Aktion zu bestätigen
     * (z.B. das Empfangen der Schiffsdaten).
     */
    public void sendDone() {
        out.write("done\n");
        out.flush();
    }

    /**
     * Sendet einen Befehl zum Speichern des Spiels an den Server.
     * @param id Die einzigartige ID des zu erstellenden Spielstands.
     */
    public void sendSave(long id) {
        out.write("save " + id+"\n");
        out.flush();
    }

    /**
     * Sendet einen Befehl zum Laden eines Spiels an den Server.
     * @param id Die einzigartige ID des zu ladenden Spielstands.
     */
    public void sendLoad(long id) {
        out.write("load " + id+"\n");
        out.flush();
    }

    /**
     * Sendet eine generische Nachricht an den Server.
     * @param message Die zu sendende Nachricht als String.
     */
    public void send(String message){
        out.println(message+"\n");
        out.flush();
    }

    /**
     * Schließt den Client-Socket und beendet die Verbindung zum Server.
     */
    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Empfängt eine Nachricht vom Server.
     * Diese Methode blockiert, bis eine vollständige Zeile vom Server empfangen wurde.
     *
     * @return Die vom Server empfangene Nachricht als String.
     * @throws IOException wenn die Verbindung während des Lesens unterbrochen wird.
     */
    public String receiveMessage() throws IOException {
        return in.readLine();
    }
}