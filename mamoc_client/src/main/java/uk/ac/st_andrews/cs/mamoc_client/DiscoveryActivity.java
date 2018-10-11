package uk.ac.st_andrews.cs.mamoc_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import java.util.concurrent.CompletableFuture;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.types.WebSocketOptions;
import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudletNode;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.PHONE_ACCESS_PERM_REQ_CODE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.REQUEST_READ_PHONE_STATE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.WRITE_PERMISSION;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.WRITE_PERM_REQ_CODE;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.cloudletIP;

public class DiscoveryActivity extends AppCompatActivity {

    private final String TAG = "DiscoveryActivity";

    CommunicationController controller;

    private Button discoverButton, cloudletBtn, cloudBtn;

    private TextView listeningPort, cloudletTextView;
    private Spinner cloudletSpinner;
    ArrayAdapter<String> cloudletSpinnerAdapter;

    CloudletNode cloudlet;

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

        checkWritePermissions();
        logInterfaces();

        controller = CommunicationController.getInstance(this);

        //new CommunicationController(this);
        controller.startConnectionListener();

        listeningPort = findViewById(R.id.ListenPort);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(View -> startNSD());

        cloudletBtn = findViewById(R.id.cloudletConnect);
        cloudletTextView = findViewById(R.id.cloudletTextView);
        cloudBtn = findViewById(R.id.cloudConnect);

        cloudletSpinner = findViewById(R.id.cloudletSpinner);
        cloudletSpinnerAdapter = (ArrayAdapter<String>)cloudletSpinner.getAdapter();

        loadPrefs();

        cloudletBtn.setOnClickListener(view -> connectCloudlet());
        cloudBtn.setOnClickListener(view -> connectCloud());

    }

    private void loadPrefs() {
        String cloudletIP = Utils.getValue(this, "cloudletIP");
        if (cloudletIP != null) {
            cloudletTextView.setText(cloudletIP);
            // TODO: fix this
//            cloudletSpinnerAdapter.add(cloudletIP);
        } else {
            cloudletTextView.setText(R.string.localhost);
        }
    }

    private void savePrefs(String key, String value) {
        Utils.save(this, key, value);
    }

    private void connectCloud() {
        cloudlet.send("{\"TextSearch\":\"hi\", \"start\":0, \"end\":0}");
    }

    private void connectCloudlet() {

        Log.v("cloudlet", cloudletIP + "/connect");

        cloudlet = new CloudletNode(cloudletIP, 8080);
        cloudlet.setCpuFreq(5);

        final String wsUri = cloudletTextView.getText().toString();
                //"ws://192.168.0.12:9000";
                //"ws://" + cloudletIP + "/connect";

//        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
//            wsUri = "ws://" + wsUri;
//        }

        // Create a session object
        // Add all onJoin listeners

        cloudlet.session.addOnConnectListener(this::onConnectCallback);

        Client client = new Client(cloudlet.session, "ws://104.248.167.173:8080/ws", "realm1");
        CompletableFuture<ExitInfo> exitInfoCompletableFuture = client.connect();

//        WebSocketOptions connectOptions = new WebSocketOptions();
//        connectOptions.setReconnectInterval(5000);
//
//        try {
//            cloudlet.cloudletConnection.connect(wsUri, new WebSocketConnectionHandler(){
//            @Override
//            public void onOpen() {
//                Utils.alert(DiscoveryActivity.this, "Connected.");
//                cloudletBtn.setText("Status: Connected to " + wsUri);
//                cloudletBtn.setEnabled(false);
//                controller.addCloudletDevice(cloudlet);
//                cloudlet.send("hello");
//                savePrefs("cloudletIP", wsUri);
//                Log.d("connection: ", String.valueOf(cloudlet.cloudletConnection));
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
//                controller.removeCloudletDevice(cloudlet);
//                loadPrefs();
//                cloudletBtn.setEnabled(true);
//            }
//        }, connectOptions);
//    } catch (WebSocketException e) {
//        Log.d(TAG, e.toString());
//    }

     //   if (cloudlet.isConnected()){
//            cloudletBtn.setText("Connected to Cloudlet!");
//            cloudletBtn.setEnabled(false);
     //   }
//        client.send(new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF});
//        client.end();
    }

    private void onConnectCallback(Session session) {
        Log.d("session", "Session connected, ID=" + session.getID());
        Utils.alert(DiscoveryActivity.this, "Connected.");
        cloudletBtn.setText("Status: Connected to " + "ws://104.248.167.173:8080/ws");
        cloudletBtn.setEnabled(false);
        controller.addCloudletDevice(cloudlet);
        cloudlet.send("hello");
        savePrefs("cloudletIP", "ws://104.248.167.173:8080/ws");
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onResume() {
        super.onResume();
        DBAdapter.getInstance(DiscoveryActivity.this).clearDatabase();
        listeningPort.setText(String.format(getString(R.string.port_info), Utils.getPort(this)));
    }

    private void checkWritePermissions() {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "Please allow all the needed permissions", Toast.LENGTH_SHORT).show();
            finish();
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
//                    Log.v(TAG, "is it running? " + String.valueOf(ni.isUp()));
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
