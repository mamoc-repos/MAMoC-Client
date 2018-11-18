package uk.ac.standrews.cs.mamoc_client.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.ac.standrews.cs.mamoc_client.Constants;

class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = Constants.DB_NAME;

    // offload table columns
    static final String TABLE_OFFLOAD = "offloads";
    static final String OFFLOAD_ID = "offloadid";
    static final String COL_APP_NAME = "appname";
    static final String COL_TASK_NAME = "taskname";
    static final String COL_EXEC_LOCATION = "executionlocation";
    static final String COL_NETWORK_TYPE = "networktype";
    static final String COL_EXECUTION_TIME = "executiontime";
    static final String COL_COMMUNICATION_OVERHEAD = "communicationoverhead";
    static final String COL_RTT_SPEED = "rttspeed";
    static final String COL_OFFLOAD_DATE = "offloaddate";
    static final String COL_OFFLOAD_COMPLETE = "completed";

    private static final String CREATE_OFFLOAD_TABLE = "CREATE TABLE if not exists " + TABLE_OFFLOAD + "("
            + OFFLOAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_APP_NAME + " TEXT NOT NULL, "
            + COL_TASK_NAME + " TEXT NOT NULL, "
            + COL_EXEC_LOCATION + " TEXT NOT NULL, "
            + COL_NETWORK_TYPE + " TEXT NOT NULL, "
            + COL_EXECUTION_TIME + " INTEGER, "
            + COL_COMMUNICATION_OVERHEAD + " INTEGER, "
            + COL_RTT_SPEED + " INTEGER, "
            + COL_OFFLOAD_DATE + " INTEGER, "
            + COL_OFFLOAD_COMPLETE + " INTEGER"
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

    private static final String CREATE_MOBILE_DEVICE_TABLE = "CREATE TABLE if not exists " + TABLE_MOBILE_DEVICES + "("
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
        getWritableDatabase().execSQL(CREATE_OFFLOAD_TABLE);
        getWritableDatabase().execSQL(CREATE_MOBILE_DEVICE_TABLE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBHelper", "creating tables");
        db.execSQL(CREATE_OFFLOAD_TABLE);
        db.execSQL(CREATE_MOBILE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
