package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

public class AHP {

    private static final String TAG = "AHP";

    /**
     * Random Consistency Index
     */
    private static double RI[] = {0.0, 0.0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

    /**
     * The matrix
     */
    private Array2DRowRealMatrix mtx;

    /**
     * Contains
     */
    private double pairwiseComparisonArray[];

    /**
     * Number of alternatives
     */
    private int nrAlternatives;

    /**
     * The resulting weights/priorities
     */
    protected double weights[];

    /**
     * Corresponds to the weights
     */
    private String labels[] = null;

    /**
     *
     */
    private EigenDecomposition evd;

    /**
     * Convenience array, i.e. comparisonIndices[length=NumberOfPairwiseComparisons][2]
     * Contains minimum number of comparisons.
     */
    private int[][] comparisonIndices;

    /**
     * Index of the greatest Eigenvalue/ -vector
     */
    private int evIdx = 0; // index of actual eigenvalue/-vector

    /**
     * @param labels
     */
    public AHP(String labels[]) {
        this(labels.length);
        this.labels = labels;
    }

    /**
     * Construct an AHP with number of alternatives
     *
     * @param nrAlternatives
     */
    AHP(int nrAlternatives) {
        this.nrAlternatives = nrAlternatives;
        mtx = new Array2DRowRealMatrix(nrAlternatives, nrAlternatives);
        weights = new double[nrAlternatives];

        pairwiseComparisonArray = new double[getNrOfPairwiseComparisons()];
        comparisonIndices = new int[getNrOfPairwiseComparisons()][];
        for (int i = 0; i < getNrOfPairwiseComparisons(); i++) {
            comparisonIndices[i] = new int[2];
        }

        // only need diagonal 1, but set everything to 1.0
        for (int row = 0; row < nrAlternatives; row++) {
            for (int col = 0; col < nrAlternatives; col++) {
                mtx.setEntry(row, col, 1.0);
            }
        }
    }

    /**
     * @return the number of pairwise comparisons which have to be done by the user
     */
    int getNrOfPairwiseComparisons() {
        return ((nrAlternatives - 1) * nrAlternatives) / 2;
    }

    /**
     * @return the user input of the pairwise comparisons
     */
    private double[] getPairwiseComparison() {
        return pairwiseComparisonArray;
    }

    /**
     * Set the pairwise comparison scores and calculate all relevant numbers
     *
     * @param a
     */
    void setPairwiseComparisonArray(double a[]) {
        int i = 0;
        for (int row = 0; row < nrAlternatives; row++) {
            for (int col = row + 1; col < nrAlternatives; col++) {
                //System.out.println(row + "/" + col + "=" + a[i]);
                mtx.setEntry(row, col, a[i]);
                mtx.setEntry(col, row, 1.0 / mtx.getEntry(row, col));
                comparisonIndices[i][0] = row;
                comparisonIndices[i][1] = col;
                i++;
            }
        }
        evd = new EigenDecomposition(mtx);

        evIdx = 0;
        for (int k = 0; k < evd.getRealEigenvalues().length; k++) {
            //System.out.println(evd.getRealEigenvalues()[k]);
            evIdx = (evd.getRealEigenvalue(k) > evd.getRealEigenvalue(evIdx)) ? k : evIdx;
        }
        //System.out.println("evIdx=" + evIdx);
        //System.out.println("EigenValue=" + evd.getRealEigenvalue(evIdx));

        double sum = 0.0;
        RealVector v = evd.getEigenvector(evIdx);
        for (double d : v.toArray()) {
            sum += d;
        }
        //System.out.println(sum);
        for (int k = 0; k < v.getDimension(); k++) {
            weights[k] = v.getEntry(k) / sum;
        }
    }

    /**
     * @param arrayIdx
     * @return
     */
    int[] getIndicesForPairwiseComparison(int arrayIdx) {
        return comparisonIndices[arrayIdx];
    }

    /**
     * @return resulting weights for alternatives
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * @return the consistency index
     */
    double getConsistencyIndex() {
        return (evd.getRealEigenvalue(evIdx) - (double) nrAlternatives) / (double) (nrAlternatives - 1);
    }

    /**
     * @return the consistency ratio. Should be less than 10%
     */
    private double getConsistencyRatio() {
        return getConsistencyIndex() / RI[nrAlternatives] * 100.0;
    }

    void calculateAHP(){

        double[] compArray = getPairwiseComparison();

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

        this.setPairwiseComparisonArray(compArray);

//        for (int i = 0; i < this.getNrOfPairwiseComparisons(); i++) {
//            Log.d(TAG, "Importance of " + Config.criteria[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
//            Log.d(TAG, Config.criteria[this.getIndicesForPairwiseComparison(i)[1]] + "= ");
//            Log.d(TAG, String.valueOf(this.getPairwiseComparison()[i]));
//        }

        Log.d(TAG, "Consistency Index: " + Config.df.format(this.getConsistencyIndex()));
        Log.d(TAG, "Consistency Ratio: " + Config.df.format(this.getConsistencyRatio()) + "%");
        Log.d(TAG, "Weights: ");
        for (int k=0; k<this.getWeights().length; k++) {
            Config.ahpWeights[k] = this.getWeights()[k];
            Log.d(TAG, Config.criteria[k] + ": " + Config.df.format(this.getWeights()[k]));
        }
    }

    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nrAlternatives; i++)
            sb.append(mtx.getRowVector(i)).append("\n");
        return sb.toString();
    }
}