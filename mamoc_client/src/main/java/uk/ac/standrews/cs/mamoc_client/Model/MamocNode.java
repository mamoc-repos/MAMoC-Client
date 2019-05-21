package uk.ac.standrews.cs.mamoc_client.Model;

import android.os.Build;

import com.google.gson.Gson;

import java.io.Serializable;

public class MamocNode implements Serializable {

    private String nodeName;
    private String ip;
    private int port;
    private long joinedDate;
    private int numberOfCPUs;
    private int cpuFreq;
    private long memoryMB;
    private double OffloadingScore;

    public long getJoinedDate() { return joinedDate; }

    public void setJoinedDate(long joinedDate) { this.joinedDate = joinedDate; }

    public int getNumberOfCPUs() { return numberOfCPUs; }

    public void setNumberOfCPUs(int numberOfCPUs) { this.numberOfCPUs = numberOfCPUs; }

    public int getCpuFreq() { return cpuFreq; }

    public void setCpuFreq(int cpuFreq) { this.cpuFreq = cpuFreq; }

    public long getMemoryMB() { return memoryMB; }

    public void setMemoryMB(long memoryMB) { this.memoryMB = memoryMB; }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getOffloadingScore() { return OffloadingScore; }

    public void setOffloadingScore(double offloadingScore) { OffloadingScore = offloadingScore; }
}
