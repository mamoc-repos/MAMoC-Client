package uk.ac.standrews.cs.mamoc_client.Execution;

import android.content.Context;
import android.util.Log;

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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import static uk.ac.standrews.cs.mamoc_client.Constants.BATTERY_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.CPU_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.MEMORY_WEIGHT;
import static uk.ac.standrews.cs.mamoc_client.Constants.RTT_WEIGHT;

public class DecisionEngine {

    private static final String TAG = "DecisionEngine";

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


    ExecutionLocation makeDecision(String taskName, Boolean isParallel) {

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
                return ExecutionLocation.PUBLIC_CLOUD;
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


    private double calculateOffloadingScore(MamocNode node) {

        double cpuWeight = 0, memWeight = 0, rttWeight = 0, batteryWeight = 0;
        ArrayList<double[]> bbt = new ArrayList<>();

        if (node instanceof MobileNode) {
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

        double[] deviceValues = new double[4];
        deviceValues[0] = cpuWeight;
        deviceValues[1] = memWeight;
        deviceValues[2] = batteryWeight;
        deviceValues[3] = rttWeight;
        bbt.add(deviceValues);

        double[][] array = new double[bbt.size()][];
        for (int i = 0; i < bbt.size(); i++) {
            double[] row = bbt.get(i);
            array[i] = row;
        }

        return calculateAHP(array);
    }

    private MamocNode getNodeWithMaxOffloadingScore() {
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

    private double calculateAHP(double[][] bbt){

        int dim = bbt.length;
        EigenDecomposition evd2;
        evd2 = new EigenDecomposition(
                new Array2DRowRealMatrix(bbt), 0);
        double[] eigenvalues = evd2.getRealEigenvalues();

        RealMatrix uHatrm = evd2.getV();
        double[][] uHat = new double[dim][];
        for (int i = 0; i < dim; i++) {
            uHat[i] = uHatrm.getRow(i);
        }
        int nrV = 4;
        double RI[] = {0.0, 0.0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

        double[][] matrix = new double[nrV][];
        for (int i = 0; i < nrV; i++) {
            matrix[i] = new double[nrV];
        }

        // diagonal
        for (int i = 0; i < nrV; i++) {
            matrix[i][i] = 1.0;
        }
        matrix[0][1] = 4;
        matrix[0][2] = 3;
        matrix[0][3] = 7;
        matrix[1][2] = 1.0 / 3.0;
        matrix[1][3] = 3.0;
        matrix[2][3] = 5.0;

        // (i,k) is 1/(k,i)
        for (int k = 0; k < nrV; k++) {
            for (int i = 0; i < nrV; i++) {
                matrix[i][k] = 1.0 / matrix[k][i];
            }
        }

        for (int k = 0; k < nrV; k++) {
            for (int i = 0; i < nrV; i++) {
                System.out.print(matrix[k][i] + "    ");
            }
            Log.d(TAG,"");
        }

        EigenDecomposition evd = new EigenDecomposition(new Array2DRowRealMatrix(matrix), 0);

        double sum = 0;
        for (int i = 0; i < 1; i++) {
            RealVector v = evd.getEigenvector(i);
            for (double d : v.toArray()) {
                sum += d;
            }
            //Log.d(TAG,(sum);
            for (double xx : v.toArray()) {
                Log.d(TAG,(xx / sum + "; "));
            }
            Log.d(TAG,"");
        }

        int evIdx = 0;
        Log.d(TAG,("\nEigenvalues"));
        for (int i = 0; i < evd.getRealEigenvalues().length; i++) {
            Log.d(TAG, String.valueOf((evd.getRealEigenvalues()[i])));
            evIdx = (evd.getRealEigenvalue(i) > evd.getRealEigenvalue(evIdx)) ? i : evIdx;
        }
        Log.d(TAG,("\n\nMax Eigenvalue = " + evd.getRealEigenvalue(evIdx)));

        double ci = (evd.getRealEigenvalue(evIdx) - (double) nrV) / (double) (nrV - 1);
        Log.d(TAG,("\nConsistency Index: " + ci));

        Log.d(TAG,("\nConsistency Ratio: " + ci / RI[nrV] * 100 + "%"));

        return ci;
    }
}
