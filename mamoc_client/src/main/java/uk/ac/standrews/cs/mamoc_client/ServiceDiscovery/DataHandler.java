package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import uk.ac.standrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;

public class DataHandler {

    private final String TAG = "DataHandler";

    public static final String DEVICE_LIST_CHANGED = "device_list_updated";
    public static final String REQUEST_RECEIVED =  "request_received";
    public static final String RESPONSE_RECEIVED = "response_received";

    public static final String KEY_REQUEST = "_requester_or_responder";
    public static final String KEY_IS_REQUEST_ACCEPTED = "is__request_Accespter";

    private Context mContext;
    private ITransferable data;
    private String senderIP;
    private LocalBroadcastManager broadcaster;
    private DBAdapter dbAdapter = null;

    public DataHandler(Context mContext, String senderIP, ITransferable data) {
        this.mContext = mContext;
        this.data = data;
        this.senderIP = senderIP;
        this.dbAdapter = DBAdapter.getInstance(mContext);
        this.broadcaster = LocalBroadcastManager.getInstance(mContext);
    }

    public void process() {
        if (data.getRequestType().equalsIgnoreCase(TransferConstants.TYPE_REQUEST)){
            processRequest();
        } else {
            processResponse();
        }
    }

    private void processRequest() {
        switch (data.getRequestCode()) {
            case TransferConstants.CLIENT_DATA:
                processPeerDeviceInfo();
                DataSender.sendCurrentDeviceData(mContext, senderIP,
                        dbAdapter.getMobileDevice(senderIP).getPort(), false);
                break;
            case TransferConstants.REQUEST_SENT:
                processRequestReceipt();
            default:
                break;
        }
    }

    private void processResponse() {
        switch (data.getRequestCode()) {
            case TransferConstants.CLIENT_DATA:
                processPeerDeviceInfo();
                break;
            case TransferConstants.DATA:
                processData();
                break;
            case TransferConstants.REQUEST_ACCEPTED:
                processRequestResponse(true);
                break;
            case TransferConstants.REQUEST_REJECTED:
                processRequestResponse(false);
                break;
            default:
                break;
        }
    }

    private void processPeerDeviceInfo() {
        String deviceJSON = data.getData();
        Log.d("JSON", deviceJSON);
        MobileNode device = MobileNode.fromJSON(deviceJSON);

        Log.d("senderIP", senderIP);

        device.setIp(senderIP);
        long rowid = dbAdapter.addMobileDevice(device);

        if (rowid > 0) {
            Log.d(TAG, Build.MANUFACTURER + " received: " + deviceJSON);
        } else {
            Log.e(TAG, Build.MANUFACTURER + " can't save: " + deviceJSON);
        }

        Intent intent = new Intent(DEVICE_LIST_CHANGED);
        broadcaster.sendBroadcast(intent);
    }

    private void processRequestReceipt() {
        String RequesterDeviceJSON = data.getData();
        MobileNode RequesterDevice = MobileNode.fromJSON(RequesterDeviceJSON);
        RequesterDevice.setIp(senderIP);

        Intent intent = new Intent(REQUEST_RECEIVED);
        intent.putExtra(KEY_REQUEST, RequesterDevice);
        broadcaster.sendBroadcast(intent);
    }

    private void processRequestResponse(boolean isRequestAccepted) {
        String ResponderDeviceJSON = data.getData();
        MobileNode ResponderDevice = MobileNode.fromJSON(ResponderDeviceJSON);
        ResponderDevice.setIp(senderIP);

        Intent intent = new Intent(RESPONSE_RECEIVED);
        intent.putExtra(KEY_REQUEST, ResponderDevice);
        intent.putExtra(KEY_IS_REQUEST_ACCEPTED, isRequestAccepted);
        broadcaster.sendBroadcast(intent);
    }

    private void processData() {
        String JSON = data.getData();
        MamocNode object = MobileNode.fromJSON(JSON);
        object.setIp(senderIP);

        //Save in db if needed here


//        Intent ReceivedIntent = new Intent(Activity.ACTION_RECEIVED);
//        ReceivedIntent.putExtra(Activity.KEY_DATA, Object);
//        broadcaster.sendBroadcast(ReceivedIntent);
    }

}
