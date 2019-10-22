package uk.ac.standrews.cs.mamoc_client.Profilers;

public enum BatteryState {
    CHARGING (100),
    NOT_CHARGING (0);

    private int value;

    public int getValue() {
        return value;
    }

    BatteryState(int value){ this.value = value; }
}
