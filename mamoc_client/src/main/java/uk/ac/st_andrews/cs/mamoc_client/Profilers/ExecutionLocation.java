package uk.ac.st_andrews.cs.mamoc_client.Profilers;

public enum ExecutionLocation {

    LOCAL("local"),
    D2D("D2D"),
    EDGE("edge"),
    PUBLIC_CLOUD("public"),
    DYNAMIC("dynamic"),
    DYNAMIC_TIME("dynamic_time"),
    DYNAMIC_ENERGY("dynamic_energy");

    private String value;

    public String getValue(){
        return value;
    }

    ExecutionLocation(String value){
        this.value = value;
    }
}
