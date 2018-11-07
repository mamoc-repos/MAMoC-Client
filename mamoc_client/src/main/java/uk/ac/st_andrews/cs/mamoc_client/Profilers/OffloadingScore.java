package uk.ac.st_andrews.cs.mamoc_client.Profilers;

import android.content.Context;

import uk.ac.st_andrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.BATTERY_WEIGHT;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.CPU_WEIGHT;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.MEMORY_WEIGHT;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.RTT_WEIGHT;

public class OffloadingScore {

    Context mContext;
    NetworkProfiler netProfiler;
    DeviceProfiler devProfiler;

    public OffloadingScore(Context context) {
        mContext = context;
        netProfiler = new NetworkProfiler(context);
        devProfiler = new DeviceProfiler(context);
    }

    public double calculateOffloadingScore(MamocNode node){

        double cpuWeight = 0, memWeight = 0, rttWeight = 0, batteryWeight = 0;

        if (node instanceof MobileNode){
            MobileNode mNode = (MobileNode) node;

            cpuWeight = devProfiler.getTotalCpuFreq(mContext) * CPU_WEIGHT;
            memWeight = mNode.getMemoryMB() * MEMORY_WEIGHT;

            BatteryState state = devProfiler.isDeviceCharging();
            if (state == BatteryState.CHARGING) {
                batteryWeight = 1 * BATTERY_WEIGHT;
            } else {
                batteryWeight = (100 - devProfiler.getBatteryPercentage()) * BATTERY_WEIGHT;
            }

            // calculate different RTT values for self node or connected nearby node
            if (mNode.getDeviceID().equals(devProfiler.getDeviceID(mContext))) {
                rttWeight = 1.0 * RTT_WEIGHT; // for local execution
            } else {
                rttWeight = netProfiler.measureRtt(mNode.getIp(), mNode.getPort()) * RTT_WEIGHT;
            }
        } else {



        }

        return cpuWeight * memWeight * rttWeight * batteryWeight;
    }
}
