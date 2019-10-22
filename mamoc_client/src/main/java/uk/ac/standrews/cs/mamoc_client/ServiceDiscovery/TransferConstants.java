package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

public interface TransferConstants {

    int INITIAL_DEFAULT_PORT = 8998;

    String KEY_MY_IP = "LocalIP";
    String KEY_NODE_NAME = "NodeName";

    String TYPE_REQUEST = "request";
    String TYPE_RESPONSE = "response";

    int CLIENT_DATA = 3001;

    int DATA = 3004;
    int REQUEST_SENT = 3011;
    int REQUEST_ACCEPTED = 3012;
    int REQUEST_REJECTED = 3013;

    String KEY_PORT_NUMBER = "PortNumber";
    String KEY_DEVICE_STATUS = "DeviceStatus";
    String KEY_WIFI_IP = "WifiIP";
}
