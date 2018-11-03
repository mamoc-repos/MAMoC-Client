package uk.ac.st_andrews.cs.mamoc_client;

import android.Manifest;

public class Constants {

    public static final String DB_NAME = "mamoc.db";

    public static final String EDGE_IP = "ws://192.168.0.12:8080/ws";
    public static final String EDGE_REALM_NAME = "mamoc_realm";

    public static final String CLOUD_IP = "ws://104.248.167.173:8080/ws";
    public static final String CLOUD_REALM_NAME = "mamoc_realm";

    public static final String WAMP_LOOKUP = "wamp.registration.lookup";

    public static final String OFFLOADING_PUB = "uk.ac.standrews.cs.mamoc.offloading";
    public static final String OFFLOADING_RESULT_SUB = "uk.ac.standrews.cs.mamoc.offloadingresult";

    public static final int PING = 11;
    public static final int PONG = 12;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    public static final String REQUEST_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final int PHONE_ACCESS_PERM_REQ_CODE = 20;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 0;
}
