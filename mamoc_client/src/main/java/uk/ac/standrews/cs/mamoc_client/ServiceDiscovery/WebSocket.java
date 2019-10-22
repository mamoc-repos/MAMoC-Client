package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

public interface WebSocket {

    void connect();

    void send(String text);

    void send(byte[] binary);

    void end();
}
