package uk.ac.standrews.cs.mamoc_client.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.TaskExecution;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkType;

import static uk.ac.standrews.cs.mamoc_client.DB.DBHelper.TABLE_MOBILE_DEVICES;
import static uk.ac.standrews.cs.mamoc_client.DB.DBHelper.TABLE_OFFLOAD;

public class DBAdapter {

    private final String TAG = "DSAdapter";

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

    public long addTaskExecution(TaskExecution task){

        Log.d(TAG, "inserting task to db: " + task.getTaskName());

        if (task == null) { return -1; }

        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_APP_NAME, context.getPackageName());
        values.put(DBHelper.COL_TASK_NAME, task.getTaskName());
        values.put(DBHelper.COL_EXEC_LOCATION, task.getExecLocation().toString());
        values.put(DBHelper.COL_COMMUNICATION_OVERHEAD, task.getCommOverhead());
        values.put(DBHelper.COL_NETWORK_TYPE, task.getNetworkType().toString());
        values.put(DBHelper.COL_RTT_SPEED, task.getRttSpeed());
        values.put(DBHelper.COL_OFFLOAD_DATE, task.getExecutionDate());
        values.put(DBHelper.COL_OFFLOAD_COMPLETE, task.isCompleted() ? 1 : 0);

        return db.insert(TABLE_OFFLOAD, null, values);
    }

    public ArrayList<TaskExecution> getExecutions(String taskName, boolean Remote){

        Log.d(TAG, "Fetching " + (Remote?"remote":"local") + " executions: " + taskName);

        ArrayList<TaskExecution> taskExecutions =  new ArrayList<>();

        Cursor cursor = db.rawQuery("select * from " + TABLE_OFFLOAD,null);

        int nameIndex = cursor.getColumnIndex(DBHelper.COL_TASK_NAME);
        int locationIndex = cursor.getColumnIndex(DBHelper.COL_EXEC_LOCATION);
        int networkTypeIndex = cursor.getColumnIndex(DBHelper.COL_NETWORK_TYPE);
        int execTimeIndex = cursor.getColumnIndex(DBHelper.COL_EXECUTION_TIME);
        int commIndex = cursor.getColumnIndex(DBHelper.COL_COMMUNICATION_OVERHEAD);
        int rttIndex = cursor.getColumnIndex(DBHelper.COL_RTT_SPEED);
        int offDateIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOAD_DATE);
        int completedIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOAD_COMPLETE);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_TASK_NAME));
            if (Remote) {
                // only fetch the executions where the commOverhead if greater than 0
                if (name.equalsIgnoreCase(taskName) && cursor.getDouble(commIndex) > 0) {
                    TaskExecution remote = new TaskExecution();
                    remote.setTaskName(cursor.getString(nameIndex));
                    remote.setExecLocation(ExecutionLocation.valueOf(cursor.getString(locationIndex)));
                    remote.setNetworkType(NetworkType.valueOf(cursor.getString(networkTypeIndex)));
                    remote.setExecutionTime(cursor.getDouble(execTimeIndex));
                    remote.setCommOverhead(cursor.getDouble(commIndex));
                    remote.setRttSpeed(cursor.getLong(rttIndex));
                    remote.setExecutionDate(cursor.getLong(offDateIndex));
                    remote.setExecutionDate(cursor.getInt(completedIndex));

                    taskExecutions.add(remote);
                }
            } else {
                // only fetch the executions where the commOverhead is 0 (Local Execution)
                if (name.equalsIgnoreCase(taskName) && cursor.getDouble(commIndex) == 0) {
                    TaskExecution remote = new TaskExecution();
                    remote.setTaskName(cursor.getString(nameIndex));
                    remote.setExecLocation(ExecutionLocation.valueOf(cursor.getString(locationIndex)));
                    remote.setNetworkType(NetworkType.valueOf(cursor.getString(networkTypeIndex)));
                    remote.setExecutionTime(cursor.getDouble(execTimeIndex));
                    remote.setCommOverhead(cursor.getDouble(commIndex));
                    remote.setRttSpeed(cursor.getLong(rttIndex));
                    remote.setExecutionDate(cursor.getLong(offDateIndex));
                    remote.setExecutionDate(cursor.getInt(completedIndex));

                    taskExecutions.add(remote);
                }
            }
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return taskExecutions;
    }



    public long addMobileDevice(MobileNode device) {

        Log.d(TAG, "inserting mobile device: " + device.getNodeName());

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
        values.put(DBHelper.COL_OFFLOADING_SCORE, device.getOffloadingScore());

        if (!deviceExists(device.getIp())) {
            return db.insert(TABLE_MOBILE_DEVICES, null, values);
        }

        return -1;
    }

    public boolean removeMobileDevice(MobileNode device) {
        int rowsAffected = db.delete(TABLE_MOBILE_DEVICES, DBHelper.COL_DEV_IP + "=?"
                , new String[]{device.getIp()});
        return (rowsAffected > 0);
    }

    public MobileNode getMobileDevice(String senderIP) {
        MobileNode device = null;

        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, DBHelper.COL_DEV_IP + "=?",
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
        int osIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOADING_SCORE);

        while (cursor.moveToNext()) {
            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(cursor.getInt(cpuFreqIndex));
            device.setNumberOfCPUs(cursor.getInt(cpuNumIndex));
            device.setMemoryMB(cursor.getLong(memIndex));
            device.setJoinedDate(cursor.getLong(joinedIndex));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(BatteryState.valueOf(cursor.getString(bsIndex)));
            device.setOffloadingScore(cursor.getInt(osIndex));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return device;
    }

    public ArrayList<MobileNode> getMobileDevicesList() {
        ArrayList<MobileNode> devices =  new ArrayList<>();

        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, null, null,
                null, null, DBHelper.COL_DEV_ID);

        int idIndex = cursor.getColumnIndex(DBHelper.COL_DEV_ID);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int cpuNumIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_NUM);
        int cpuFreqIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_FREQ);
        int memIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MEMORY);
        int joinedIndex = cursor.getColumnIndex(DBHelper.COL_DEV_JOINED);
        int blIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_LEVEL);
        int bsIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_STATE);
        int osIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOADING_SCORE);

        while (cursor.moveToNext()) {
            MobileNode device = new MobileNode(context);

            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(Integer.parseInt(cursor.getString(cpuFreqIndex)));
            device.setNumberOfCPUs(Integer.parseInt(cursor.getString(cpuNumIndex)));
            device.setMemoryMB(Long.parseLong(cursor.getString(memIndex)));
            device.setJoinedDate(Long.parseLong(cursor.getString(joinedIndex)));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(BatteryState.valueOf(cursor.getString(bsIndex)));
            device.setOffloadingScore(cursor.getInt(osIndex));

            devices.add(device);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return devices;
    }

    private boolean deviceExists(String ip) {
        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, DBHelper.COL_DEV_IP + "=?", new
                String[]{ip}, null, null, null);

        return (cursor.getCount() > 0);
    }

    public int clearMobileDevicesTable() {
        return db.delete(TABLE_MOBILE_DEVICES, null, null);
    }

    public int clearOffloadTable() {
        return db.delete(TABLE_OFFLOAD, null, null);
    }

}
