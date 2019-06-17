package uk.ac.standrews.cs.mamoc_client.Profilers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;


public class DeviceProfiler {
    public static final String TAG = "DeviceProfiler";

    private final Context context;

    public DeviceProfiler(Context context) {
        this.context = context;
    }

//    @SuppressLint({"HardwareIds", "MissingPermission"})
//    public String getDeviceID(Context context){
//        TelephonyManager mngr = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
//        String IMEI = mngr.getDeviceId();
//        String device_unique_id = Settings.Secure.getString(context.getContentResolver(),
//                Settings.Secure.ANDROID_ID);
//        return device_unique_id;
//    }

    public double getTotalCpuFreq(Context mContext) {
        double result = 0;
        int mCpuCoreNumber = getNumCpuCores();
        BufferedReader br = null;

        try {
            for (int i = 0; i < mCpuCoreNumber; i++) {
                final String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                File mFile = new File(path);
                if (mFile.exists()) {
                    br = new BufferedReader(new FileReader(path));
                    String line = br.readLine();
                    if (line != null) {
                        result += Double.parseDouble(line);
                    }
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public int getNumCpuCores() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(file -> Pattern.matches("cpu[0-9]+", file.getName()));
            return files.length;
        } catch (Exception e) {
            Log.e(TAG, "Failed to count number of cores, defaulting to 1", e);
            return 1;
        }
    }

    public final int getBatteryLevel() {
        int percentage = 0;
        Intent batteryStatus = getBatteryStatusIntent();
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            percentage = (int) ((level / (float) scale) * 100);
        }

        return percentage;
    }

    private Intent getBatteryStatusIntent() {
        IntentFilter batFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return context.registerReceiver(null, batFilter);
    }

    public final BatteryState isDeviceCharging() {
        Intent batteryStatus = getBatteryStatusIntent();
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING  || status == BatteryManager.BATTERY_STATUS_FULL){
            return BatteryState.CHARGING;
        } else{
            return BatteryState.NOT_CHARGING;
        }
    }

    public final long getTotalMemory() {
        long totalMemory = 0;

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        if (activityManager != null) {
            activityManager.getMemoryInfo(mi);
            totalMemory = mi.totalMem;
            Log.i(TAG, "total memory: " + totalMemory);
        }

        return totalMemory;
    }

    public final long getAvailMemory() {

        long freeMem = 0;

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            activityManager.getMemoryInfo(mi);
            freeMem = mi.availMem;
            Log.i(TAG, "free memory: " + freeMem);
        }
        return freeMem;
    }
}