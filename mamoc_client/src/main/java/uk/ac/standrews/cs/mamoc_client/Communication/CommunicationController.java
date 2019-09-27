package uk.ac.standrews.cs.mamoc_client.Communication;

import android.content.Context;

import java.util.TreeSet;

import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

public class CommunicationController {
    private final String TAG = "CommunicationController";

    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static CommunicationController instance;

    private TreeSet<MobileNode> mobileDevices = new TreeSet<>();
    private TreeSet<EdgeNode> edgeDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private boolean isConnectionListenerRunning = false;

    private CommunicationController(Context context) {
        this.mContext = context;
        myPort = Utils.getPort(mContext);
        connListener = new ConnectionListener(mContext, myPort);
    }

    public static CommunicationController getInstance(Context context) {
        if (instance == null) {
            synchronized (CommunicationController.class) {
                if (instance == null) {
                    instance = new CommunicationController(context);
                }
            }
        }
        return instance;
    }

    public void stopConnectionListener() {
        if (!isConnectionListenerRunning) {
            return;
        }
        if (connListener != null) {
            connListener.tearDown();
            connListener = null;
        }
        isConnectionListenerRunning = false;
    }

    public void startConnectionListener() {
        if (isConnectionListenerRunning) {
            return;
        }
        if (connListener == null) {
            connListener = new ConnectionListener(mContext, myPort);
        }
        if (!connListener.isAlive()) {
            connListener.interrupt();
            connListener.tearDown();
            connListener = null;
        }
        connListener = new ConnectionListener(mContext, myPort);
        connListener.start();
        isConnectionListenerRunning = true;
    }

    private void startConnectionListener(int port) {
        myPort = port;
        startConnectionListener();
    }

    public int getMyPort() {
        return myPort;
    }

    public boolean isConnectionListenerRunning() {
        return isConnectionListenerRunning;
    }

    public TreeSet<MobileNode> getMobileDevices() {
        return mobileDevices;
    }

    public void addMobileDevice(MobileNode mobileNode) {
        this.mobileDevices.add(mobileNode);
    }

    public void removeMobileDevice(MobileNode mobileNode){
        this.mobileDevices.remove(mobileNode);
    }

    public TreeSet<EdgeNode> getEdgeDevices() {
        return edgeDevices;
    }

    public void addEdgeDevice(EdgeNode edge) {
        this.edgeDevices.add(edge);
    }

    public void removeEdgeDevice(EdgeNode edge) {
        this.edgeDevices.remove(edge);
    }

    public TreeSet<CloudNode> getCloudDevices() {
        return cloudDevices;
    }

    public void addCloudDevices(CloudNode cloud) { this.cloudDevices.add(cloud); }

    public void removeCloudDevice(CloudNode cloud) {
        this.cloudDevices.remove(cloud);
    }

}
