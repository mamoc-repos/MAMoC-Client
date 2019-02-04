package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import java.text.DecimalFormat;

class Config {

    // Set your alternatives here
    static final String alternatives[] = new String[]{"Mobile", "Edge", "Public"};

    // Set your criteria here
    static final String criteria[] = new String[]{"Bandwidth", "Speed", "Availability", "Security", "Price"};

    // Set true for benefir criterion, false for cost criterion
    static final boolean[] costCriteria = new boolean[]{false, false, false, false, true}; // price is cost

    static Double[] ahpWeights = new Double[Config.criteria.length];

    // AHP criteria weights in respect to each other
    static final Double BANDWIDTH_SPEED = 1.0;
    static final Double BANDWIDTH_AVAILABILITY = 5.0;
    static final Double BANDWIDTH_SECURITY = 7.0;
    static final Double BANDWIDTH_PRICE = 9.0;
    static final Double SPEED_AVAILABILITY = 5.0;
    static final Double SPEED_SECURITY = 6.0;
    static final Double SPEED_PRICE = 8.0;
    static final Double AVAILABLITY_SECURITY = 3.0;
    static final Double AVAIALABILITY_PRICE = 3.0;
    static final Double SECURITY_PRICE = 2.0;

    // The following values are obtained in profiling stage prior to offloading
    static Fuzzy MOBILE_BANDWIDTH = Fuzzy.VERY_HIGH;
    static Fuzzy MOBILE_SPEED = Fuzzy.GOOD;
    static Fuzzy MOBILE_AVAILABILITY = Fuzzy.HIGH;
    static Fuzzy MOBILE_SECURITY = Fuzzy.HIGH;
    static Fuzzy MOBILE_PRICE = Fuzzy.VERY_LOW;

    static Fuzzy EDGE_BANDWIDTH = Fuzzy.VERY_HIGH;
    static Fuzzy EDGE_SPEED = Fuzzy.HIGH;
    static Fuzzy EDGE_AVAILABILITY = Fuzzy.HIGH;
    static Fuzzy EDGE_SECURITY = Fuzzy.HIGH;
    static Fuzzy EDGE_PRICE = Fuzzy.LOW;

    static Fuzzy PUBLIC_BANDWIDTH = Fuzzy.LOW;
    static Fuzzy PUBLIC_SPEED = Fuzzy.VERY_HIGH;
    static Fuzzy PUBLIC_AVAILABILITY = Fuzzy.VERY_HIGH;
    static Fuzzy PUBLIC_SECURITY = Fuzzy.GOOD;
    static Fuzzy PUBLIC_PRICE = Fuzzy.VERY_HIGH;

    // These values can also be computed from max and min of weighted decision matrix
    static final double[] idealSolution = {1,1,1};
    static final double[] antiIdealSolution = {0,0,0};

    // Number of decimal points for float number formatting
    static final DecimalFormat df = new DecimalFormat("0.0000");

}
