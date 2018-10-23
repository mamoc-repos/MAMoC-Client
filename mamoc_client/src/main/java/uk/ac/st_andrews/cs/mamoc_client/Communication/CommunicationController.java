package uk.ac.st_andrews.cs.mamoc_client.Communication;

import android.content.Context;
import android.util.Log;

import org.atteo.classindex.ClassIndex;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Logger;

import uk.ac.st_andrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.Offloadable;
import uk.ac.st_andrews.cs.mamoc_client.Model.Remote;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

public class CommunicationController {
    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static CommunicationController instance;

    private TreeSet<MobileNode> mobileDevices = new TreeSet<>();
    private TreeSet<EdgeNode> edgeDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private boolean isConnectionListenerRunning = false;

    private ArrayList<Class> offloadableClasses = new ArrayList<>();

    private CommunicationController(Context context) {
        this.mContext = context;
        myPort = Utils.getPort(mContext);
        connListener = new ConnectionListener(mContext, myPort);
        findOffloadableClasses();
    }

    private void findOffloadableClasses() {
        Iterable<Class<?>> klasses = ClassIndex.getAnnotated(Offloadable.class);
        for (Class<?> klass: klasses) {
            offloadableClasses.add(klass);
            Log.d("annotation", "new annotated class found: " + klass.getName());
        }
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

    public TreeSet<MobileNode> getMobileDevices() {
        return mobileDevices;
    }

    public void setMobileDevices(TreeSet<MobileNode> mobileDevices) {
        this.mobileDevices = mobileDevices;
    }

    public TreeSet<EdgeNode> getEdgeDevices() {
        return edgeDevices;
    }

    public void addEdgeDevice(EdgeNode edge) {
        this.edgeDevices.add(edge);
    }

    public void removeEdgeDevice(EdgeNode edge){
        this.edgeDevices.remove(edge);
    }

    public TreeSet<CloudNode> getCloudDevices() {
        return cloudDevices;
    }

    public void setCloudDevices(TreeSet<CloudNode> cloudDevices) {
        this.cloudDevices = cloudDevices;
    }

    public ArrayList<Class> getOffloadableClasses() {
        return offloadableClasses;
    }

    public void runLocally(){

    }
}
