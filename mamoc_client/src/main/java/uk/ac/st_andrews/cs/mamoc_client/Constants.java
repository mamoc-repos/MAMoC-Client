package uk.ac.st_andrews.cs.mamoc_client;

import android.Manifest;

public class Constants {

    public static final String DB_NAME = "mamoc.db";

    public static String EDGE_IP = "ws://104.248.167.173:8080/ws";
    public static final String REALM_NAME = "realm1";

    //"192.168.0.12:8080"

    public static final int PING = 11;
    public static final int PONG = 12;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    public static final String REQUEST_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final int PHONE_ACCESS_PERM_REQ_CODE = 20;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 0;
}
