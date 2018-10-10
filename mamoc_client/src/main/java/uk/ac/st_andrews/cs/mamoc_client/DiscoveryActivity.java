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

import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudletNode;
import uk.ac.st_andrews.cs.mamoc_client.WebSocket.Cloudlet;

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

        controller = new CommunicationController(this);
        controller.startConnectionListener();

        listeningPort = findViewById(R.id.ListenPort);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNSD();
            }
        });

        cloudletBtn = findViewById(R.id.cloudletConnect);
        cloudletTextView = findViewById(R.id.cloudletTextView);
        cloudBtn = findViewById(R.id.cloudletConnect2);

        cloudletSpinner = findViewById(R.id.cloudletSpinner);
        cloudletSpinnerAdapter = (ArrayAdapter<String>)cloudletSpinner.getAdapter();

        loadPrefs();

        cloudletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //    cloudletIP = cloudletTextView.getText().toString();
                try {
                    connectCloudlet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        cloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectCloud();
            }
        });
        checkWritePermissions();
        logInterfaces();
    }

    private void loadPrefs() {
        String cloudletIP = Utils.getValue(this, "cloudletIP");
        if (Utils.getValue(this, cloudletIP) != null) {
            cloudletTextView.setText(cloudletIP);
            cloudletSpinnerAdapter.add(cloudletIP);
        } else {
            cloudletTextView.setText("localhost");
        }
    }

    private void connectCloud() {
        cloudlet.send("{\"TextSearch\":\"hi\", \"start\":0, \"end\":0}");
    }

    private void connectCloudlet() {

        Log.v("cloudlet", cloudletIP + "/connect");

        cloudlet = new CloudletNode(cloudletIP, 8080);
        cloudlet.setCpuFreq(5);

        final String wsUri = "ws://192.168.0.12:9000";
                //"ws://" + cloudletIP + "/connect";

//        if (!wsUri.startsWith("ws://") && !wsUri.startsWith("wss://")) {
//            wsUri = "ws://" + wsUri;
//        }

        try {
            cloudlet.connect();
            cloudlet.cloudletConnection.connect(wsUri, new WebSocketConnectionHandler(){
            @Override
            public void onOpen() {
                Utils.alert(DiscoveryActivity.this, "Connected.");
                cloudletBtn.setText("Status: Connected to " + wsUri);
                Utils.save(DiscoveryActivity.this, "cloudletIP", wsUri);
                cloudletBtn.setEnabled(false);
                controller.addCloudletDevices(cloudlet);
//                savePrefs();
//                mSendMessage.setEnabled(true);
//                mMessage.setEnabled(true);
            }

            @Override
            public void onMessage(String payload) {
                Utils.alert(DiscoveryActivity.this, "Got echo: " + payload);
            }

            @Override
            public void onClose(int code, String reason) {
                Utils.alert(DiscoveryActivity.this, "Connection lost.");
                cloudletBtn.setEnabled(true);
            }
        });
    } catch (WebSocketException e) {
        Log.d(TAG, e.toString());
    }

     //   if (cloudlet.isConnected()){
//            cloudletBtn.setText("Connected to Cloudlet!");
//            cloudletBtn.setEnabled(false);
     //   }
//        client.send(new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF});
//        client.end();
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
}
