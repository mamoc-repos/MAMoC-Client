package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.standrews.cs.mamoc_client.Constants;
import uk.ac.standrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.R;
import uk.ac.standrews.cs.mamoc_client.Utils.DialogUtils;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

public class NSD_Activity extends AppCompatActivity implements PeerListFragment.OnListFragmentInteractionListener {
    NsdHelper nsdHelper;
    PeerListFragment deviceListFragment;

    ServiceDiscovery serviceDiscovery = null;

    View progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nsd_activity);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressBar = findViewById(R.id.progressBarNSD);

        serviceDiscovery = ServiceDiscovery.getInstance(this);

        String ip = Utils.getIPAddress(true);
        Utils.save(this, TransferConstants.KEY_MY_IP, ip);

        checkWritePermission();

        setToolBarTitle(0);

        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();
        nsdHelper.discoverServices();
    }

    private void setToolBarTitle(int peerCount) {
        if (getSupportActionBar() != null) {
            String title = String.format(getString(R.string.nsd_title_with_count), String
                    .valueOf(peerCount));
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nsd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // handle arrow click here
        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void advertiseService(View view) {
        nsdHelper.registerService(Utils.getPort(this));

        Log.d("Info", Build.MANUFACTURER + " IP: " + Utils.getIPAddress(true));
        Snackbar.make(view,
                "Advertising on: " + Utils.getIPAddress(true),
                Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onPause() {
        if (nsdHelper != null) {
            nsdHelper.stopDiscovery();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mamocReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NsdHelper.BROADCAST_TAG);
        filter.addAction(DataHandler.DEVICE_LIST_CHANGED);
        filter.addAction(DataHandler.REQUEST_RECEIVED);
        filter.addAction(DataHandler.RESPONSE_RECEIVED);
        LocalBroadcastManager.getInstance(NSD_Activity.this).registerReceiver(mamocReceiver,
                filter);
        LocalBroadcastManager.getInstance(NSD_Activity.this).sendBroadcast(new Intent(DataHandler
                .DEVICE_LIST_CHANGED));
    }

    @Override
    protected void onDestroy() {
        Utils.clearPreferences(NSD_Activity.this);
        serviceDiscovery.stopConnectionListener();
        nsdHelper.tearDown();
        nsdHelper = null;
    //    DBAdapter.getInstance(this).clearDatabase();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d("DXDX", Build.MANUFACTURER + ": NSD Stopped");
        super.onStop();
    }

    private BroadcastReceiver mamocReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("broadcastswitch", intent.getAction());

            switch (intent.getAction()) {
                case NsdHelper.BROADCAST_TAG:
                    NsdServiceInfo serviceInfo = nsdHelper.getChosenServiceInfo();
                    String ipAddress = serviceInfo.getHost().getHostAddress();
                    int port = serviceInfo.getPort();
                    DataSender.sendCurrentDeviceData(NSD_Activity.this, ipAddress, port, true);
                    break;
                case DataHandler.DEVICE_LIST_CHANGED:
                    ArrayList<MobileNode> devices = DBAdapter.getInstance(NSD_Activity.this)
                            .getMobileDevicesList();
                    int peerCount = (devices == null) ? 0 : devices.size();
                    Log.v("peercount:", String.valueOf(peerCount));

                    if (peerCount > 0) {
                        progressBar.setVisibility(View.GONE);
                        deviceListFragment = new PeerListFragment();
                        Bundle args = new Bundle();
                        args.putSerializable(PeerListFragment.ARG_DEVICE_LIST, devices);
                        Log.v("ARG_DEVICE_LIST", PeerListFragment.ARG_DEVICE_LIST);
                        deviceListFragment.setArguments(args);

                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.deviceListHolder, deviceListFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
                        ft.commit();
                    }
                    setToolBarTitle(peerCount);
                    break;
                case DataHandler.REQUEST_RECEIVED:
                    MobileNode chatRequesterDevice = (MobileNode) intent.getSerializableExtra(DataHandler
                            .KEY_REQUEST);
                    DialogUtils.getChatRequestDialog(NSD_Activity.this, chatRequesterDevice).show();
                    break;
                case DataHandler.RESPONSE_RECEIVED:
                    boolean isChatRequestAccepted = intent.getBooleanExtra(DataHandler
                            .KEY_IS_REQUEST_ACCEPTED, false);
                    if (!isChatRequestAccepted) {
                        Toast.makeText(NSD_Activity.this, "Request Rejected!", Toast.LENGTH_SHORT).show();
                    } else {
                        MobileNode chatDevice = (MobileNode) intent.getSerializableExtra(DataHandler
                                .KEY_REQUEST);
                        DialogUtils.openChatActivity(NSD_Activity.this, chatDevice);
                        Toast.makeText(NSD_Activity.this, "Request Accepted from:" + chatDevice.getNodeName(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        switch (requestCode) {
//            case DialogUtils.CODE_PICK_IMAGE:
//                if (resultCode == RESULT_OK) {
//                    Uri imageUri = data.getData();
//                    DataSender.sendFile(LocalDashNSD.this, selectedDevice.getIp(),
//                            selectedDevice.getPort(), imageUri);
//                }
//                break;
//            default:
//                break;
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            finish();
        }
    }

    private void checkWritePermission() {
        boolean isGranted = Utils.checkPermission(Constants.WRITE_PERMISSION, this);
        if (!isGranted) {
            Utils.requestPermission(Constants.WRITE_PERMISSION, Constants
                    .WRITE_PERM_REQ_CODE, this);
        }
    }

    @Override
    public void onListFragmentInteraction(MobileNode node) {
        Toast.makeText(this, "Trying to connect to: " + node.getIp(), Toast.LENGTH_SHORT).show();
        MobileNode selectedNode = node;
        DialogUtils.getServiceSelectionDialog(this, selectedNode).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
