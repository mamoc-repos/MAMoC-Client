package uk.ac.standrews.cs.mamoc_client;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {

    public static final String DB_NAME = "mamoc.db";

    public static final String EDGE_IP = "192.168.0.12";
    public static final String EDGE_REALM_NAME = "mamoc_realm";

    public static final String CLOUD_IP = "104.248.167.173";
    public static final String CLOUD_REALM_NAME = "mamoc_realm";

    public static final String WAMP_LOOKUP = "wamp.registration.match";

    public static final String OFFLOADING_PUB = "uk.ac.standrews.cs.mamoc.offloading";
    public static final String OFFLOADING_RESULT_SUB = "uk.ac.standrews.cs.mamoc.offloadingresult";

    public static final int PING = 11;
    public static final int PONG = 12;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    public static final String REQUEST_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final int PHONE_ACCESS_PERM_REQ_CODE = 20;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 0;

    public static final double CPU_WEIGHT = 0.3;
    public static final double MEMORY_WEIGHT = 0.2;
    public static final double RTT_WEIGHT = 0.3;
    public static final double BATTERY_WEIGHT = 0.2;

    private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String MAMOC_FOLDER = SDCARD + File.separator + "mamoc";
    public static final String DB_FILE = MAMOC_FOLDER + File.separator + "db-";

    public static final Double BANDWIDTH_SPEED = 1.0;
    public static final Double BANDWIDTH_AVAILABILITY = 5.0;
    public static final Double BANDWIDTH_SECURITY = 7.0;
    public static final Double BANDWIDTH_PRICE = 9.0;
    public static final Double SPEED_AVAILABILITY = 5.0;
    public static final Double SPEED_SECURITY = 7.0;
    public static final Double SPEED_PRICE = 9.0;
    public static final Double AVAILABLITY_SECURITY = 3.0;
    public static final Double AVAIALABILITY_PRICE = 3.0;
    public static final Double SECURITY_PRICE = 2.0;


    public static final Double IMPORTANCE_HIGH = 0.9;
    public static final Double IMPORTANCE_MEDIUM = 0.5;
    public static final Double IMPORTANCE_LOW = 0.1;

}
