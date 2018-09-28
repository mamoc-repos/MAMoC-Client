package uk.ac.standrews.cs.emap;

import android.os.Build;

import com.google.gson.Gson;

import java.io.Serializable;

class MamocNode implements Serializable {
    private String nodeName = Build.MODEL;
    private String osVersion = Build.VERSION.RELEASE;
    private String manufacturer = Build.MANUFACTURER;
    private String ip;
    private int port;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
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

    public static MamocNode fromJSON(String deviceJSON) {
        Gson gson = new Gson();
        MamocNode deviceDTO = gson.fromJson(deviceJSON, MamocNode.class);
        return deviceDTO;
    }

    @Override
    public String toString() {
//        return "MamocNode{" +
//                "nodeName='" + nodeName + '\'' +
//                ", osVersion='" + osVersion + '\'' +
//                ", manufacturer='" + manufacturer + '\'' +
//                ", ip='" + ip + '\'' +
//                ", port=" + port +
//                '}';
        String stringRep = (new Gson()).toJson(this);
        return stringRep;
    }

}
