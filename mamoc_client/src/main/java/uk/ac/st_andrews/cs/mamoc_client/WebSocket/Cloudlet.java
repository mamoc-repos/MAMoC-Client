package uk.ac.st_andrews.cs.mamoc_client.WebSocket;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.interfaces.IWebSocket;
import io.crossbar.autobahn.websocket.interfaces.IWebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.types.WebSocketOptions;

/*public class Cloudlet implements WebSocket {
    private final String TAG = "Web Socket Impl";
    private final List<Payload> subscriptions = new ArrayList<>();
    private Handler mainHandler = new Handler();
    private WebSocketConnection cloudletConnection = new WebSocketConnection();
    private String serverUrl;
    private Runnable handleSocketReconnection = new Runnable() {
        @Override
        public void run() {
            try {
                if (cloudletConnection != null && !cloudletConnection.isConnected())
                    connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void changeSocketURI(String host, String port) throws Exception {

        if (serverUrl != null && !serverUrl.isEmpty()) {

            if (cloudletConnection != null && cloudletConnection.isConnected()) {
                end();
            }
            connect();
        }
    }

    @Override
    public WebSocket connect() throws Exception {
        if(serverUrl == null || !serverUrl.startsWith("ws")){
            throw new Exception("Right server url is not provided");
        }

        cloudletConnection.connect(serverUrl, new WebSocketConnectionHandler() {
            @Override
            public void onOpen() {
                Log.i(TAG,"Connected");
            }
            @Override
            public void onClose(int i, String s) {
                //force recnnection to web socket
                Log.e(TAG, "Disconnected; Code " + i);

                if (i == 1 || i == 3 || i == 2 || i == 4 || i == 5) {
                    mainHandler.removeCallbacks(handleSocketReconnection);
                    mainHandler.postDelayed(handleSocketReconnection, 15000);
                }
            }
        });
        return this;
    }

    @Override
    public void send(String text) {
        if(cloudletConnection.isConnected()) {
            Log.v("sending:", text);
            cloudletConnection.sendMessage(text);
        } else {
            Log.v("socket", String.valueOf(cloudletConnection.isConnected()));
        }
    }

    @Override
    public void send(byte[] binary) {
        if(cloudletConnection.isConnected())
            cloudletConnection.sendMessage(binary, true);
    }

    @Override
    public void end() {
        cloudletConnection.sendClose();
    }

    public boolean isConnected(){
        return cloudletConnection.isConnected();
    }

    final private class Payload<T>{
        private String channel;
        private Class<T> objectType;
        private WsListner listner;

        Payload(String channel, Class<T> objectType, WsListner listner) {
            this.channel = channel;
            this.objectType = objectType;
            this.listner = listner;
        }
    }
}*/

public class Cloudlet extends WebSocketConnection {

    String serverUrl = null;
    int port;

    public Cloudlet(String websocketServerUri, int port) {
        this.serverUrl = websocketServerUri;
        this.port = port;
    }

    @Override
    public void connect(String wsUri, IWebSocketConnectionHandler wsHandler) throws WebSocketException {
        super.connect(wsUri, wsHandler);
    }

    public boolean send(String message){
        if (this.isConnected()) {
            super.sendMessage(message);
            return true;
        } else{
            return false;
        }
    }
}
