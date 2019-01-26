package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.TaskExecution;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import static uk.ac.standrews.cs.mamoc_client.Constants.AVAIALABILITY_PRICE;
import static uk.ac.standrews.cs.mamoc_client.Constants.AVAILABLITY_SECURITY;
import static uk.ac.standrews.cs.mamoc_client.Constants.BANDWIDTH_AVAILABILITY;
import static uk.ac.standrews.cs.mamoc_client.Constants.BANDWIDTH_PRICE;
import static uk.ac.standrews.cs.mamoc_client.Constants.BANDWIDTH_SECURITY;
import static uk.ac.standrews.cs.mamoc_client.Constants.BANDWIDTH_SPEED;
import static uk.ac.standrews.cs.mamoc_client.Constants.IMPORTANCE_HIGH;
import static uk.ac.standrews.cs.mamoc_client.Constants.IMPORTANCE_LOW;
import static uk.ac.standrews.cs.mamoc_client.Constants.IMPORTANCE_MEDIUM;
import static uk.ac.standrews.cs.mamoc_client.Constants.SECURITY_PRICE;
import static uk.ac.standrews.cs.mamoc_client.Constants.SPEED_AVAILABILITY;
import static uk.ac.standrews.cs.mamoc_client.Constants.SPEED_PRICE;
import static uk.ac.standrews.cs.mamoc_client.Constants.SPEED_SECURITY;

public class DecisionEngine {

    private static final String TAG = "DecisionEngine";

    private static DecisionEngine instance;
    // TODO: set these values dynamically based on past offloading data and number of checks performed
    private final int MAX_REMOTE_EXECUTIONS = 5;
    private final int MAX_LOCAL_EXECUTIONS = 5;

    // AHP variables
    private int criteria = 5;
    private String labels[] = {"Bandwidth", "Speed", "Availability", "Security", "Price"};
    private String sites[] = {"Mobile", "Edge", "Public"};
    private Double[] ahpWeights = new Double[labels.length];
    private HashMap<MamocNode, Double> nodeScores = new HashMap<>();

    private Context mContext;
    private MamocFramework framework;

    private DecisionEngine(Context context) {
        this.mContext = context;
        framework = MamocFramework.getInstance(mContext);
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

        Log.d(TAG, "making offloading decision for: " + taskName);

        calculateAHP();

        int localExecs, remoteExecs;

        ArrayList<TaskExecution> remoteTaskExecutions = framework.dbAdapter.getExecutions(taskName, true);
        remoteExecs = remoteTaskExecutions.size();

        ArrayList<TaskExecution> localTaskExecutions = framework.dbAdapter.getExecutions(taskName, false);
        localExecs = localTaskExecutions.size();

        MobileNode selfNode = MamocFramework.getInstance(mContext).getSelfNode();

        // check for 5 consecutive local executions
        if (localExecs == MAX_LOCAL_EXECUTIONS){
            localExecs = 0;
            if (getNodeWithMaxOffloadingScore() == selfNode) {
                return ExecutionLocation.LOCAL;
            }
        }

        // more than 5 remote executions of the task - let's recalculate offloading scores
        // and double check if it is still worth offloading
        if (remoteExecs == MAX_REMOTE_EXECUTIONS ){
            remoteExecs = 0;

            // if there are no local executions, at least have one without calculating its offloading score
            if (localExecs == 0){
                return ExecutionLocation.LOCAL;
            } else {
                //    selfNode.setOffloadingScore(profileNode(selfNode));
                MamocNode maxNode = getNodeWithMaxOffloadingScore();

                remoteExecs++;
                if (maxNode instanceof MobileNode) {
                    return ExecutionLocation.D2D;
                } else if (maxNode instanceof EdgeNode) {
                    return ExecutionLocation.EDGE;
                } else if (maxNode instanceof CloudNode) {
                    return ExecutionLocation.PUBLIC_CLOUD;
                }
                //   }
            }
        }

        // check if the task has previously been offloaded, if not, let's do some remote offloading
        // to record their execution times
        if (remoteExecs == 0) {

            Log.d(TAG, "remote executions exist");

            TreeSet<MobileNode> mobileNodes = MamocFramework.getInstance(mContext).commController.getMobileDevices();
            if (mobileNodes.size() > 0) {
                // check if the nearby device has a higher offloading score than me
                for (MobileNode node : mobileNodes) {
                    if (node.getOffloadingScore() > selfNode.getOffloadingScore()) {
                        Log.d(TAG, "selecting nearby");
                        return ExecutionLocation.D2D;
                    }
                }
            } else if (MamocFramework.getInstance(mContext).commController.getEdgeDevices().size() > 0) {
                Log.d(TAG, "selecting edge");
                return ExecutionLocation.EDGE;
            } else if (MamocFramework.getInstance(mContext).commController.getCloudDevices().size() > 0) {
                Log.d(TAG, "selecting public cloud");
                return ExecutionLocation.PUBLIC_CLOUD;
            }
            // if no remote resources are available then we just revert back to local execution
            else {
                return ExecutionLocation.LOCAL;
            }
        }

        return ExecutionLocation.LOCAL;
    }


    private ArrayList<Double> profileNode(MamocNode node) {

        double cpu, mem, rtt, battery;
        ArrayList<Double> siteCriteria = new ArrayList<>();

        MobileNode selfNode = MamocFramework.getInstance(mContext).getSelfNode();

//        if (node.getNodeName().equals("SelfNode")) {
//            return 1.0;
//        }
        // nearby mobile device
        if (node instanceof MobileNode) {

            // calculate RTT values for connected nearby mobile devices
            rtt = framework.networkProfiler.measureRtt(node.getIp(), node.getPort());
            if (rtt < 100) {
                siteCriteria.add(IMPORTANCE_HIGH);
            } else if (rtt < 200){
                siteCriteria.add(IMPORTANCE_MEDIUM);
            } else {
                siteCriteria.add(IMPORTANCE_LOW);
            }

            // compare cpu and mem of self node and nearby mobile devices
            cpu = framework.deviceProfiler.getTotalCpuFreq(mContext);
            mem = node.getMemoryMB();

            if (cpu > selfNode.getCpuFreq() && mem > selfNode.getMemoryMB()){
                siteCriteria.add(IMPORTANCE_HIGH);
            } else if (cpu > selfNode.getCpuFreq() || mem > selfNode.getMemoryMB()){
                siteCriteria.add(IMPORTANCE_MEDIUM);
            } else {
                siteCriteria.add(IMPORTANCE_LOW);
            }

            // check the battery level for availability
            BatteryState state = framework.deviceProfiler.isDeviceCharging();
            if (state == BatteryState.CHARGING) {
                battery = 100;
            } else {
                battery = (100 - framework.deviceProfiler.getBatteryLevel());
            }

            if (battery > 80) {
                siteCriteria.add(IMPORTANCE_HIGH);
            } else if (battery < 20) {
                siteCriteria.add(IMPORTANCE_LOW);
            } else {
                siteCriteria.add(IMPORTANCE_MEDIUM);
            }

            siteCriteria.add(IMPORTANCE_HIGH); // high security
            siteCriteria.add(-IMPORTANCE_LOW); // low price

        }
        // edge or public cloud
        else {
            // TODO: get resource monitoring data from the server and set the importance accordingly
            // Edge device
            if (node.getIp().startsWith("192")) { // DIRTY HACK
                siteCriteria.add(IMPORTANCE_HIGH);
                siteCriteria.add(IMPORTANCE_HIGH);
                siteCriteria.add(IMPORTANCE_MEDIUM);
                siteCriteria.add(IMPORTANCE_HIGH);
                siteCriteria.add(-IMPORTANCE_MEDIUM);
            }
            // Public cloud instance
            else {
                siteCriteria.add(IMPORTANCE_MEDIUM);
                siteCriteria.add(IMPORTANCE_HIGH);
                siteCriteria.add(IMPORTANCE_HIGH);
                siteCriteria.add(IMPORTANCE_LOW);
                siteCriteria.add(-IMPORTANCE_HIGH);
            }
        }

        return siteCriteria;
    }

    private MamocNode getNodeWithMaxOffloadingScore() {
        MobileNode selfNode = framework.getSelfNode();
        TreeSet<MobileNode> mobileNodes = framework.commController.getMobileDevices();
        TreeSet<EdgeNode> edgeNodes = framework.commController.getEdgeDevices();
        TreeSet<CloudNode> cloudNodes = framework.commController.getCloudDevices();

        HashMap<MamocNode, ArrayList<Double>> availableSites = new HashMap<>();

        MamocNode maxNode = selfNode;

        for (MobileNode node : mobileNodes) {
            ArrayList<Double> criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        for (EdgeNode node : edgeNodes) {
            ArrayList<Double> criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        for (CloudNode node : cloudNodes) {
            ArrayList<Double> criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        performEvaluation(availableSites);

        for (Map.Entry<MamocNode,Double> entry: nodeScores.entrySet()) {
            if (entry.getValue() > maxNode.getOffloadingScore()){
                maxNode =entry.getKey();
            }
        }

        return maxNode;
    }

    private void calculateAHP(){

        AHP ahp = new AHP(criteria);

        double compArray[] = ahp.getPairwiseComparisonArray();

        // Set the pairwise comparison values
        compArray[0] = BANDWIDTH_SPEED;
        compArray[1] = BANDWIDTH_AVAILABILITY;
        compArray[2] = BANDWIDTH_SECURITY;
        compArray[3] = BANDWIDTH_PRICE;
        compArray[4] = SPEED_AVAILABILITY;
        compArray[5] = SPEED_SECURITY;
        compArray[6] = SPEED_PRICE;
        compArray[7] = AVAILABLITY_SECURITY;
        compArray[8] = AVAIALABILITY_PRICE;
        compArray[9] = SECURITY_PRICE;

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            Log.d(TAG, "Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            Log.d(TAG, labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            Log.d(TAG, String.valueOf(ahp.getPairwiseComparisonArray()[i]));
        }

        Log.d(TAG, "Consistency Index: " + ahp.getConsistencyIndex());
        Log.d(TAG, "Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        Log.d(TAG, "Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            ahpWeights[k] = ahp.getWeights()[k];
            Log.d(TAG, labels[k] + ": " + ahp.getWeights()[k]);
        }
    }

    private void performEvaluation(HashMap<MamocNode, ArrayList<Double>> availableSites) {
        // L = IMPORTANCE_LOW, M = IMPORTANCE_MEDIUM , H = IMPORTANCE_HIGH
        Array2DRowRealMatrix sitesM = new Array2DRowRealMatrix(availableSites.size(), labels.length);
        int i = 0;
        for (Map.Entry<MamocNode,ArrayList<Double>> entry: availableSites.entrySet()) {
            for (int j = 0; j < criteria; j++) {
                sitesM.setEntry(i, j, entry.getValue().get(j));

                // Old approach: setting importance statically
//                // Mobile
//                sitesM.setEntry(0, 0, IMPORTANCE_HIGH); // bandwidth
//                sitesM.setEntry(0, 1, IMPORTANCE_MEDIUM); // speed
//                sitesM.setEntry(0, 2, IMPORTANCE_LOW); // availability
//                sitesM.setEntry(0, 3, IMPORTANCE_HIGH); // security
//                sitesM.setEntry(0, 4, -IMPORTANCE_LOW); // price (which is cost)
//
//                // Edge
//                sitesM.setEntry(1, 0, IMPORTANCE_HIGH);
//                sitesM.setEntry(1, 1, IMPORTANCE_HIGH);
//                sitesM.setEntry(1, 2, IMPORTANCE_MEDIUM);
//                sitesM.setEntry(1, 3, IMPORTANCE_HIGH);
//                sitesM.setEntry(1, 4, -IMPORTANCE_MEDIUM);
//
//                // Public
//                sitesM.setEntry(2, 0, IMPORTANCE_MEDIUM);
//                sitesM.setEntry(2, 1, IMPORTANCE_HIGH);
//                sitesM.setEntry(2, 2, IMPORTANCE_HIGH);
//                sitesM.setEntry(2, 3, IMPORTANCE_LOW);
//                sitesM.setEntry(2, 4, -IMPORTANCE_HIGH);
            }
        }

        Double nodeScore, totalScore = 0.0;

        for (int k=0; k < sitesM.getRowDimension();k++){
            nodeScore = 0.0; // reset for new node
            Log.d(TAG, "calculating for " + availableSites.keySet().toArray()[k]);

            for (int j=0; j < sitesM.getColumnDimension(); j++){
                Log.d(TAG, labels[j] + " criteria weight: " + sitesM.getEntry(i,j) * ahpWeights[j]);
                nodeScore += sitesM.getEntry(i,j) * ahpWeights[j];
            }

            nodeScores.put((MamocNode) availableSites.keySet().toArray()[k], nodeScore);
            totalScore += nodeScore;
            Log.d(TAG, ((MamocNode) availableSites.keySet().toArray()[k]).getNodeName() +
                    " score: " + nodeScores.get(availableSites.keySet().toArray()[k]));
        }

//        for (int i = 0; i < nodeScores.size(); i++) {
//            Log.d(TAG, sites[i] + " score percentage: " + (nodeScores.get(sites[i])/ totalScore) * 100);
//        }
    }
}
