package uk.ac.standrews.cs.mamoc_client.Model;

import uk.ac.standrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkType;

public class OffloadedTask {

    private String taskName;
    private ExecutionLocation execLocation;
    private NetworkType networkType;
    private long executionTime;
    private long commOverhead;
    private long rttSpeed;
    private long offloadedDate;

    public OffloadedTask(String taskName, ExecutionLocation execLocation, NetworkType networkType,
                         long executionTime, long commOverhead, long rttSpeed, long offloadedDate) {
        this.taskName = taskName;
        this.execLocation = execLocation;
        this.networkType = networkType;
        this.executionTime = executionTime;
        this.commOverhead = commOverhead;
        this.rttSpeed = rttSpeed;
        this.offloadedDate = offloadedDate;
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

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getCommOverhead() {
        return commOverhead;
    }

    public void setCommOverhead(long commOverhead) {
        this.commOverhead = commOverhead;
    }

    public long getRttSpeed() {
        return rttSpeed;
    }

    public void setRttSpeed(long rttSpeed) {
        this.rttSpeed = rttSpeed;
    }

    public long getOffloadedDate() {
        return offloadedDate;
    }

    public void setOffloadedDate(long offloadedDate) {
        this.offloadedDate = offloadedDate;
    }
}
