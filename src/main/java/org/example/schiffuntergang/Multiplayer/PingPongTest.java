package org.example.schiffuntergang.Multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class PingPongTest {
    static Server s;
    static Client c;
    static boolean client;
    static BufferedReader usr =
            new BufferedReader(new InputStreamReader(System.in));


    public static void main(String[] args) throws IOException {
        System.out.println("[PingPongTest] PingPongTest: Test\n");
        System.out.println("[PingPongTest] c für client, s für server: ");
        String methode = usr.readLine();
        if(methode.equals("c")) {
            client = true;
            c = new Client();
            System.out.println("[PingPongTest] PingPongTest: IP eingeben: ");
            String ip = usr.readLine();
            try {
                c.connect(ip, 5000);
                System.out.println("[PingPongTest] PingPongTest: Erfolgreich verbunden!");

            }catch(IOException e) {
                e.printStackTrace();
            }
        } else if (methode.equals("s")) {
            client = false;
            s = new Server();
            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[PingPongTest] PingPongTest: IP lautet: "+ip);
            s.start(5000);
        }
    }
}