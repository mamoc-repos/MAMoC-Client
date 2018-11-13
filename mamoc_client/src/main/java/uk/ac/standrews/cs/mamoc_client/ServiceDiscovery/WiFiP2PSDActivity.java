package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import uk.ac.standrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.standrews.cs.mamoc_client.Constants;
import uk.ac.standrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.standrews.cs.mamoc_client.Communication.WiFiP2PSDReceiver;
import uk.ac.standrews.cs.mamoc_client.Communication.TransferConstants;
import uk.ac.standrews.cs.mamoc_client.Communication.DataHandler;
import uk.ac.standrews.cs.mamoc_client.Communication.DataSender;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.R;
import uk.ac.standrews.cs.mamoc_client.Utils.DialogUtils;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WiFiP2PSDActivity extends AppCompatActivity implements PeerListFragment.OnListFragmentInteractionListener
        , WifiP2pManager.ConnectionInfoListener {

//    private static final int CODE_PICK_IMAGE = 21;

    private static final String TAG = "WiFIP2PSD";
    private static final String SERVICE_INSTANCE = "MAMoC";
    private static final String SERVICE_TYPE = "_mamoc._tcp";

    PeerListFragment deviceListFragment;

    View progressBar;

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel wifip2pChannel;
    WiFiP2PSDReceiver wiFiP2PSDReceiver;
    //private boolean isWifiP2pEnabled = false;

    private CommunicationController commController;

    WifiP2pDnsSdServiceRequest serviceRequest = null;

    private MobileNode selectedDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nsd_activity);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initialize();
    }

    private void initialize() {

        commController = CommunicationController.getInstance(this);

        selectedDevice = new MobileNode(this);

        progressBar = findViewById(R.id.progressBarNSD);

        commController.startConnectionListener();

        setToolBarTitle(0);

        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifip2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

        checkWritePermission();
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

        return super.onOptionsItemSelected(item);
    }

    public void findPeers(View v) {
        Snackbar.make(v, "Replace this junk with yours", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery(int port) {

        String player = Utils.getValue(this, TransferConstants.KEY_USER_NAME);

        Map<String, String> record = new HashMap<String, String>();
        record.put(TransferConstants.KEY_BUDDY_NAME, player == null ? Build.MANUFACTURER : player);
        record.put(TransferConstants.KEY_PORT_NUMBER, String.valueOf(port));
        record.put(TransferConstants.KEY_DEVICE_STATUS, "available");
        record.put(TransferConstants.KEY_WIFI_IP, Utils.getIPAddress(true));

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_TYPE, record);
        wifiP2pManager.addLocalService(wifip2pChannel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.e(TAG, "ERRORCEPTION: Failed to add a service");
            }
        });
        discoverService();
    }

    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        wifiP2pManager.setDnsSdResponseListeners(wifip2pChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(TAG, instanceName + "####" + registrationType);
                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            // yes it is
                            WiFiP2pServiceHolder serviceHolder = new WiFiP2pServiceHolder();
                            serviceHolder.device = srcDevice;
                            serviceHolder.registrationType = registrationType;
                            serviceHolder.instanceName = instanceName;
                            connectP2p(serviceHolder);
                        } else {
                            //no it isn't
                        }
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        boolean isGroupOwner = device.isGroupOwner();
                        peerPort = Integer.parseInt(record.get(TransferConstants.KEY_PORT_NUMBER).toString());
                        Log.v(TAG, Build.MANUFACTURER + ". peer port received: " + peerPort);
                        if (peerIP != null && peerPort > 0 && !isConnectionInfoSent) {
                            String player = record.get(TransferConstants.KEY_BUDDY_NAME).toString();

                            DataSender.sendCurrentDeviceData(WiFiP2PSDActivity.this,
                                    peerIP, peerPort, true);
                            isWDConnected = true;
                            isConnectionInfoSent = true;
                        }
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiP2pManager.addServiceRequest(wifip2pChannel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(TAG, "ERRORCEPTION: Failed adding service discovery request");
                    }
                });
        wifiP2pManager.discoverServices(wifip2pChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(TAG, "Service discovery failed: " + arg0);
            }
        });
    }

    @Override
    protected void onPause() {
//        if (mNsdHelper != null) {
//            mNsdHelper.stopDiscovery();
//        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MamocReceiver);
        unregisterReceiver(wiFiP2PSDReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(DataHandler.DEVICE_LIST_CHANGED);
        localFilter.addAction(DataHandler.REQUEST_RECEIVED);
        localFilter.addAction(DataHandler.RESPONSE_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(MamocReceiver,
                localFilter);

        IntentFilter wifip2pFilter = new IntentFilter();
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wiFiP2PSDReceiver = new WiFiP2PSDReceiver(wifiP2pManager,
                wifip2pChannel, this);
        registerReceiver(wiFiP2PSDReceiver, wifip2pFilter);

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DataHandler.DEVICE_LIST_CHANGED));
    }

    @Override
    protected void onDestroy() {
//        mNsdHelper.tearDown();
//        connListener.tearDown();
        commController.stopConnectionListener();

//        Utility.clearPreferences(LocalDashWiFiP2PSD.this);
//        Utility.deletePersistentGroups(wifiP2pManager, wifip2pChannel);

        if (wifiP2pManager != null && wifip2pChannel != null) {
            wifiP2pManager.removeGroup(wifip2pChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
        super.onDestroy();
    }

    private BroadcastReceiver MamocReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DataHandler.DEVICE_LIST_CHANGED:
                    ArrayList<MobileNode> devices = DBAdapter.getInstance(WiFiP2PSDActivity.this).getMobileDevicesList();
                    int peerCount = (devices == null) ? 0 : devices.size();
                    if (peerCount > 0) {
                        isWDConnected = true;
                        progressBar.setVisibility(View.GONE);
                        deviceListFragment = new PeerListFragment();
                        Bundle args = new Bundle();
                        args.putSerializable(PeerListFragment.ARG_DEVICE_LIST, devices);
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
                    DialogUtils.getChatRequestDialog(WiFiP2PSDActivity.this,
                            chatRequesterDevice).show();
                    break;
                case DataHandler.RESPONSE_RECEIVED:
                    boolean isChatRequestAccepted = intent.getBooleanExtra(DataHandler
                            .KEY_IS_REQUEST_ACCEPTED, false);
                    if (!isChatRequestAccepted) {
                        Toast.makeText(WiFiP2PSDActivity.this, "Rejected!", Toast.LENGTH_SHORT).show();
                    } else {
                        MobileNode chatDevice = (MobileNode) intent.getSerializableExtra(DataHandler
                                .KEY_REQUEST);
                        DialogUtils.openChatActivity(WiFiP2PSDActivity.this, chatDevice);
                        Toast.makeText(WiFiP2PSDActivity.this, chatDevice.getIp() + " Accepted request", Toast.LENGTH_SHORT).show();
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

//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case CODE_PICK_IMAGE:
//                    Uri imageUri = data.getData();
//                    DataSender.sendFile(LocalDashWiFiP2PSD.this, selectedDevice.getIp(),
//                            selectedDevice.getPort(), imageUri);
//                    break;
//                default:
//                    break;
//            }
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
        } else {
            startRegistrationAndDiscovery(Utils.getPort(this));
        }
    }

    private boolean isConnectionInfoSent = false;
    private String peerIP = null;
    private int peerPort = -1;

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

        Log.v(TAG, Build.MANUFACTURER + ". Conn info available" + wifiP2pInfo);
        Log.v(TAG, Build.MANUFACTURER + ". peer port: " + peerPort);

        if (wifiP2pInfo.groupFormed) {
            peerIP = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        }

        if (!isConnectionInfoSent && peerPort > 0 && wifiP2pInfo != null && wifiP2pInfo.groupFormed) {
            DataSender.sendCurrentDeviceData(this, peerIP, peerPort, true);
            isConnectionInfoSent = true;
        }
    }


    private boolean isConnectP2pCalled = false;

    private void connectP2p(WiFiP2pServiceHolder serviceHolder) {
        if (!isConnectP2pCalled) {
            isConnectP2pCalled = true;
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = serviceHolder.device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            if (serviceRequest != null)
                wifiP2pManager.removeServiceRequest(wifip2pChannel, serviceRequest,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int arg0) {
                            }
                        });

            wifiP2pManager.connect(wifip2pChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    //("Connecting to service");
                }

                @Override
                public void onFailure(int errorCode) {
                    //("Failed connecting to service");
                }
            });
        }
    }

    private boolean isWDConnected = false;

    @Override
    public void onListFragmentInteraction(MobileNode deviceDTO) {
        if (!isWDConnected) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceDTO.getIp();
            config.wps.setup = WpsInfo.PBC;
            wifiP2pManager.connect(wifip2pChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Connection request succeeded. No code needed here
                }

                @Override
                public void onFailure(int i) {
                    Toast.makeText(WiFiP2PSDActivity.this, "Connection failed. try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(WiFiP2PSDActivity.this, deviceDTO.getIp() + " clicked", Toast.LENGTH_SHORT).show();
            selectedDevice = deviceDTO;
            DialogUtils.getServiceSelectionDialog(this, deviceDTO).show();
        }
    }

    private class WiFiP2pServiceHolder {
        WifiP2pDevice device;
        String instanceName;
        String registrationType;
    }

    private void setToolBarTitle(int peerCount) {
        if (getSupportActionBar() != null) {
            String title = "connected: " + String
                    .valueOf(peerCount);
            getSupportActionBar().setTitle(title);

        }
    }
}

