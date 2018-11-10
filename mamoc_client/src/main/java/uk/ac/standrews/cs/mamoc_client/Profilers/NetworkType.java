package uk.ac.standrews.cs.mamoc_client.Profilers;

public enum NetworkType {

    UNKNOWN("unknown"),
    WIFI("wifi"),
    CELLULAR_3G("3G"),
    CELLULAR_4G("4G"),
    CELLULAR_UNKNOWN("cell_unknown");
    
    private String value;

    public String getValue() {
        return value;
    }

    NetworkType(String value) {
        this.value = value;
    }
}