package com.slightech.testwficonnectandsave;

import java.io.Serializable;

/**
 * Created by Rokey on 2017/3/29.
 */

public class WifiSavedBean implements Serializable {
    private String SSID;
    private String pwd;
    //加密方式
    private int type;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
