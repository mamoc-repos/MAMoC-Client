package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java8.util.concurrent.CompletableFuture;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.CloseDetails;
import io.crossbar.autobahn.wamp.types.ExitInfo;

import uk.ac.standrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.standrews.cs.mamoc_client.Constants;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.R;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

import static uk.ac.standrews.cs.mamoc_client.Constants.CLOUD_IP;
import static uk.ac.standrews.cs.mamoc_client.Constants.CLOUD_REALM_NAME;
import static uk.ac.standrews.cs.mamoc_client.Constants.EDGE_REALM_NAME;
import static uk.ac.standrews.cs.mamoc_client.Constants.REQUEST_CODE_ASK_PERMISSIONS;
import static uk.ac.standrews.cs.mamoc_client.Constants.EDGE_IP;

public class DiscoveryActivity extends AppCompatActivity {

    private final String TAG = "DiscoveryActivity";
    private MamocFramework framework;
    EdgeNode edge;
    CloudNode cloud;

    private Button discoverButton, edgeBtn, cloudBtn;

    private TextView listeningPort, edgeTextView, cloudTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarDiscovery);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        framework = MamocFramework.getInstance(this);
        framework.commController = CommunicationController.getInstance(this);
        framework.commController.startConnectionListener();

        listeningPort = findViewById(R.id.ListenPort);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(View -> startNSD());

        edgeBtn = findViewById(R.id.edgeConnect);
        edgeTextView = findViewById(R.id.edgeTextView);

        cloudBtn = findViewById(R.id.cloudConnect);
        cloudTextView = findViewById(R.id.cloudTextView);

        edgeBtn.setOnClickListener(view -> connectEdge());
        cloudBtn.setOnClickListener(view -> connectCloud());

        checkWritePermissions();
        logInterfaces();
        loadPrefs();
    }

    private void loadPrefs() {
        String enteredEdgeIP = Utils.getValue(this, "edgeIP");
        if (enteredEdgeIP != null) {
            edgeTextView.setText(enteredEdgeIP);
        } else {
            edgeTextView.setText(EDGE_IP);
        }

        String enteredCloudIP = Utils.getValue(this, "cloudIP");
        if (enteredCloudIP != null) {
            cloudTextView.setText(enteredCloudIP);
        } else {
            cloudTextView.setText(CLOUD_IP);
        }
    }

    private void savePrefs(String key, String value) {
        Utils.save(this, key, value);
    }

    private void connectEdge() {

        edge = new EdgeNode(EDGE_IP, 8080);

        String wsUri = edgeTextView.getText().toString();

        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
            wsUri = "ws://" + wsUri + ":8080/ws";
        }

        // Add all onJoin listeners
        edge.session.addOnConnectListener(this::onConnectCallbackEdge);
        edge.session.addOnLeaveListener(this::onLeaveCallbackEdge);
        edge.session.addOnDisconnectListener(this::onDisconnectCallbackEdge);

        Client client = new Client(edge.session, wsUri, EDGE_REALM_NAME);
        CompletableFuture<ExitInfo> exitInfoCompletableFuture = client.connect();
    }

    private void onConnectCallbackEdge(Session session) {
        Log.d(TAG, "Session connected, ID=" + session.getID());
        Utils.alert(DiscoveryActivity.this, "Connected.");
        edgeBtn.setText("Status: Connected to " + EDGE_IP);
        edgeBtn.setEnabled(false);
        framework.commController.addEdgeDevice(edge);
        savePrefs("edgeIP", EDGE_IP);
    }

    private void onLeaveCallbackEdge(Session session, CloseDetails detail) {
        Log.d(TAG, String.format("Left reason=%s, message=%s", detail.reason, detail.message));
        Utils.alert(DiscoveryActivity.this, "Left.");
        edgeBtn.setEnabled(true);
    }

    private void onDisconnectCallbackEdge(Session session, boolean wasClean) {
        Log.d(TAG, String.format("Session with ID=%s, disconnected.", session.getID()));
        Utils.alert(DiscoveryActivity.this, "Disconnected.");
        framework.commController.removeEdgeDevice(edge);
        edgeBtn.setEnabled(true);
    }

    private void connectCloud() {

        cloud = new CloudNode(CLOUD_IP, 8080);

        String wsUri = cloudTextView.getText().toString();

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

    private void onConnectCallbackCloud(Session session) {
        Log.d(TAG, "Session connected, ID=" + session.getID());
        Utils.alert(DiscoveryActivity.this, "Connected.");
        cloudBtn.setText("Status: Connected to " + CLOUD_IP);
        cloudBtn.setEnabled(false);
        framework.commController.addCloudDevices(cloud);
        Log.d(TAG, "commController added " + CLOUD_IP);
        savePrefs("cloudIP", CLOUD_IP);
    }

    private void onLeaveCallbackCloud(Session session, CloseDetails detail) {
        Log.d(TAG, String.format("Left reason=%s, message=%s", detail.reason, detail.message));
        Utils.alert(DiscoveryActivity.this, "Left.");
        cloudBtn.setEnabled(true);
    }

    private void onDisconnectCallbackCloud(Session session, boolean wasClean) {
        Log.d(TAG, String.format("Session with ID=%s, disconnected.", session.getID()));
        Utils.alert(DiscoveryActivity.this, "Disconnected.");
        framework.commController.removeCloudDevice(cloud);
        cloudBtn.setEnabled(true);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onResume() {
        super.onResume();
        listeningPort.setText(String.format(getString(R.string.port_info), Utils.getPort(this)));

        if (edge != null && edge.session.isConnected()){
            edgeBtn.setText("Status: Connected to " + EDGE_IP);
            edgeBtn.setEnabled(false);
        } else{
            edgeBtn.setEnabled(true);
        }

        if (cloud != null && cloud.session.isConnected()){
            cloudBtn.setText("Status: Connected to " + CLOUD_IP);
            cloudBtn.setEnabled(false);
        } else{
            cloudBtn.setEnabled(true);
        }
    }

    private void checkWritePermissions() {

        boolean isGranted = Utils.checkPermission(Constants.WRITE_PERMISSION, this);
        if (!isGranted) {
            Utils.requestPermission(Constants.WRITE_PERMISSION, Constants
                    .WRITE_PERM_REQ_CODE, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            final int numOfRequest = grantResults.length;
            final boolean isGranted = numOfRequest == 1
                    && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
            if (isGranted) {
                // you are good to go
            } else {
                Toast.makeText(this, "Please allow all the needed permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void logInterfaces(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface ni: Collections.list(interfaces)
                 ) {
                // Only display the up and running network interfaces
                if (ni.isUp()) {
                    Log.v(TAG, "Display name: " + ni.getDisplayName());
                    Log.v(TAG, "name: " + ni.getName());
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    for (InetAddress singleNI : Collections.list(addresses)
                            ) {
                        Log.v(TAG, "inet address: " + singleNI.getHostAddress());
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void startNSD(){
        if (Utils.isWifiConnected(this)){
            Intent nsdIntent = new Intent(DiscoveryActivity.this, NSD_Activity.class);
            startActivity(nsdIntent);
            finish();
        } else {
            Toast.makeText(this, "Wifi not connected! :(", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
