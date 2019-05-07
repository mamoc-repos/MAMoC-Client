package uk.ac.standrews.cs.mamoc_client.Model;

import android.util.Log;

import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.interfaces.IWebSocket;
import io.crossbar.autobahn.websocket.interfaces.IWebSocketConnectionHandler;
import uk.ac.standrews.cs.mamoc_client.WebSocket.WebSocket;

public class CloudNode extends MamocNode implements WebSocket, Comparable<MamocNode> {

    private String wsUri;
    private final IWebSocket cloudConnection = new WebSocketConnection();
    public Session session;

    public CloudNode(String url, int i) {

        super.setIp(url);
        super.setPort(i);
        super.setNodeName(url);

        this.wsUri = url;
        session = new Session();
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
        cloudConnection.connect(wsUri, wsHandler);
    }


    @Override
    public void send(String text) {
        if (cloudConnection.isConnected()) {
            cloudConnection.sendMessage(text);
        } else{
            Log.e("cloudConnection", String.valueOf(cloudConnection.isConnected()));
        }
    }

    @Override
    public void send(byte[] binary) {
        cloudConnection.sendMessage(binary, true);
    }

    @Override
    public void end() {
        cloudConnection.sendClose();
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
