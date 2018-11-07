package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.st_andrews.cs.mamoc_client.Constants;

class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = Constants.DB_NAME;

    // offload table columns
    static final String TABLE_OFFLOAD = "offloads";
    static final String OFFLOAD_ID = "offload_id";
    static final String COL_APP_NAME = "app_name";
    static final String COL_TASK_NAME = "task_name";
    static final String COL_EXEC_LOCATION = "execution_location";
    static final String COL_NETWORK_TYPE = "network_type";
    static final String COL_EXECUTION_TIME = "execution_time";
    static final String COL_COMMUNICATION_OVERHEAD = "communication_overhead";
    static final String COL_RTT_SPEED = "rtt_speed";
    static final String COL_OFFLOAD_DATE = "offload_date";

    private static final String CREATE_OFFLOAD_TABLE = "CREATE TABLE " + TABLE_OFFLOAD + "("
            + OFFLOAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_APP_NAME + " TEXT NOT NULL, "
            + COL_TASK_NAME + " TEXT NOT NULL, "
            + COL_EXEC_LOCATION + " INTEGER NOT NULL, "
            + COL_NETWORK_TYPE + " INTEGER NOT NULL, "
            + COL_EXECUTION_TIME + " INTEGER, "
            + COL_COMMUNICATION_OVERHEAD + " INTEGER, "
            + COL_RTT_SPEED + " INTEGER, "
            + COL_OFFLOAD_DATE + " INTEGER"
            + ");";

    // Mobile devices table columns
    static final String TABLE_MOBILE_DEVICES = "mobile_devices";
    static final String COL_DEV_ID = "device_id";
    static final String COL_DEV_NAME = "device_name";
    static final String COL_DEV_IP = "ip_address";
    static final String COL_DEV_CPU_FREQ = "cpu_freq";
    static final String COL_DEV_CPU_NUM = "cpu_num";
    static final String COL_DEV_MEMORY = "memory";
    static final String COL_DEV_JOINED = "joined";
    static final String COL_DEV_BATTERY_LEVEL = "battery_level";
    static final String COL_DEV_BATTERY_STATE = "battery_state";
    static final String COL_OFFLOADING_SCORE = "offloading_score";

    private static final String CREATE_MOBILE_DEVICE_TABLE = "CREATE TABLE " + TABLE_MOBILE_DEVICES + "("
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
        sqLiteDatabase.execSQL(CREATE_MOBILE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
