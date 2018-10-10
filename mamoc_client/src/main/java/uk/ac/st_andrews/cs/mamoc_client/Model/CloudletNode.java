package uk.ac.st_andrews.cs.mamoc_client.Model;

import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.interfaces.IWebSocketConnectionHandler;
import uk.ac.st_andrews.cs.mamoc_client.WebSocket.WebSocket;

public class CloudletNode extends MamocNode implements WebSocket, Comparable<MamocNode> {

    private String wsUri;
    public WebSocketConnection cloudletConnection = new WebSocketConnection();

    public CloudletNode(String url, int i) {
        this.wsUri = url;
        connect();
    }

    @Override
    public void connect() {
        try {
            connectToWebSocket(wsUri, new WebSocketConnectionHandler());
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void connectToWebSocket(String wsUri, IWebSocketConnectionHandler wsHandler) throws WebSocketException {
        cloudletConnection.connect(wsUri, wsHandler);
    }


    @Override
    public void send(String text) {
        cloudletConnection.sendMessage(text);
    }

    @Override
    public void send(byte[] binary) {
        cloudletConnection.sendMessage(binary, true);
    }

    @Override
    public void end() {
        cloudletConnection.sendClose();
    }

    @Override
    public int compareTo(MamocNode o) {
        if (this.getCpuFreq() > o.getCpuFreq()) {
            return 1;
        } else {
            return -1;
        }
    }
}
