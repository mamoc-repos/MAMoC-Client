package uk.ac.standrews.cs.emap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Button discoverButton;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    private TextView listeningPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listeningPort = findViewById(R.id.ListenPort);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNSD();
            }
        });

        checkWritePermissions();
        logInterfaces();
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onResume() {
        super.onResume();
        DBAdapter.getInstance(MainActivity.this).clearDatabase();
        listeningPort.setText(String.format(getString(R.string.port_info), Utils.getPort(this)));
    }

    private void checkWritePermissions() {
        boolean isGranted = Utils.checkPermission(WRITE_PERMISSION, this);
        if (!isGranted){
            Utils.requestPermission(WRITE_PERMISSION, WRITE_PERM_REQ_CODE, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "This permission is needed!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void logInterfaces(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface ni: Collections.list(interfaces)
                 ) {
                Log.v(TAG, "Display name: " + ni.getDisplayName());
                Log.v(TAG, "name: " + ni.getName() );
                Log.v(TAG, "is it running? " + String.valueOf(ni.isUp()));
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                for (InetAddress singleNI: Collections.list(addresses)
                     ) {
                    Log.v(TAG, "inet address: " + singleNI.getHostAddress());
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void startNSD(){
        if (Utils.isWifiConnected(this)){
            Intent nsdIntent = new Intent(MainActivity.this, NSD_Activity.class);
            startActivity(nsdIntent);
            finish();
        } else {
            Toast.makeText(this, "Wifi not connected! :(", Toast.LENGTH_SHORT).show();
        }
    }
}
