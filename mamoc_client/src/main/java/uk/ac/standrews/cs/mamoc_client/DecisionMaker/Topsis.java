package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.util.Log;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;

public class Topsis {

    private static final String TAG = "TOPSIS";

    private HashMap<MamocNode, ArrayList<Double>> sitesMatrix;
    private HashMap<MamocNode, ArrayList<Fuzzy>> availableSites;

    Topsis(){
        sitesMatrix =new HashMap<>();
        availableSites = new HashMap<>();
    }

    public TreeMap<MamocNode, Double> calculateTopsis(HashMap<MamocNode, ArrayList<Fuzzy>> sites){

        availableSites = sites;

        sitesMatrix = calculateFuzzyTopsis(availableSites);

        HashMap<MamocNode, Double> idealDistances = calculateDistance(sitesMatrix, true);
        HashMap<MamocNode, Double> antiIdealDistances = calculateDistance(sitesMatrix, false);

        TreeMap<MamocNode, Double> ccStar = calculateRelativeCloseness(idealDistances, antiIdealDistances);

        Log.d(TAG, "**********************************");
        Log.d(TAG, "Final Ranking:");

        for (Map.Entry<MamocNode, Double> entry: ccStar.entrySet()){
            Log.d(TAG, entry.getKey() + ": " + Config.df.format(entry.getValue()));
        }

        return ccStar;
    }

    private HashMap<MamocNode, ArrayList<Double>> calculateFuzzyTopsis(HashMap<MamocNode, ArrayList<Fuzzy>> availableSites) {

//        Log.d(TAG, availableSites.keySet());
//        Log.d(TAG, availableSites.values());

        for (Map.Entry<MamocNode,ArrayList<Fuzzy>> entry: availableSites.entrySet()) {

            Log.d(TAG, String.valueOf(entry));

            ArrayList<Double> weightedMatrix = new ArrayList<>();

            for (int k = 0; k < entry.getValue().size(); k++) {
                for (double fuzzyValue:entry.getValue().get(k).getValue()) {
                    weightedMatrix.add(fuzzyValue);
                }
            }

            sitesMatrix.put(entry.getKey(), weightedMatrix);
        }

        Log.d(TAG, "unweighted fuzzy values: " + sitesMatrix);


        for (Map.Entry<MamocNode,ArrayList<Double>> entry: sitesMatrix.entrySet()) {

            ArrayList<Double> weightedMatrix = new ArrayList<>();

            int j = 0;
            for (int i = 0; i < entry.getValue().size(); i++) {
                weightedMatrix.add(entry.getValue().get(i) * Config.ahpWeights[j]);
                if (j%3 == 0)
                    j++;
            }
            entry.setValue(weightedMatrix);
        }

        Log.d(TAG, "weighted fuzzy values: " + sitesMatrix);

        return sitesMatrix;
    }

//    private ArrayList<Fuzzy> profileNode(String node){
//        ArrayList<Fuzzy> siteCriteria = new ArrayList<>();
//
//        // Mobile node
//        if (node.equalsIgnoreCase(Config.alternatives[0])){
//            siteCriteria.add(Config.MOBILE_BANDWIDTH);
//            siteCriteria.add(Config.MOBILE_SPEED);
//            siteCriteria.add(Config.MOBILE_AVAILABILITY);
//            siteCriteria.add(Config.MOBILE_SECURITY);
//            siteCriteria.add(Config.MOBILE_PRICE);
//        }
//        else if (node.equalsIgnoreCase(Config.alternatives[1])) { // Edge
//            siteCriteria.add(Config.EDGE_BANDWIDTH);
//            siteCriteria.add(Config.EDGE_SPEED);
//            siteCriteria.add(Config.EDGE_AVAILABILITY);
//            siteCriteria.add(Config.EDGE_SECURITY);
//            siteCriteria.add(Config.EDGE_PRICE);
//        }
//        // Public cloud instance
//        else {
//            siteCriteria.add(Config.PUBLIC_BANDWIDTH);
//            siteCriteria.add(Config.PUBLIC_SPEED);
//            siteCriteria.add(Config.PUBLIC_AVAILABILITY);
//            siteCriteria.add(Config.PUBLIC_SECURITY);
//            siteCriteria.add(Config.PUBLIC_PRICE);
//        }
//
//        return siteCriteria;
//    }

    private HashMap<MamocNode, Double> calculateDistance(HashMap<MamocNode, ArrayList<Double>> sitesMatrix, boolean ideal) {
        EuclideanDistance distance = new EuclideanDistance();
        // The normalized values for the ideal solution and negative ideal solution on criteria are always (1,1,1) and (0,0,0) respectively
        Double dValue = 0.0;
        HashMap<MamocNode, Double> results = new HashMap<>();

        for (Map.Entry<MamocNode,ArrayList<Double>> entry: sitesMatrix.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i = i + 3) {
                double[] fuzzyValues = {entry.getValue().get(i), entry.getValue().get(i+1), entry.getValue().get(i+2)};
                if (ideal) { // For D+
                    if (Config.costCriteria[i/3]) { // cost value for price criterion
                        dValue += distance.compute(fuzzyValues, Config.antiIdealSolution) * (1.0/3.0);
                    } else {
                        dValue += distance.compute(fuzzyValues, Config.idealSolution) * (1.0/3.0);
                    }
                } else { // For D-
                    if (Config.costCriteria[i/3]) { // cost value for price criterion
                        dValue += distance.compute(fuzzyValues, Config.idealSolution) * (1.0/3.0);
                    } else {
                        dValue += distance.compute(fuzzyValues, Config.antiIdealSolution) * (1.0/3.0);
                    }
                }
            }

            results.put(entry.getKey(), dValue);
            Log.d(TAG, ideal? "D+": "D-");
            Log.d(TAG, " for " + entry.getKey().getNodeName() + " is: " + Config.df.format(dValue));
        }

        return results;
    }

    private TreeMap<MamocNode, Double> calculateRelativeCloseness(HashMap<MamocNode, Double> dPlusList, HashMap<MamocNode, Double> dMinusList) {

        TreeMap<MamocNode, Double> cStar = new TreeMap<>();

        for (Map.Entry<MamocNode,Double> entry: dPlusList.entrySet()) {
            // c* = d- / (d- + d+)
            cStar.put(entry.getKey(), dMinusList.get(entry.getKey()) / ( dMinusList.get(entry.getKey()) + entry.getValue()));
        }

        Log.d(TAG, "closeness coefficient set is: " + cStar);

        return cStar;
    }
}
