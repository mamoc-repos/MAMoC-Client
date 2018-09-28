package uk.ac.standrews.cs.emap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

class DataHandler {
    public static final String DEVICE_LIST_CHANGED = "device_list_updated";
    public static final String REQUEST_RECEIVED =  "request_received";
    public static final String RESPONSE_RECEIVED = "response_received";

    public static final String KEY_CHAT_REQUEST = "chat_requester_or_responder";
    public static final String KEY_IS_CHAT_REQUEST_ACCEPTED = "is_chat_request_Accespter";

    private Context mContext;
    private ITransferable data;
    private String senderIP;
    private LocalBroadcastManager broadcaster;
    private DBAdapter dbAdapter = null;

    public DataHandler(Context mContext, ITransferable data, String senderIP) {
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
                        dbAdapter.getDevice(senderIP).getPort(), false);
                break;
            case TransferConstants.CHAT_REQUEST_SENT:
                processChatRequestReceipt();
            default:
                break;
        }
    }

    private void processResponse() {
        switch (data.getRequestCode()) {
            case TransferConstants.CLIENT_DATA:
                processPeerDeviceInfo();
                break;
            case TransferConstants.CHAT_DATA:
                processChatData();
                break;
            case TransferConstants.CHAT_REQUEST_ACCEPTED:
                processChatRequestResponse(true);
                break;
            case TransferConstants.CHAT_REQUEST_REJECTED:
                processChatRequestResponse(false);
                break;
            default:
                break;
        }
    }

    private void processPeerDeviceInfo() {
        String deviceJSON = data.getData();
        MamocNode device = MamocNode.fromJSON(deviceJSON);
        device.setIp(senderIP);
        long rowid = dbAdapter.addDevice(device);

        if (rowid > 0) {
            Log.d("DXDX", Build.MANUFACTURER + " received: " + deviceJSON);
        } else {
            Log.e("DXDX", Build.MANUFACTURER + " can't save: " + deviceJSON);
        }

        Intent intent = new Intent(DEVICE_LIST_CHANGED);
        broadcaster.sendBroadcast(intent);
    }

    private void processChatRequestReceipt() {
        String chatRequesterDeviceJSON = data.getData();
        MamocNode chatRequesterDevice = MamocNode.fromJSON(chatRequesterDeviceJSON);
        chatRequesterDevice.setIp(senderIP);

        Intent intent = new Intent(REQUEST_RECEIVED);
        intent.putExtra(KEY_CHAT_REQUEST, chatRequesterDevice);
        broadcaster.sendBroadcast(intent);
    }

    private void processChatRequestResponse(boolean isRequestAccepted) {
        String chatResponderDeviceJSON = data.getData();
        MamocNode chatResponderDevice = MamocNode.fromJSON(chatResponderDeviceJSON);
        chatResponderDevice.setIp(senderIP);

        Intent intent = new Intent(RESPONSE_RECEIVED);
        intent.putExtra(KEY_CHAT_REQUEST, chatResponderDevice);
        intent.putExtra(KEY_IS_CHAT_REQUEST_ACCEPTED, isRequestAccepted);
        broadcaster.sendBroadcast(intent);
    }

    private void processChatData() {
//        String chatJSON = data.getData();
//        ChatDTO chatObject = ChatDTO.fromJSON(chatJSON);
//        chatObject.setFromIP(senderIP);
//        //Save in db if needed here
//        Intent chatReceivedIntent = new Intent(ChatActivity.ACTION_CHAT_RECEIVED);
//        chatReceivedIntent.putExtra(ChatActivity.KEY_CHAT_DATA, chatObject);
//        broadcaster.sendBroadcast(chatReceivedIntent);
    }


}
