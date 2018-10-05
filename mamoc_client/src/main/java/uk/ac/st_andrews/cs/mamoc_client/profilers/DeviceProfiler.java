package uk.ac.st_andrews.cs.mamoc_client.profilers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import uk.ac.st_andrews.cs.mamoc_client.R;

public class DeviceProfiler {
    public static final String TAG = "DeviceProfiler";

    private final Context context;

    public static int batteryLevel;

    public DeviceProfiler(Context context) {
        this.context = context;
    }

    public static int getNumCpuCores() {
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    // Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]+", file.getName())) {
                        return true;
                    }
                    return false;
                }
            });
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            Log.e(TAG, "Failed to count number of cores, defaulting to 1", e);
            return 1;
        }
    }

    @SuppressLint("StringFormatInvalid")
    public static List<String> getCpuCurFreq(Context mContext) {
        List<String> result = new ArrayList<>();
        int mCpuCoreNumber = getNumCpuCores();
        BufferedReader br = null;

        try {
            for (int i = 0; i < mCpuCoreNumber; i++) {
                final String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq";
                File mFile = new File(path);
                if (mFile.exists()) {
                    br = new BufferedReader(new FileReader(path));
                    String line = br.readLine();
                    if (line != null) {
                        result.add(String.format(mContext.getResources().getString(R.string.cpu_cur_freq), i, line));
                    }
                } else {
                    result.add(String.format(mContext.getResources().getString(R.string.cpu_stopped), i));
                }
                br.close();
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

    public final int getBatteryPercentage() {
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
        }

        return totalMemory;
    }

}
