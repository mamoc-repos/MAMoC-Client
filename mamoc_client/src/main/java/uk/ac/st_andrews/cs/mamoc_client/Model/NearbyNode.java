package uk.ac.st_andrews.cs.mamoc_client.Model;

import android.content.Context;
import android.os.Build;

import java.util.Date;

import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;
import uk.ac.st_andrews.cs.mamoc_client.profilers.BatteryState;
import uk.ac.st_andrews.cs.mamoc_client.profilers.DeviceProfiler;

public class NearbyNode extends MamocNode{

    private String osVersion;
    private String manufacturer;
    private int batteryLevel;
    private BatteryState batteryState;

    public NearbyNode(Context context) {
        DeviceProfiler deviceProfiler = new DeviceProfiler(context);
        super.setNodeName(deviceProfiler.getDeviceID(context));
        super.setNumberOfCPUs(deviceProfiler.getNumCpuCores());
        super.setMemoryMB(deviceProfiler.getTotalMemory());
        super.setJoinedDate(new Date().getTime());
        super.setIp(Utils.getLocalIpAddress(context));

        this.osVersion = Build.VERSION.RELEASE;
        this.manufacturer = Build.MANUFACTURER;
        this.batteryLevel = deviceProfiler.getBatteryPercentage();
        this.batteryState = deviceProfiler.isDeviceCharging();
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public BatteryState getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(BatteryState batteryState) {
        this.batteryState = batteryState;
    }

}
