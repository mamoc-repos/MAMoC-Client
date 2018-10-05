package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.st_andrews.cs.mamoc_client.Constants;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.st_andrews.cs.mamoc_client.profilers.NetworkType;

class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = Constants.DB_NAME;

    // Devices table columns
    static final String TABLE_DEVICES = "devices";
    static final String COL_DEV_ID = "deviceid";
    static final String COL_DEV_IP = "ipaddress";
    static final String COL_DEV_MODEL = "devicemodel";
    static final String COL_DEV_PORT = "port";
    static final String COL_DEV_VERSION = "osversion";

    // offload table columns
    private String TABLE_OFFLOAD = "offloads";
    private String COL_APP_NAME = "appname";
    private String COL_METHOD_NAME = "methodname";
    private ExecutionLocation COL_EXEC_LOCATION = ExecutionLocation.DYNAMIC;
    private int COL_NETWORK_TYPE = NetworkType.UNKNOWN;
    private int ulRate;
    private int dlRate;
    private long execDuration;
    private long execEnergy;
    private long timestamp;

    private static final String CREATE_DEVICE_TABLE = "CREATE TABLE " + TABLE_DEVICES + "("
            + COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_DEV_IP + " TEXT NOT NULL, "
            + COL_DEV_MODEL + " TEXT NOT NULL, "
            + COL_DEV_PORT + " INTEGER DEFAULT -1, "
            + COL_DEV_VERSION + " TEXT " + ");";

    static final String TABLE_CONNECTIONS = "connections";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
