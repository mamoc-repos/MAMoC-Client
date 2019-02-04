package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

public enum Fuzzy {

    VERY_LOW(new double[] {0.0, 0.0, 0.25}),
    LOW(new double[] {0.0, 0.25, 0.50}),
    GOOD(new double[] {0.25, 0.50, 0.75}),
    HIGH(new double[] {0.50, 0.75, 1.0}),
    VERY_HIGH(new double[] {0.75, 1.0, 1.0});

    private double[] value;

    public double[] getValue() {
        return value;
    }

    Fuzzy(double[] value) {
        this.value = value;
    }
}
