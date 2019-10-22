package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.content.Context;
import android.util.Log;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;

import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Profilers.BatteryState;

public class OffloadingScore {

    private static final String TAG = "OffloadingScore";

    public double os;
    private Context mContext;
    MamocFramework framework;

    public OffloadingScore(Context context){
        this.mContext = context;
    }

    public void computeOffloadingScore(MamocNode node){

        framework = MamocFramework.getInstance(mContext);

        if (node instanceof MobileNode) {
            int cp_mn = node.getCpuFreq();
            int rtt_mn = framework.networkProfiler.measureRtt(node.getIp(), node.getPort());
            int bs_mn = ((MobileNode) node).getBatteryState().equals(BatteryState.CHARGING) ? 1 : 0;
            int bm_mn = ((MobileNode) node).getBatteryLevel();

            double[] values = {cp_mn, rtt_mn, bs_mn, bm_mn};
            double[] normalizedValues = normalize(values);

            // OS_{MN} = (B_{MN} + CP_{MN}) - RTT_{MN} - (1-BS_{MN}) \times (100 - BL_{MN})
            node.setOffloadingScore(normalizedValues[0] - normalizedValues[1] - (normalizedValues[2] * normalizedValues[3]));

        } else if (node instanceof EdgeNode || node instanceof CloudNode) {

            int cp_en = node.getCpuFreq();
            int rtt_en = framework.networkProfiler.measureRtt(node.getIp(), node.getPort());

            double[] values = {cp_en, rtt_en};
            double[] normalizedValues = normalize(values);

            // OS_{EN} = (B_{EN} + CP_{EN}) - RTT_{EN}
            node.setOffloadingScore(normalizedValues[0] - normalizedValues[1]);
        } else {
            // This should not happen!
            Log.d(TAG, "Node type not found!");
        }
    }

    private double[] normalize(double[] values){
        double variance = StatUtils.populationVariance(values);
        double sd = Math.sqrt(variance);
        double mean = StatUtils.mean(values);
        NormalDistribution nd = new NormalDistribution();
        double[] normalized = new double[values.length];
        int i=0;
        for ( double value: values ) {
            double stdscore = (value-mean)/sd;
            double sf = 1.0 - nd.cumulativeProbability(Math.abs(stdscore));
//            System.out.println("" + stdscore + " " + sf);
            normalized[i] = stdscore;
            i++;
        }

        return normalized;
    }
}
