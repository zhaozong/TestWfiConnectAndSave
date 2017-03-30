package com.slightech.testwficonnectandsave;

/**
 * Created by Rokey on 2017/3/29.
 */

public class ScanWifiResultBean {
    private String SSID;
    private String status;
    private int level;
    private int type;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
