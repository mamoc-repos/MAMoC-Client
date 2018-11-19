package uk.ac.standrews.cs.mamoc_client.Model;

import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkType;

public class TaskExecution {

    private String taskName;
    private ExecutionLocation execLocation;
    private NetworkType networkType;
    private double executionTime;
    private double commOverhead;
    private long rttSpeed;
    private long executionDate;
    private boolean completed;

    public TaskExecution(){

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

    public void setExecLocation(ExecutionLocation execLocation) {
        this.execLocation = execLocation;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NetworkType networkType) {
        this.networkType = networkType;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public double getCommOverhead() {
        return commOverhead;
    }

    public void setCommOverhead(double commOverhead) {
        this.commOverhead = commOverhead;
    }

    public long getRttSpeed() {
        return rttSpeed;
    }

    public void setRttSpeed(long rttSpeed) {
        this.rttSpeed = rttSpeed;
    }

    public long getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(long offloadedDate) {
        this.executionDate = offloadedDate;
    }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
