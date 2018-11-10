package uk.ac.standrews.cs.mamoc_client.Model;

import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.interfaces.IWebSocket;

public class CloudNode extends MamocNode implements Comparable<MamocNode> {

    private String wsUri;
    private final IWebSocket cloudConnection = new WebSocketConnection();
    public Session session;

    public CloudNode(String url, int i) {
        this.wsUri = url;
        session = new Session();
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
