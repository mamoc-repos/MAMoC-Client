package uk.ac.standrews.cs.emap;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

class NSD_Activity extends AppCompatActivity implements PeerListFragment.OnListFragmentInteractionListener{
    NsdHelper nsdHelper;
    PeerListFragment deviceListFragment;
    private MamocNode selectedNode;
    ConnectionListener connListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String ip = Utils.getLocalIpAddress();

        ConnectionListener listener = null;
        try {
            listener = new ConnectionListener(this, Utils.getPort());

        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.start();

        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();
        nsdHelper.discoverServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onListFragmentInteraction(MamocNode node) {

    }
}
