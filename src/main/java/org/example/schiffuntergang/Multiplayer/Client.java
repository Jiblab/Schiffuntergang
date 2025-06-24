package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client implements Network{
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
        out.print("ships");
        for (int l : lengths) {
            out.print(" " + l);
        }
        out.println();
    }

    public void sendShot(int row, int col) {
        out.println("shot " + row + " " + col);
    }

    public void sendAnswer(int result) {
        out.println("answer " + result);
    }

    public void sendPass() {
        out.println("pass");
    }

    public void sendReady() {
        out.println("ready");
    }

    public void sendDone() {
        out.println("done");
    }

    public void sendSave(long id) {
        out.println("save " + id);
    }

    public void sendLoad(long id) {
        out.println("load " + id);
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
