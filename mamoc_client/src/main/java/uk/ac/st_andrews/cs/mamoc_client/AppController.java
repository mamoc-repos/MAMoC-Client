package uk.ac.st_andrews.cs.mamoc_client;

import android.app.Application;

import java.io.IOException;

import uk.ac.st_andrews.cs.mamoc_client.Communication.ConnectionListener;

public class AppController extends Application{
    private int myPort;
    private ConnectionListener connListener;

    private boolean isConnectionListenerRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        myPort = Utils.getPort(getApplicationContext());
        connListener = new ConnectionListener(getApplicationContext(), myPort);
    }

    public void stopConnectionListener(){
        if (!isConnectionListenerRunning){
            return;
        }
        if (connListener != null){
            connListener.tearDown();
            connListener = null;
        }
        isConnectionListenerRunning = false;
    }

    public void startConnectionListener() {
        if (isConnectionListenerRunning){
            return;
        }
        if (connListener == null){
            connListener = new ConnectionListener(getApplicationContext(), myPort);
        }
        if (!connListener.isAlive()){
            connListener.interrupt();
            connListener.tearDown();
            connListener = null;
        }
        connListener = new ConnectionListener(getApplicationContext(), myPort);
        connListener.start();
        isConnectionListenerRunning = true;
    }

    public void startConnectionListener(int port){
        myPort = port;
        startConnectionListener();
    }

    public void restartConnectionListenerWith(int port){
        stopConnectionListener();
        startConnectionListener(port);
    }

    public int getMyPort() {
        return myPort;
    }

    public boolean isConnectionListenerRunning() {
        return isConnectionListenerRunning;
    }
}
