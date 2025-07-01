package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Network {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;


    public void connect(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        System.out.println("Verbunden mit Server.");
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void sendSize(int rows, int cols) {
        out.println("size " + rows);
    }

    public void sendShips(int[] lengths) {
        out.write("ships\n");
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
        out.write("answer " +result+"\n");
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
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() throws IOException {
        return in.readLine();  // Nutze das in deinem GameLoop
    }
}
