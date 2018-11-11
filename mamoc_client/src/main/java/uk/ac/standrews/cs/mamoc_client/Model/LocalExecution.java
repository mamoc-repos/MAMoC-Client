package uk.ac.standrews.cs.mamoc_client.Model;

import uk.ac.standrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkType;

public class LocalExecution {

    private String taskName;
    private ExecutionLocation execLocation;
    private NetworkType networkType;
    private long executionTime;
    private long executionDate;

    public LocalExecution(){
    }

    public LocalExecution(String taskName, NetworkType networkType,
                           long executionTime, long executionDate) {
        this.taskName = taskName;
        this.networkType = networkType;
        this.executionTime = executionTime;
        this.executionDate = executionDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public ExecutionLocation getExecLocation() {
        return execLocation;
    }

    public void setExecLocation(ExecutionLocation execLocation) { this.execLocation = execLocation; }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NetworkType networkType) {
        this.networkType = networkType;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(long executionDate) {
        this.executionDate = executionDate;
    }
}
