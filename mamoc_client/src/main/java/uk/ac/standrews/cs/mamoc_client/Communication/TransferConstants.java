package uk.ac.standrews.cs.mamoc_client.Communication;

public interface TransferConstants {
    String KEY_MY_IP = "LocalIP";
    String KEY_NODE_NAME = "nodename";

    String TYPE_REQUEST = "request";
    String TYPE_RESPONSE = "response";

    int CLIENT_DATA = 3001;

    int DATA = 3004;
    int REQUEST_SENT = 3011;
    int REQUEST_ACCEPTED = 3012;
    int REQUEST_REJECTED = 3013;

    String KEY_BUDDY_NAME = "buddyname";
    String KEY_PORT_NUMBER = "portnumber";
    String KEY_DEVICE_STATUS = "devicestatus";
    String KEY_USER_NAME = "username";
    String KEY_WIFI_IP = "wifiip";
}
