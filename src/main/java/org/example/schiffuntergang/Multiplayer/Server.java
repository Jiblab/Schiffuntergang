package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements org.example.schiffuntergang.Multiplayer.Network {
    private ServerSocket s;
    private Socket cl;
    private BufferedReader in;
    private PrintWriter out;

    public Server(){

    }
    public void start(int port) throws IOException {
        s = new ServerSocket(port);
        System.out.println("Warte auf Verbindung...");
        cl = s.accept();
        System.out.println("Client verbunden.");
        in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
        out = new PrintWriter(cl.getOutputStream(), true);
    }

    public void sendSize(int rows, int cols) {
        out.write("size " +rows+ " " +cols+" \n");
        out.flush();
    }

    public void sendShips(int[] lengths) {
        out.write("ships");
        out.flush();
        for (int l : lengths) {
            out.write(" " + l);
            out.flush();
        }
        out.println();
    }

    public void sendShot(int row, int col) {
        out.write("shot " + row + " " + col+"\n");
        out.flush();
    }

    public void sendAnswer(int result) {
        out.write("answer " + result+"\n");
        out.flush();
    }

    public void sendPass() {
        out.write("pass\n");
        out.flush();

    }

    public void sendReady() {
        out.write("ready\n");
        out.flush();

    }

    public void sendDone() {
        out.write("done\n");
        out.flush();

    }

    public void sendSave(long id) {
        out.write("save " + id+"\n");
        out.flush();

    }

    public void sendLoad(long id) {
        out.write("load " + id+"\n");
        out.flush();

    }

    public void close() {
        try {
            cl.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }
}
