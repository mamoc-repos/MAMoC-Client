package uk.ac.standrews.cs.emap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "mamoc.db";

    static final String TABLE_DEVICES = "devices";
    static final String COL_DEV_ID = "deviceid";
    static final String COL_DEV_IP = "ipaddress";
    static final String COL_DEV_MODEL = "devicemodel";
    static final String COL_DEV_PORT = "port";
    static final String COL_DEV_VERSION = "osversion";

    private static final String CREATE_DEVICE_TABLE = "CREATE TABLE " + TABLE_DEVICES + "("
            + COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_DEV_IP + " TEXT NOT NULL, "
            + COL_DEV_MODEL + " TEXT NOT NULL, "
            + COL_DEV_PORT + " INTEGER DEFAULT -1, "
            + COL_DEV_VERSION + " TEXT " + ");";

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
