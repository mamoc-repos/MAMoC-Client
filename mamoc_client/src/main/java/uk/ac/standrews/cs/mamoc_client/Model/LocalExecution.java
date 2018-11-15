package uk.ac.standrews.cs.mamoc_client.Model;

public class LocalExecution {

    private String taskName;
    private long executionTime;
    private long executionDate;

    public LocalExecution(){
    }

    public LocalExecution(String taskName, long executionDate) {
        this.taskName = taskName;
        this.executionDate = executionDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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
