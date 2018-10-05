package uk.ac.st_andrews.cs.mamoc_client;

import android.Manifest;

public class Constants {

    public static final String DB_NAME = "mamoc.db";

    public static String cloudletIP = "138.251.207.93:8080";
    //"192.168.0.12:8080"

    public static final int PING = 11;
    public static final int PONG = 12;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    public static final String REQUEST_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final int PHONE_ACCESS_PERM_REQ_CODE = 20;
}
