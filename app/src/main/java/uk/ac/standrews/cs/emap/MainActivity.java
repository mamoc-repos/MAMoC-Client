package uk.ac.standrews.cs.emap;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Button discoverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discoverButton = findViewById(R.id.discoverBtn);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNSD();
            }
        });
        logInterfaces();
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
        if (isWifiConnected()){
            Intent nsdIntent = new Intent(MainActivity.this, NSD_Activity.class);
            startActivity(nsdIntent);
            finish();
        } else {
            Toast.makeText(this, "Wifi not connected! :(", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isWifiConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(this.getApplicationContext().CONNECTIVITY_SERVICE);

        return (cm != null) && (cm.getActiveNetworkInfo() != null) &&
                (cm.getActiveNetworkInfo().getType() == 1);
    }
}
