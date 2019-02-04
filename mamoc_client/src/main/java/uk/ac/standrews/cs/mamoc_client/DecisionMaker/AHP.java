package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import android.support.annotation.NonNull;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

public class AHP {

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
     *
     * @param labels
     */
    public AHP(String labels[]) {
        this(labels.length);
        this.labels = labels;
    }

    /**
     * Construct an AHP with number of alternatives
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
     *
     * @return the number of pairwise comparisons which have to be done by the user
     */
    int getNrOfPairwiseComparisons() {
        return ((nrAlternatives - 1) * nrAlternatives) / 2;
    }

    /**
     *
     * @return the user input of the pairwise comparisons
     */
    double[] getPairwiseComparisonArray() {
        return pairwiseComparisonArray;
    }

    /**
     * Set the pairwise comparison scores and calculate all relevant numbers
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
     *
     * @param arrayIdx
     * @return
     */
    int[] getIndicesForPairwiseComparison(int arrayIdx) {
        return comparisonIndices[arrayIdx];
    }

    /**
     *
     * @return resulting weights for alternatives
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     *
     * @return the consistency index
     */
    double getConsistencyIndex() {
        return (evd.getRealEigenvalue(evIdx) - (double) nrAlternatives) / (double) (nrAlternatives - 1);
    }

    /**
     *
     * @return the consistency ratio. Should be less than 10%
     */
    double getConsistencyRatio() {
        return getConsistencyIndex() / RI[nrAlternatives] * 100.0;
    }

    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<nrAlternatives; i++)
            sb.append(mtx.getRowVector(i)).append("\n");
        return sb.toString();
    }

    /**
     * Test the AHP with these static values
     * @param argv
     */
    public static void main(String argv[]) {
        int nrVx = 5;
        String labels[] = {"Bandwidth", "Speed", "Availability", "Security", "Price"};

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        double compArray[] = ahp.getPairwiseComparisonArray();

        // Set the pairwise comparison values
        compArray[0] = 1.0; // BANDWIDTH_SPEED;
        compArray[1] = 5.0; // BANDWIDTH_AVAILABILITY;
        compArray[2] = 7.0; // BANDWIDTH_SECURITY;
        compArray[3] = 9.0; // BANDWIDTH_PRICE;
        compArray[4] = 5.0; // SPEED_AVAILABILITY;
        compArray[5] = 6.0; // SPEED_SECURITY;
        compArray[6] = 8.0; // SPEED_PRICE;
        compArray[7] = 3.0; // AVAILABLITY_SECURITY;
        compArray[8] = 3.0; // AVAIALABILITY_PRICE;
        compArray[9] = 2.0; // SECURITY_PRICE;

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k]);
        }

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
    }

}