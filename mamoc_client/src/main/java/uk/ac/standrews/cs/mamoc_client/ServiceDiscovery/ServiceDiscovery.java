package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.TreeSet;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.CloseDetails;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import java8.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

import static uk.ac.standrews.cs.mamoc_client.Constants.CLOUD_IP;
import static uk.ac.standrews.cs.mamoc_client.Constants.CLOUD_REALM_NAME;
import static uk.ac.standrews.cs.mamoc_client.Constants.EDGE_IP;
import static uk.ac.standrews.cs.mamoc_client.Constants.EDGE_REALM_NAME;
import static uk.ac.standrews.cs.mamoc_client.Constants.SERVICE_DISCOVERY_BROADCASTER;

public class ServiceDiscovery {
    private final String TAG = "ServiceDiscovery";

    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static ServiceDiscovery instance;

    private TreeSet<MobileNode> mobileDevices = new TreeSet<>();
    private TreeSet<EdgeNode> edgeDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private MobileNode mobile;
    private EdgeNode edge;
    private CloudNode cloud;

    private boolean isConnectionListenerRunning = false;

    private ServiceDiscovery(Context context) {
        this.mContext = context;
        myPort = Utils.getPort(mContext);
        connListener = new ConnectionListener(mContext, myPort);
    }

    public static ServiceDiscovery getInstance(Context context) {
        if (instance == null) {
            synchronized (ServiceDiscovery.class) {
                if (instance == null) {
                    instance = new ServiceDiscovery(context);
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

    public void startConnectionListener(int port) {
        myPort = port;
        startConnectionListener();
    }

    public void restartConnectionListenerWith(int port) {
        stopConnectionListener();
        startConnectionListener(port);
    }

    public int getMyPort() {
        return myPort;
    }

    public boolean isConnectionListenerRunning() {
        return isConnectionListenerRunning;
    }

    public TreeSet<MobileNode> listMobileNodes() {
        return mobileDevices;
    }

    public void addMobileDevice(MobileNode mobileNode) {
        this.mobileDevices.add(mobileNode);
    }

    public void removeMobileDevice(MobileNode mobileNode){
        this.mobileDevices.remove(mobileNode);
    }

    public TreeSet<EdgeNode> listEdgeNodes() {
        return edgeDevices;
    }

    public void addEdgeDevice(EdgeNode edge) {
        this.edgeDevices.add(edge);
    }

    private void removeEdgeDevice(EdgeNode edge) {
        this.edgeDevices.remove(edge);
    }

    public TreeSet<CloudNode> listPublicNodes() {
        return cloudDevices;
    }

    public void addCloudDevices(CloudNode cloud) { this.cloudDevices.add(cloud); }

    private void removeCloudDevice(CloudNode cloud) {
        this.cloudDevices.remove(cloud);
    }

    void connectEdge(String wsUri){
        edge = new EdgeNode(EDGE_IP, 8080);

        EDGE_IP = wsUri;

        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
//            wsUri = "ws://" + wsUri + ":8080/ws";
            wsUri = "wss://" + wsUri; // That is already appended for the student server
        }

        // Add all onJoin listeners
        edge.session.addOnConnectListener(this::onConnectCallbackEdge);
        edge.session.addOnLeaveListener(this::onLeaveCallbackEdge);
        edge.session.addOnDisconnectListener(this::onDisconnectCallbackEdge);

        Client client = new Client(edge.session, wsUri, EDGE_REALM_NAME);
        CompletableFuture<ExitInfo> exitInfoCompletableFuture = client.connect();
    }

    void connectCloud(String wsUri){
        cloud = new CloudNode(CLOUD_IP, 8080);

        CLOUD_IP = wsUri;

        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
            wsUri = "ws://" + wsUri + ":8080/ws";
        }

        // Add all onJoin listeners
        cloud.session.addOnConnectListener(this::onConnectCallbackCloud);
        cloud.session.addOnLeaveListener(this::onLeaveCallbackCloud);
        cloud.session.addOnDisconnectListener(this::onDisconnectCallbackCloud);

        Client client = new Client(cloud.session, wsUri, CLOUD_REALM_NAME);
        CompletableFuture<ExitInfo> exitInfoCompletableFuture = client.connect();
    }

    private void onConnectCallbackEdge(Session session) {
        Log.d(TAG, "Session connected, ID=" + session.getID());
        addEdgeDevice(edge);
        Utils.save(mContext,"edgeIP", EDGE_IP);

        Log.d(TAG, "Broadcasting Edge Connect");
        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("connected");
        intent.putExtra("node", "edge");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onLeaveCallbackEdge(Session session, CloseDetails detail) {
        Log.d(TAG, String.format("Left reason=%s, message=%s", detail.reason, detail.message));
        removeEdgeDevice(edge);

        Log.d(TAG, "Broadcasting Edge Leave");
        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("disconnected");
        intent.putExtra("node", "edge");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onDisconnectCallbackEdge(Session session, boolean wasClean) {
        Log.d(TAG, String.format("Session with ID=%s, disconnected.", session.getID()));
        removeEdgeDevice(edge);

        Log.d(TAG, "Broadcasting Edge Disconnect");
        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("disconnected");
        intent.putExtra("node", "edge");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onConnectCallbackCloud(Session session) {
        Log.d(TAG, "Session connected, ID=" + session.getID());
        addCloudDevices(cloud);
        Log.d(TAG, "serviceDiscovery added " + CLOUD_IP);
        Utils.save(mContext, "cloudIP", CLOUD_IP);

        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("connected");
        intent.putExtra("node", "cloud");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onLeaveCallbackCloud(Session session, CloseDetails detail) {
        Log.d(TAG, String.format("Left reason=%s, message=%s", detail.reason, detail.message));
        removeCloudDevice(cloud);

        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("disconnected");
        intent.putExtra("node", "cloud");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onDisconnectCallbackCloud(Session session, boolean wasClean) {
        Log.d(TAG, String.format("Session with ID=%s, disconnected.", session.getID()));
        removeCloudDevice(cloud);

        Intent intent = new Intent(SERVICE_DISCOVERY_BROADCASTER);
        intent.setAction("disconnected");
        intent.putExtra("node", "cloud");
        intent.setClass(mContext, DiscoveryActivity.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    boolean isEdgeConnected(){
        return edge != null && edge.session.isConnected();
    }

    boolean isCloudConnected(){
        return cloud != null && cloud.session.isConnected();
    }

}
