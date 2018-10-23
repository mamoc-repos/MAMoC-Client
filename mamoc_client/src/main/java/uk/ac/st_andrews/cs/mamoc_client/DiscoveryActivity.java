package uk.ac.st_andrews.cs.mamoc_client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.types.WebSocketOptions;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.PHONE_ACCESS_PERM_REQ_CODE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.REQUEST_CODE_ASK_PERMISSIONS;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.REQUEST_READ_PHONE_STATE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.WRITE_PERMISSION;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.WRITE_PERM_REQ_CODE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.EDGE_IP;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.REALM_NAME;

public class DiscoveryActivity extends AppCompatActivity {

    private final String TAG = "DiscoveryActivity";

    CommunicationController controller;
    EdgeNode edge;

    private Button discoverButton, edgeBtn, cloudBtn;

    private TextView listeningPort, edgeTextView;

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

        controller = CommunicationController.getInstance(this);
        controller.startConnectionListener();

        listeningPort = findViewById(R.id.ListenPort);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(View -> startNSD());

        edgeBtn = findViewById(R.id.edgeConnect);
        edgeTextView = findViewById(R.id.edgeTextView);
        cloudBtn = findViewById(R.id.cloudConnect);

        edgeBtn.setOnClickListener(view -> connectEdge());
        cloudBtn.setOnClickListener(view -> connectCloud());

        checkWritePermissions();
        logInterfaces();
        loadPrefs();
    }

    private void loadPrefs() {
        String enteredIP = Utils.getValue(this, "edgeIP");
        if (enteredIP != null) {
            edgeTextView.setText(enteredIP);
        } else {
            edgeTextView.setText(EDGE_IP);
        }
    }

    private void savePrefs(String key, String value) {
        Utils.save(this, key, value);
    }

    private void connectCloud() {
        edge.send("{\"TextSearch\":\"hi\", \"start\":0, \"end\":0}");
    }

    private void connectEdge() {

        edge = new EdgeNode(EDGE_IP, 8080);

        final String wsUri = edgeTextView.getText().toString();

//        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
//            wsUri = "ws://" + wsUri;
//        }

        // Add all onJoin listeners
        edge.session.addOnConnectListener(this::onConnectCallback);
        edge.session.addOnLeaveListener(this::onLeaveCallback);
        edge.session.addOnDisconnectListener(this::onDisconnectCallback);

        Client client = new Client(edge.session, EDGE_IP, REALM_NAME);
        CompletableFuture<ExitInfo> exitInfoCompletableFuture = client.connect();

//        WebSocketOptions connectOptions = new WebSocketOptions();
//        connectOptions.setReconnectInterval(5000);
//
//        try {
//            edge.edgeConnection.connect(wsUri, new WebSocketConnectionHandler(){
//            @Override
//            public void onOpen() {
//                Utils.alert(DiscoveryActivity.this, "Connected.");
//                edgeBtn.setText("Status: Connected to " + wsUri);
//                edgeBtn.setEnabled(false);
//                controller.addEdgeDevice(edge);
//                edge.send("hello");
//                savePrefs("edgeIP", wsUri);
//                Log.d("connection: ", String.valueOf(edge.edgeConnection));
//            }
//
//            @Override
//            public void onMessage(String payload) {
//                Utils.alert(DiscoveryActivity.this, "Got echo: " + payload);
//            }
//
//            @Override
//            public void onClose(int code, String reason) {
//                Utils.alert(DiscoveryActivity.this, "Connection lost.");
//                controller.removeEdgeDevice(edge);
//                loadPrefs();
//                edgeBtn.setEnabled(true);
//            }
//        }, connectOptions);
//    } catch (WebSocketException e) {
//        Log.d(TAG, e.toString());
//    }

     //   if (edge.isConnected()){
//            edgeBtn.setText("Connected to Edge!");
//            edgeBtn.setEnabled(false);
     //   }
//        client.send(new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF});
//        client.end();
    }

    private void onConnectCallback(Session session) {
        Log.d(TAG, "Session connected, ID=" + session.getID());
        Utils.alert(DiscoveryActivity.this, "Connected.");
        edgeBtn.setText("Status: Connected to " + EDGE_IP);
        edgeBtn.setEnabled(false);
        controller.addEdgeDevice(edge);
        savePrefs("edgeIP", EDGE_IP);
    }

    private void onLeaveCallback(Session session, CloseDetails detail) {
        Log.d(TAG, String.format("Left reason=%s, message=%s", detail.reason, detail.message));
        Utils.alert(DiscoveryActivity.this, "Left.");
        edgeBtn.setEnabled(true);
    }

    private void onDisconnectCallback(Session session, boolean wasClean) {
        Log.d(TAG, String.format("Session with ID=%s, disconnected.", session.getID()));
        Utils.alert(DiscoveryActivity.this, "Disconnected.");
        controller.removeEdgeDevice(edge);
        edgeBtn.setEnabled(true);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onResume() {
        super.onResume();
    //    DBAdapter.getInstance(DiscoveryActivity.this).clearDatabase();
        listeningPort.setText(String.format(getString(R.string.port_info), Utils.getPort(this)));
    }

    private void checkWritePermissions() {

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        boolean isWriteGranted = Utils.checkPermission(WRITE_PERMISSION, this);
        if (!isWriteGranted){
            Utils.requestPermission(WRITE_PERMISSION, WRITE_PERM_REQ_CODE, this);
        }
        boolean isStateGranted = Utils.checkPermission(REQUEST_READ_PHONE_STATE, this);
        if (!isStateGranted){
            Utils.requestPermission(REQUEST_READ_PHONE_STATE, PHONE_ACCESS_PERM_REQ_CODE, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    // you are good to go
                } else {
                    Toast.makeText(this, "Please allow all the needed permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
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
//        if (mConnection.isConnected()) {
//            mConnection.sendClose();
//        }
    }
}
