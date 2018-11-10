package uk.ac.standrews.cs.mamoc_client.WebSocket;

public interface WebSocket {

    void connect();

    void send(String text);

    void send(byte[] binary);

    void end();
}
