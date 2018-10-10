package uk.ac.st_andrews.cs.mamoc_client.Communication;

import android.content.Context;

import java.util.TreeSet;

import uk.ac.st_andrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudletNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.NearbyNode;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

public class CommunicationController {
    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static CommunicationController instance;

    private TreeSet<NearbyNode> nearbyDevices = new TreeSet<>();
    private TreeSet<CloudletNode> cloudletDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private boolean isConnectionListenerRunning = false;

    public CommunicationController(Context context) {
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
            connListener = new ConnectionListener(mContext, myPort);
        }
        if (!connListener.isAlive()){
            connListener.interrupt();
            connListener.tearDown();
            connListener = null;
        }
        connListener = new ConnectionListener(mContext, myPort);
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

    public TreeSet<NearbyNode> getNearbyDevices() {
        return nearbyDevices;
    }

    public void setNearbyDevices(TreeSet<NearbyNode> nearbyDevices) {
        this.nearbyDevices = nearbyDevices;
    }

    public TreeSet<CloudletNode> getCloudletDevices() {
        return cloudletDevices;
    }

    public void addCloudletDevice(CloudletNode cloudlet) {
        this.cloudletDevices.add(cloudlet);
    }

    public void removeCloudletDevice(CloudletNode cloudlet){
        this.cloudletDevices.remove(cloudlet);
    }

    public TreeSet<CloudNode> getCloudDevices() {
        return cloudDevices;
    }

    public void setCloudDevices(TreeSet<CloudNode> cloudDevices) {
        this.cloudDevices = cloudDevices;
    }
}
