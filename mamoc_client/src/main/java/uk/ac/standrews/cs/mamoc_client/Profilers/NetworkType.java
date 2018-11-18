package uk.ac.standrews.cs.mamoc_client.Profilers;

public enum NetworkType {

    UNKNOWN("UNKNOWN"),
    WIFI("WIFI"),
    CELLULAR_3G("CELLULAR_3G"),
    CELLULAR_4G("CELLULAR_4G"),
    CELLULAR_UNKNOWN("CELLULAR_UNKNOWN");
    
    private String value;

    public String getValue() {
        return value;
    }

    NetworkType(String value) {
        this.value = value;
    }
}