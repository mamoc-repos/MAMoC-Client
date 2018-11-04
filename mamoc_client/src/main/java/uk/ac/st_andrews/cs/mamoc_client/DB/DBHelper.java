package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.st_andrews.cs.mamoc_client.Constants;
import uk.ac.st_andrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.st_andrews.cs.mamoc_client.Profilers.NetworkType;

class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = Constants.DB_NAME;

    // offload table columns
    private static final String TABLE_OFFLOAD = "offloads";
    private static final int OFFLOAD_ID = 0;
    private static final String COL_APP_NAME = "appname";
    private static final String COL_TASK_NAME = "taskname";
    private static final String COL_EXEC_LOCATION = ExecutionLocation.LOCAL.getValue();
    private static final int COL_NETWORK_TYPE = NetworkType.UNKNOWN;
    private static final long COL_EXECUTION_TIME = 0;
    private static final long COL_COMMUNICATION_OVERHEAD = 0;
    private static final int COL_RTT_SPEED = 0;
    private static final long COL_OFFLOAD_DATE = System.currentTimeMillis();

    private static final String CREATE_OFFLOAD_TABLE = "CREATE TABLE " + TABLE_OFFLOAD + "("
            + OFFLOAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_APP_NAME + "TEXT NOT NULL, "
            + COL_TASK_NAME + "TEXT NOT NULL, "
            + COL_EXEC_LOCATION + "INTEGER NOT NULL, "
            + COL_NETWORK_TYPE + "INTEGER NOT NULL, "
            + COL_EXECUTION_TIME + "INTEGER, "
            + COL_COMMUNICATION_OVERHEAD + "INTEGER, "
            + COL_RTT_SPEED + "INTEGER, "
            + COL_OFFLOAD_DATE + "INTEGER"
            + ");";

    // Mobile devices table columns
    static final String TABLE_MOBILE_DEVICES = "mobiledevices";
    static final String COL_DEV_ID = "deviceid";
    static final String COL_DEV_NAME = "devicename";
    static final String COL_DEV_IP = "ipaddress";
    static final String COL_DEV_CPU_FREQ = "cpufreq";
    static final String COL_DEV_CPU_NUM = "cpunum";
    static final String COL_DEV_MEMORY = "memory";
    static final String COL_DEV_JOINED = "joined";
    static final String COL_DEV_BATTERY_LEVEL = "batterylevel";
    static final String COL_DEV_BATTERY_STATE = "batterystate";
    static final String COL_OFFLOADING_SCORE = "offloadingscore";

    private static final String CREATE_DEVICE_TABLE = "CREATE TABLE " + TABLE_MOBILE_DEVICES + "("
            + COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_DEV_NAME + " TEXT NOT NULL, "
            + COL_DEV_IP + " TEXT NOT NULL, "
            + COL_DEV_CPU_NUM + " INTEGER, "
            + COL_DEV_CPU_FREQ + " INTEGER, "
            + COL_DEV_MEMORY + " INTEGER, "
            + COL_DEV_JOINED + " INTEGER, "
            + COL_DEV_BATTERY_LEVEL + " INTEGER, "
            + COL_DEV_BATTERY_STATE + " TEXT, "
            + COL_OFFLOADING_SCORE + " INTEGER"
            + ");";


    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_OFFLOAD_TABLE);
        sqLiteDatabase.execSQL(CREATE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
