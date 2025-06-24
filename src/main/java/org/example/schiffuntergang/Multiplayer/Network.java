package org.example.schiffuntergang.Multiplayer;

public interface Network {
    void sendShot(int row, int col);
    void sendAnswer(int result);
    void sendPass();
    void sendReady();
    void sendDone();
    void sendSave(long id);
    void sendLoad(long id);
    void sendSize(int rows, int cols);
    void sendShips(int[] lengths);
    void close();
}
