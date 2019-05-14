package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.TaskExecution;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;

public class DecisionEngine {

    private static final String TAG = "DecisionEngine";

    private static DecisionEngine instance;
    // TODO: set these values dynamically based on past offloading data and number of checks performed
    private final int MAX_REMOTE_EXECUTIONS = 5;
    private final int MAX_LOCAL_EXECUTIONS = 5;

    private Context mContext;
    private MamocFramework framework;

    private AHP ahp;
    private Topsis topsis;

    private DecisionEngine(Context context) {
        this.mContext = context;
        framework = MamocFramework.getInstance(context);
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

        int localExecs, remoteExecs;

        ArrayList<TaskExecution> remoteTaskExecutions = framework.dbAdapter.getExecutions(taskName, true);
        remoteExecs = remoteTaskExecutions.size();

        Log.d(TAG, "Remote execs: " + remoteExecs);

        ArrayList<TaskExecution> localTaskExecutions = framework.dbAdapter.getExecutions(taskName, false);
        localExecs = localTaskExecutions.size();

        Log.d(TAG, "Local execs: " + localExecs);
        Log.d(TAG, "Last Execution: " + framework.lastExecution);

        // more than 5 local executions of the task - let's check if local is still better
        if (localExecs % MAX_LOCAL_EXECUTIONS == 0 && framework.lastExecution.equals("Local")){
            Log.d(TAG, "MAX LOCAL EXECUTIONS REACHED");
            return evaluateRemoteSites();
        }

        // more than 5 remote executions of the task - let's recalculate offloading scores
        // and double check if it is still worth offloading
        if (remoteExecs % MAX_REMOTE_EXECUTIONS == 0 && framework.lastExecution.equals("Remote")){
            Log.d(TAG, "MAX REMOTE EXECUTIONS REACHED");

            double localExecTime = 0;
            double remoteExecTime = 0;

            if (localExecs != 0) {
                Log.d(TAG, "The last executed local execution time: " + localTaskExecutions.get(localExecs-1).getExecutionTime());
                localExecTime = localTaskExecutions.get(localExecs-1).getExecutionTime();
            }

            if (remoteExecs != 0) {
                Log.d(TAG, "The last executed remote execution time: " + remoteTaskExecutions.get(remoteExecs - 1).getExecutionTime());
                remoteExecTime = remoteTaskExecutions.get(remoteExecs - 1).getExecutionTime();
            }

            // Compare the last local execution with the last remote execution
            // OR if there is no local Execution at all
            if (localExecTime < remoteExecTime || localExecs == 0){
                framework.lastExecution = "Local";
                return ExecutionLocation.LOCAL;
            } else {
                return evaluateRemoteSites();
            }
        }

        // check if the task has previously been offloaded, if not, let's do some remote offloading
        // to record their execution times
        if (remoteExecs == 0) {
            Log.d(TAG, "No remote executions exist");

            if (evaluateRemoteSites() != null) {
                return evaluateRemoteSites();
            }
            // if no remote resources are available then we just revert back to local execution
            else {
                return ExecutionLocation.LOCAL;
            }
        }

        // For all other normal remote executions
        if (remoteExecs % MAX_REMOTE_EXECUTIONS != 0 && framework.lastExecution.equals("Remote")) {
            return evaluateRemoteSites();
        }

        // For all other normal local executions
        return ExecutionLocation.LOCAL;
    }

    private ExecutionLocation evaluateRemoteSites(){
        framework.lastExecution = "Remote";
        MamocNode maxNode = getNodeWithMaxOffloadingScore();

        if (maxNode instanceof MobileNode) {
            return ExecutionLocation.D2D;
        } else if (maxNode instanceof EdgeNode) {
            return ExecutionLocation.EDGE;
        } else if (maxNode instanceof CloudNode) {
            return ExecutionLocation.PUBLIC_CLOUD;
        } else {
            // This should not happen!
            return ExecutionLocation.LOCAL;
        }
    }

    private ArrayList<Fuzzy> profileNode(MamocNode node) {

        Log.d(TAG, "Profiling " + node.getNodeName() + " - " + node.getIp());

        double cpu, mem, rtt, battery;
        ArrayList<Fuzzy> siteCriteria = new ArrayList<>();

        MobileNode selfNode = framework.getSelfNode();

        // nearby mobile device
        if (node instanceof MobileNode) {

            // calculate RTT values for connected nearby mobile devices
            rtt = framework.networkProfiler.measureRtt(node.getIp(), node.getPort());
            if (rtt < 50) {
                siteCriteria.add(Fuzzy.VERY_HIGH);
            } else if (rtt < 100) {
                siteCriteria.add(Fuzzy.HIGH);
            } else if (rtt < 200) {
                siteCriteria.add(Fuzzy.GOOD);
            } else if (rtt < 300){
                siteCriteria.add(Fuzzy.LOW);
            } else {
                siteCriteria.add(Fuzzy.VERY_LOW);
            }

            // compare cpu and mem of self node and nearby mobile devices
            cpu = framework.deviceProfiler.getTotalCpuFreq(mContext);
            mem = node.getMemoryMB();

            // Three fold speedup
            if (cpu > (selfNode.getCpuFreq() * 3)  && mem > selfNode.getMemoryMB()) {
                siteCriteria.add(Fuzzy.VERY_HIGH);
            }
            // Two fold speedup
            else if (cpu > (selfNode.getCpuFreq() * 2) && mem > selfNode.getMemoryMB()) {
                siteCriteria.add(Fuzzy.HIGH);
            }
            // Slightly better
            else if (cpu > selfNode.getCpuFreq() && mem > selfNode.getMemoryMB()){
                siteCriteria.add(Fuzzy.GOOD);
            }
            // Worse
            else if (cpu < selfNode.getCpuFreq() && mem < selfNode.getMemoryMB()){
                siteCriteria.add(Fuzzy.VERY_LOW);
            } else {
                siteCriteria.add(Fuzzy.LOW);
            }

            // check the battery level for availability
            BatteryState state = framework.deviceProfiler.isDeviceCharging();
            if (state == BatteryState.CHARGING) {
                battery = 100;
            } else {
                battery = (100 - framework.deviceProfiler.getBatteryLevel());
            }

            if (battery > 90) {
                siteCriteria.add(Fuzzy.VERY_HIGH);
            } else if (battery > 80) {
                siteCriteria.add(Fuzzy.HIGH);
            } else if (battery > 50) {
                siteCriteria.add(Fuzzy.GOOD);
            } else if (battery < 20) {
                siteCriteria.add(Fuzzy.VERY_LOW);
            } else {
                siteCriteria.add(Fuzzy.LOW);
            }

            siteCriteria.add(Fuzzy.HIGH); // high security
            siteCriteria.add(Fuzzy.VERY_LOW); // low price
        }
        // edge or public cloud
        else {
            // TODO: get resource monitoring data from the server and set the importance accordingly
            // Edge device
            if (node.getIp().startsWith("192")) { // DIRTY HACK
                siteCriteria.add(Fuzzy.VERY_HIGH);
                siteCriteria.add(Fuzzy.HIGH);
                siteCriteria.add(Fuzzy.HIGH);
                siteCriteria.add(Fuzzy.HIGH);
                siteCriteria.add(Fuzzy.LOW);
            }
            // Public cloud instance
            else {
                siteCriteria.add(Fuzzy.LOW);
                siteCriteria.add(Fuzzy.VERY_HIGH);
                siteCriteria.add(Fuzzy.VERY_HIGH);
                siteCriteria.add(Fuzzy.GOOD);
                siteCriteria.add(Fuzzy.VERY_HIGH);
            }
        }

        return siteCriteria;
    }

    private MamocNode getNodeWithMaxOffloadingScore() {
        TreeSet<MobileNode> mobileNodes = framework.commController.getMobileDevices();
        TreeSet<EdgeNode> edgeNodes = framework.commController.getEdgeDevices();
        TreeSet<CloudNode> cloudNodes = framework.commController.getCloudDevices();

        HashMap<MamocNode, ArrayList<Fuzzy>> availableSites = new HashMap<>();
        ArrayList<Fuzzy> criteriaImportance;

        for (MobileNode node : mobileNodes) {
            Log.d(TAG, "Mobile node: " + node.getNodeName() + " " + node.getIp());
            criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        for (EdgeNode node : edgeNodes) {
            Log.d(TAG, "Edge node: " + node.getNodeName() + " " + node.getIp());
            criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        for (CloudNode node : cloudNodes) {
            Log.d(TAG, "Cloud node: " + node.getNodeName() + " " + node.getIp());
            criteriaImportance = profileNode(node);
            availableSites.put(node, criteriaImportance);
        }

        Log.d(TAG, "Available offloading sites: " + availableSites.size());

        // In case only one offloading site is available
        if (availableSites.size() == 1){
            return availableSites.entrySet().iterator().next().getKey();
        }

        // Calculate the weighted decision matrix
        TreeMap<MamocNode, Double> ranking = performEvaluation(availableSites);

        // Simulate a low offloading score node
        MamocNode maxNode = new MamocNode();
        maxNode.setOffloadingScore(0);

        for (Map.Entry<MamocNode,Double> entry: ranking.entrySet()) {
            if (entry.getValue() > maxNode.getOffloadingScore()){
                maxNode =entry.getKey();
            }
        }

        return maxNode;
    }

    private void calculateAHP(){

        ahp = new AHP(Config.criteria);

        double compArray[] = ahp.getPairwiseComparisonArray();

        // Set the pairwise comparison values
        compArray[0] = Config.BANDWIDTH_SPEED;
        compArray[1] = Config.BANDWIDTH_AVAILABILITY;
        compArray[2] = Config.BANDWIDTH_SECURITY;
        compArray[3] = Config.BANDWIDTH_PRICE;
        compArray[4] = Config.SPEED_AVAILABILITY;
        compArray[5] = Config.SPEED_SECURITY;
        compArray[6] = Config.SPEED_PRICE;
        compArray[7] = Config.AVAILABLITY_SECURITY;
        compArray[8] = Config.AVAIALABILITY_PRICE;
        compArray[9] = Config.SECURITY_PRICE;

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            Log.d(TAG, "Importance of " + Config.criteria[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            Log.d(TAG, Config.criteria[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            Log.d(TAG, String.valueOf(ahp.getPairwiseComparisonArray()[i]));
        }

        Log.d(TAG, "Consistency Index: " + Config.df.format(ahp.getConsistencyIndex()));
        Log.d(TAG, "Consistency Ratio: " + Config.df.format(ahp.getConsistencyRatio()) + "%");
        Log.d(TAG, "Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            Config.ahpWeights[k] = ahp.getWeights()[k];
            Log.d(TAG, Config.criteria[k] + ": " + Config.df.format(ahp.getWeights()[k]));
        }
    }

    private TreeMap<MamocNode, Double> performEvaluation(HashMap<MamocNode, ArrayList<Fuzzy>> availableSites){

        Log.d(TAG, "Calculating AHP Criteria weighting: ");
        calculateAHP();

        Log.d(TAG, "********************************");
        Log.d(TAG, "Calculating Fuzzy TOPSIS: ");

        topsis = new Topsis();

        return topsis.start(availableSites);
    }
}
