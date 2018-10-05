package uk.ac.st_andrews.cs.mamoc_client.WebSocket;

public interface WebSocket {

    WebSocket connect() throws Exception;

    void send(String text);

    void send(byte[] binary);

    void end();
}
