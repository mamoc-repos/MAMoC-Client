package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;

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

    public long addMobileDevice(MobileNode device) {
        if (device == null || device.getIp() == null || device.getPort() == 0) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_DEV_IP, device.getIp());
        values.put(DBHelper.COL_DEV_NAME, device.getDeviceID());
        values.put(DBHelper.COL_DEV_CPU_FREQ, device.getCpuFreq());
        values.put(DBHelper.COL_DEV_CPU_NUM, device.getNumberOfCPUs());
        values.put(DBHelper.COL_DEV_MEMORY, device.getMemoryMB());
        values.put(DBHelper.COL_DEV_JOINED, device.getJoinedDate());
        values.put(DBHelper.COL_DEV_BATTERY_LEVEL, device.getBatteryLevel());
        values.put(DBHelper.COL_DEV_BATTERY_STATE, device.getBatteryState().name());
        values.put(DBHelper.COL_DEV_MODEL, device.getManufacturer());
        values.put(DBHelper.COL_DEV_PORT, device.getPort());
        values.put(DBHelper.COL_DEV_VERSION, device.getOsVersion());

        if (!deviceExists(device.getIp())) {
            long rowId = db.insert(DBHelper.TABLE_DEVICES, null, values);
            return rowId;
        }

        return -1;
    }

    public ArrayList<MobileNode> getMobileDevicesList() {
        ArrayList<MobileNode> devices = null;

        Cursor cursor = db.query(DBHelper.TABLE_DEVICES, null, null, null, null, null,
                DBHelper.COL_DEV_ID);

        if (cursor != null) {
            devices = new ArrayList<>();
        } else {
            return devices;
        }

        int idIndex = cursor.getColumnIndex(DBHelper.COL_DEV_ID);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int cpuNumIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_NUM);
        int cpuFreqIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_FREQ);
        int memIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MEMORY);
        int joinedIndex = cursor.getColumnIndex(DBHelper.COL_DEV_JOINED);
        int blIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_LEVEL);
        int bsIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_STATE);
        int modelIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MODEL);
        int portIndex = cursor.getColumnIndex(DBHelper.COL_DEV_PORT);
        int versionIndex = cursor.getColumnIndex(DBHelper.COL_DEV_VERSION);

        while (cursor.moveToNext()) {
            MobileNode device = new MobileNode(context);

            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(Integer.parseInt(cursor.getString(cpuFreqIndex)));
            device.setNumberOfCPUs(Integer.parseInt(cursor.getString(cpuNumIndex)));
            device.setMemoryMB(Long.parseLong(cursor.getString(memIndex)));
            device.setJoinedDate(Long.parseLong(cursor.getString(joinedIndex)));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(cursor.getString(bsIndex));
            device.setPort(cursor.getInt(portIndex));
            device.setOsVersion(cursor.getString(versionIndex));
            device.setManufacturer(cursor.getString(modelIndex));

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

    public MobileNode getMobileDevice(String senderIP) {
        MobileNode device = null;

        Cursor cursor = db.query(DBHelper.TABLE_DEVICES, null, DBHelper.COL_DEV_IP + "=?",
                new String[]{senderIP}, null, null, DBHelper.COL_DEV_ID);

        if (cursor != null) {
            device = new MobileNode(context);
        } else {
            return device;
        }

        int idIndex = cursor.getColumnIndex(DBHelper.COL_DEV_ID);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int cpuNumIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_NUM);
        int cpuFreqIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_FREQ);
        int memIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MEMORY);
        int joinedIndex = cursor.getColumnIndex(DBHelper.COL_DEV_JOINED);
        int blIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_LEVEL);
        int bsIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_STATE);
        int modelIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MODEL);
        int portIndex = cursor.getColumnIndex(DBHelper.COL_DEV_PORT);
        int versionIndex = cursor.getColumnIndex(DBHelper.COL_DEV_VERSION);

        while (cursor.moveToNext()) {
            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(Integer.parseInt(cursor.getString(cpuFreqIndex)));
            device.setNumberOfCPUs(Integer.parseInt(cursor.getString(cpuNumIndex)));
            device.setMemoryMB(Long.parseLong(cursor.getString(memIndex)));
            device.setJoinedDate(Long.parseLong(cursor.getString(joinedIndex)));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(cursor.getString(bsIndex));
            device.setPort(cursor.getInt(portIndex));
            device.setOsVersion(cursor.getString(versionIndex));
            device.setManufacturer(cursor.getString(modelIndex));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return device;
    }
}
