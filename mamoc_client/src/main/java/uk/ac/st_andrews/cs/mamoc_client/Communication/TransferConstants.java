package uk.ac.st_andrews.cs.mamoc_client.Communication;

public interface TransferConstants {
    String KEY_MY_IP = "LocalIP";
    String KEY_NODE_NAME = "nodename";

    int INITIAL_DEFAULT_PORT = 8080;
    int CLIENT_DATA = 3001;

    String TYPE_REQUEST = "request";
    String TYPE_RESPONSE = "response";

    int CHAT_DATA = 3004;
    int CHAT_REQUEST_SENT = 3011;
    int CHAT_REQUEST_ACCEPTED = 3012;
    int CHAT_REQUEST_REJECTED = 3013;
}
