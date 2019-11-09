package uk.ac.standrews.cs.mamoc_client;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {

    public static final String DB_NAME = "mamoc.db";

//    public static String EDGE_IP = "ws://192.168.0.12:8080/ws";
    public static String EDGE_IP = "djs21.host.cs.st-andrews.ac.uk/offload/ws/"; // Change it to student host server for edge server

    public static final String EDGE_REALM_NAME = "mamoc_realm";

    public static String CLOUD_IP = "18.130.29.6";
    public static final String CLOUD_REALM_NAME = "mamoc_realm";

    public static final String WAMP_LOOKUP = "wamp.registration.match";

    public static final String OFFLOADING_PUB = "uk.ac.standrews.cs.mamoc.offloading";
    public static final String SENDING_FILE_PUB = "uk.ac.standrews.cs.mamoc.receive_file";
    public static final String OFFLOADING_RESULT_SUB = "uk.ac.standrews.cs.mamoc.offloadingresult";

    public static final String SERVICE_DISCOVERY_BROADCASTER = "service_discovery";

    public static final int PING = 11;
    public static final int PONG = 12;

    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int WRITE_PERM_REQ_CODE = 19;
    public static final String REQUEST_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final int PHONE_ACCESS_PERM_REQ_CODE = 20;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 0;

    private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String MAMOC_FOLDER = SDCARD + File.separator + "mamoc";
    public static final String DB_FILE = MAMOC_FOLDER + File.separator + "db-";

    // Moved to DecisionMaker.Config to enable them to be changed dynamically
//    public static final Double BANDWIDTH_SPEED = 1.0;
//    public static final Double BANDWIDTH_AVAILABILITY = 5.0;
//    public static final Double BANDWIDTH_SECURITY = 7.0;
//    public static final Double BANDWIDTH_PRICE = 9.0;
//    public static final Double SPEED_AVAILABILITY = 5.0;
//    public static final Double SPEED_SECURITY = 6.0;
//    public static final Double SPEED_PRICE = 8.0;
//    public static final Double AVAILABLITY_SECURITY = 3.0;
//    public static final Double AVAIALABILITY_PRICE = 3.0;
//    public static final Double SECURITY_PRICE = 2.0;

    // Changed to fuzzy values for TOPSIS (Check @Fuzzy class)
//    public static final Double IMPORTANCE_HIGH = 0.9;
//    public static final Double IMPORTANCE_MEDIUM = 0.5;
//    public static final Double IMPORTANCE_LOW = 0.1;

}
