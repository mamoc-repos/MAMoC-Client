package uk.ac.standrews.cs.mamoc_client.Execution;

public enum ExecutionLocation {

    LOCAL("LOCAL"),
    D2D("D2D"),
    EDGE("EDGE"),
    PUBLIC_CLOUD("PUBLIC_CLOUD"),
    DYNAMIC("DYNAMIC"),
    DYNAMIC_TIME("DYNAMIC_TIME"),
    DYNAMIC_ENERGY("DYNAMIC_ENERGY");

    private String value;

    public String getValue(){
        return value;
    }

    ExecutionLocation(String value){
        this.value = value;
    }
}
