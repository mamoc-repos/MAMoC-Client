package uk.ac.standrews.cs.mamoc_client.Execution;

import android.content.Context;

import java.util.ArrayList;
import java.util.TreeSet;

import uk.ac.standrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.RemoteExecution;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;
import uk.ac.standrews.cs.mamoc_client.Profilers.DeviceProfiler;
import uk.ac.standrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkProfiler;

import static uk.ac.standrews.cs.mamoc_client.Constants.BATTERY_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.CPU_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.MEMORY_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.RTT_WEIGHT;

public class DecisionEngine {

    private static DecisionEngine instance;
    Context mContext;

    private MamocFramework framework;
    private NetworkProfiler netProfiler;
    private DeviceProfiler devProfiler;
    private DBAdapter db;

    // TODO: set these values dynamically based on past offloading data
    private final int MAX_REMOTE_EXECUTIONS = 5;
    private final int MAX_LOCAL_EXECUTIONS = 5;

    private DecisionEngine(Context context) {
        this.mContext = context;
        framework = MamocFramework.getInstance(mContext);
        netProfiler = framework.networkProfiler;
        devProfiler = framework.deviceProfiler;
        db = DBAdapter.getInstance(mContext);
    }

    public static DecisionEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (DecisionEngine.class) {
                if (instance == null) {
                    instance = new DecisionEngine(context);
                }
            }
        }
        return instance;
    }


    public ExecutionLocation makeDecision(String taskName, Boolean isParallel) {

        MobileNode selfNode = MamocFramework.getInstance(mContext).getSelfNode();

        ArrayList<RemoteExecution> remoteExecutions = db.getRemoteExecutions(taskName);
        // check if task has previously been offloaded
        if (remoteExecutions.size() > 0 && remoteExecutions.size() <= MAX_REMOTE_EXECUTIONS) {
            TreeSet<MobileNode> mobileNodes = MamocFramework.getInstance(mContext).commController.getMobileDevices();
            if (mobileNodes.size() > 0) {
                // check if the nearby device has a higher offloading score than me
                for (MobileNode node : mobileNodes) {
                    if (node.getOffloadingScore() > selfNode.getOffloadingScore()) {
                        return ExecutionLocation.D2D;
                    }
                }
            } else if (MamocFramework.getInstance(mContext).commController.getEdgeDevices().size() > 0) {
                return ExecutionLocation.EDGE;
            } else if (MamocFramework.getInstance(mContext).commController.getCloudDevices().size() > 0) {



            } else {
                return ExecutionLocation.LOCAL;
            }
            // more than 5 remote executions of the task - let's recalculate offloading scores
            // and double check if it is still worth offloading
        } else {
            selfNode.setOffloadingScore(calculateOffloadingScore(selfNode));
            MamocNode maxNode = getNodeWithMaxOffloadingScore();

            if (selfNode.getOffloadingScore() > maxNode.getOffloadingScore()) {
                return ExecutionLocation.LOCAL;
            } else {
                if (maxNode instanceof MobileNode) {
                    return ExecutionLocation.D2D;
                } else if (maxNode instanceof EdgeNode) {
                    return ExecutionLocation.EDGE;
                } else if (maxNode instanceof CloudNode) {
                    return ExecutionLocation.PUBLIC_CLOUD;
                }
            }
        }

        return ExecutionLocation.LOCAL;
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
                batteryWeight = (100 - devProfiler.getBatteryLevel()) * BATTERY_WEIGHT;
            }

            // calculate different RTT values for self node or connected nearby node
            if (mNode.getNodeName().equals("SelfNode")) {
                rttWeight = 1.0 * RTT_WEIGHT; // for local execution
            } else {
                rttWeight = netProfiler.measureRtt(mNode.getIp(), mNode.getPort()) * RTT_WEIGHT;
            }
        } else {




        }

        return cpuWeight * memWeight * rttWeight * batteryWeight;
    }

    public MamocNode getNodeWithMaxOffloadingScore() {
        MobileNode selfNode = framework.getSelfNode();
        TreeSet<MobileNode> mobileNodes = framework.commController.getMobileDevices();
        TreeSet<EdgeNode> edgeNodes = framework.commController.getEdgeDevices();
        TreeSet<CloudNode> cloudNodes = framework.commController.getCloudDevices();

        MamocNode maxNode = selfNode;

        if (mobileNodes.size() > 0) {
            for (MobileNode node : mobileNodes) {
                if (node.getOffloadingScore() > maxNode.getOffloadingScore())
                    maxNode = node;
            }
        }

        if (edgeNodes.size() > 0) {
            for (EdgeNode node : edgeNodes) {
                if (node.getOffloadingScore() > maxNode.getOffloadingScore())
                    maxNode = node;
            }
        }

        if (cloudNodes.size() > 0) {
            for (CloudNode node : cloudNodes) {
                if (node.getOffloadingScore() > maxNode.getOffloadingScore())
                    maxNode = node;
            }
        }

        return maxNode;

    }
}
