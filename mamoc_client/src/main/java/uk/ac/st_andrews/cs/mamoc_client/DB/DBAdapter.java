package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import uk.ac.st_andrews.cs.mamoc_client.Model.MamocNode;

public class DBAdapter {
    private static DBAdapter instance;
    private static Object lockObject = new Object();
    private Context context;

    private SQLiteDatabase db = null;

    private DBAdapter(Context context) {
        this.context = context;
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public static DBAdapter getInstance(Context context) {
        if (instance == null) {
            synchronized (DBAdapter.class) {
                if (instance == null) {
                    instance = new DBAdapter(context);
                }
            }
        }
        return instance;
    }

    public long addDevice(MamocNode device) {
        if (device == null || device.getIp() == null || device.getPort() == 0) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_DEV_MODEL, device.getNodeName());
        values.put(DBHelper.COL_DEV_IP, device.getIp());
        values.put(DBHelper.COL_DEV_PORT, device.getPort());
       // values.put(DBHelper.COL_DEV_VERSION, device.getOsVersion());

        if (!deviceExists(device.getIp())) {
            long rowId = db.insert(DBHelper.TABLE_DEVICES, null, values);
            return rowId;
        }

        return -1;
    }

    public ArrayList<MamocNode> getDeviceList() {
        ArrayList<MamocNode> devices = null;

        Cursor cursor = db.query(DBHelper.TABLE_DEVICES, null, null, null, null, null,
                DBHelper.COL_DEV_ID);

        if (cursor != null) {
            devices = new ArrayList<>();
        } else {
            return devices;
        }

        int modelIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MODEL);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int portIndex = cursor.getColumnIndex(DBHelper.COL_DEV_PORT);
        int versionIndex = cursor.getColumnIndex(DBHelper.COL_DEV_VERSION);

        while (cursor.moveToNext()) {
            MamocNode device = new MamocNode();
            device.setNodeName(cursor.getString(modelIndex));
            device.setIp(cursor.getString(ipIndex));
            device.setPort(cursor.getInt(portIndex));
        //    device.setOsVersion(cursor.getString(versionIndex));

            devices.add(device);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return devices;
    }



    private boolean deviceExists(String ip) {
        Cursor cursor = db.query(DBHelper.TABLE_DEVICES, null, DBHelper.COL_DEV_IP + "=?", new
                String[]{ip}, null, null, null);

        return (cursor.getCount() > 0);
    }

    public int clearDatabase() {
        int rowsAffected = db.delete(DBHelper.TABLE_DEVICES, null, null);
        return rowsAffected;
    }

    public boolean removeDevice(String ip) {
        int rowsAffected = db.delete(DBHelper.TABLE_DEVICES, DBHelper.COL_DEV_IP + "=?"
                , new String[]{ip});
        return (rowsAffected > 0);
    }

    public MamocNode getDevice(String senderIP) {
        MamocNode device = null;

        Cursor cursor = db.query(DBHelper.TABLE_DEVICES, null, DBHelper.COL_DEV_IP + "=?",
                new String[]{senderIP}, null, null, DBHelper.COL_DEV_ID);

        if (cursor != null) {
            device = new MamocNode();
        } else {
            return device;
        }

        int modelIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MODEL);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int portIndex = cursor.getColumnIndex(DBHelper.COL_DEV_PORT);
        int versionIndex = cursor.getColumnIndex(DBHelper.COL_DEV_VERSION);

        if (cursor.moveToNext()) {
            device.setNodeName(cursor.getString(modelIndex));
            device.setIp(cursor.getString(ipIndex));
            device.setPort(cursor.getInt(portIndex));
        //    device.setOsVersion(cursor.getString(versionIndex));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return device;
    }
}
