package uk.ac.standrews.cs.mamoc_client.Model;

import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Date;

import uk.ac.standrews.cs.mamoc_client.Utils.Utils;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;
import uk.ac.standrews.cs.mamoc_client.Profilers.DeviceProfiler;

public class MobileNode extends MamocNode{

    private String deviceID;
    private String osVersion;
    private String manufacturer;
    private int batteryLevel;
    private BatteryState batteryState;

    public MobileNode(Context context) {
        DeviceProfiler deviceProfiler = new DeviceProfiler(context);

//        this.deviceID = deviceProfiler.getDeviceID(context);
        this.osVersion = Build.VERSION.RELEASE;
        this.manufacturer = Build.MANUFACTURER;
        this.batteryLevel = deviceProfiler.getBatteryLevel();
        this.batteryState = deviceProfiler.isDeviceCharging();

//        super.setNodeName(deviceProfiler.getDeviceID(context));
        super.setNumberOfCPUs(deviceProfiler.getNumCpuCores());
        super.setMemoryMB(deviceProfiler.getTotalMemory());
        super.setJoinedDate(new Date().getTime());
        super.setIp(Utils.getIPAddress(true));
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
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

    public void setBatteryState(BatteryState state) {
        this.batteryState = state;
    }

    public static MobileNode fromJSON(String deviceJSON) {
        Gson gson = new Gson();
        MobileNode mobile = null;
        try {
            mobile = gson.fromJson(deviceJSON, MobileNode.class);
        } catch (IllegalStateException | JsonSyntaxException exception) {
            exception.printStackTrace();
        }

        return mobile;
    }

    @Override
    public String toString() {
        return (new Gson()).toJson(this);
    }
}
