package com.slightech.testwficonnectandsave;

/**
 * Created by Rokey on 2017/3/28.
 * wifi 连接，查询
 * 需要权限：
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 */


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiConnectUtils {
    private static final String TAG = WifiConnectUtils.class.getSimpleName();
    //wifi加密状态
    public static final int OPEN = 0;
    public static final int WEP = 1;
    public static final int WPA = 2;

    private WifiManager localWifiManager;//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
    //private List<ScanResult> wifiScanList;//ScanResult用来描述已经检测出的接入点，包括接入的地址、名称、身份认证、频率、信号强度等

    public WifiConnectUtils(Context context) {
        localWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 查询WIFI状态
     *
     * @return WIFI_STATE_DISABLING = 0;
     * WIFI_STATE_DISABLED = 1;
     * WIFI_STATE_ENABLING = 2;
     * WIFI_STATE_ENABLED = 3;
     * WIFI_STATE_UNKNOWN = 4;
     */
    public int wifiCheckState() {
        return localWifiManager.getWifiState();
    }

    /**
     * 打开WIFI
     */
    public void wifiOpen() {
        if (!localWifiManager.isWifiEnabled()) {
            localWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭wifi
     */
    public void wifiClose() {
        if (!localWifiManager.isWifiEnabled()) {
            localWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 扫描WIFI
     */
    public void wifiStartScan() {
        localWifiManager.startScan();
    }

    /**
     * 得到扫描的结果
     *
     * @return
     */
    public List<ScanResult> getScanResults() {
        return localWifiManager.getScanResults();//得到扫描结果
    }

    /**
     * 将所有的扫描结果转换成字符串
     *
     * @param list
     * @return
     */
    public List<String> scanResultToString(List<ScanResult> list) {
        List<String> strReturnList = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            ScanResult strScan = list.get(i);
            String str = strScan.toString();
            boolean bool = strReturnList.add(str);
            if (!bool) {
                Log.i("scanResultToSting", "Add fail");
            }
        }
        return strReturnList;
    }

    /**
     * 读取系统存储的WIFI信息
     */
    public List<WifiConfiguration> getConfiguration() {
        List<WifiConfiguration> wifiConfigList;//WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
        wifiConfigList = localWifiManager.getConfiguredNetworks();
        for (int i = 0; i < wifiConfigList.size(); i++) {
            Log.i("getConfiguration", wifiConfigList.get(i).SSID);
            Log.i("getConfiguration", String.valueOf(wifiConfigList.get(i).networkId));
        }
        return wifiConfigList;
    }


    /**
     * 根据SSID 从系统中读取WIIF的NetId
     *
     * @param wifiConfigList 系统wifi配置列表
     * @param SSID           需要查找的SSID
     * @return
     */
    public int getWifiID(List<WifiConfiguration> wifiConfigList, String SSID) {
        Log.i("IsConfiguration", String.valueOf(wifiConfigList.size()));
        for (int i = 0; i < wifiConfigList.size(); i++) {
            Log.i(wifiConfigList.get(i).SSID, String.valueOf(wifiConfigList.get(i).networkId));
            if (wifiConfigList.get(i).SSID.equals("\""+SSID+"\"")) {
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }

    /**
     * 向系统中添加WIFI配置信息
     *
     * @param ssid
     * @param pwd
     * @param type OPEN = 0 ; WEP = 1; WPA = 2;
     * @return wifiID
     */
    public int addWifiConfig(String ssid, String pwd, int type) {
        int wifiId = -1;
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        //如果系统已经保存了这个wifi配置信息，先删除，再配置。
        WifiConfiguration tempConfig = this.isExsits(ssid);
        if (tempConfig != null) {
            localWifiManager.removeNetwork(tempConfig.networkId);
            localWifiManager.saveConfiguration();
        }

        if (type == OPEN) //OPEN
        {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == WEP) //WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + pwd + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == WPA) //WPA
        {
            config.preSharedKey = "\"" + pwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        wifiId = localWifiManager.addNetwork(config);
        if (wifiId != -1) {
            return wifiId;
        } else {
            Log.e(TAG, "网络配置信息添加失败");
        }
        return wifiId;
    }

    /**
     * 连接指定wifiID的网络
     *
     * @param wifiId
     * @return
     */
    public boolean connectWifi(int wifiId) {
        List<WifiConfiguration> wifiConfigList = getConfiguration();
        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration wifi = wifiConfigList.get(i);
            if (wifi.networkId == wifiId) {
                while (!(localWifiManager.enableNetwork(wifiId, true))) {//激活该Id，建立连接
                    Log.i("ConnectWifi", String.valueOf(wifiConfigList.get(wifiId).status));//status:0--已经连接，1--不可连接，2--可以连接
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 创建一个WIFILock
     */
    public WifiLock createWifiLock(String lockName) {
        WifiLock wifiLock;//手机锁屏后，阻止WIFI也进入睡眠状态及WIFI的关闭
        wifiLock = localWifiManager.createWifiLock(lockName);
        return wifiLock;
    }

    /**
     * 锁定wifilock
     */
    public void acquireWifiLock(WifiLock wifiLock) {
        wifiLock.acquire();
    }

    /**
     * 解锁WIFI
     */
    public void releaseWifiLock(WifiLock wifiLock) {
        if (wifiLock.isHeld()) {//判定是否锁定
            wifiLock.release();
        }
    }

    /**
     * 得到建立连接的信息
     */
    public WifiInfo getConnectedInfo() {
        WifiInfo wifiConnectedInfo;//已经建立好网络链接的信息
        wifiConnectedInfo = localWifiManager.getConnectionInfo();
        return wifiConnectedInfo;
    }

    /**
     * 得到连接的MAC地址
     *
     * @return
     */
    public String getConnectedMacAddr() {
        WifiInfo wifiConnectedInfo = getConnectedInfo();
        return (wifiConnectedInfo == null) ? "NULL" : wifiConnectedInfo.getMacAddress();
    }

    /**
     * 得到连接的名称SSID
     *
     * @return
     */
    public String getConnectedSSID() {
        String ssid = localWifiManager.getConnectionInfo().getSSID();
        int deviceVersion = Build.VERSION.SDK_INT;
        //API17之后 SSID带引号，去掉引号
        if (deviceVersion >= 17) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * 得到连接的IP地址
     *
     * @return
     */
    public int getConnectedIPAddr() {
        WifiInfo wifiConnectedInfo = getConnectedInfo();
        return (wifiConnectedInfo == null) ? 0 : wifiConnectedInfo.getIpAddress();
    }

    /**
     * 得到连接的ID
     *
     * @return
     */
    public int getConnectedID() {
        WifiInfo wifiConnectedInfo = getConnectedInfo();
        return (wifiConnectedInfo == null) ? 0 : wifiConnectedInfo.getNetworkId();
    }

    /**
     * 判断要添加的WIFI配置信息是否已经存在系统中。
     *
     * @param SSID
     * @return
     */
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = localWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 断开网络连接,并删除配置
     */
    public void disconnectedWifi(String ssid) {
        removeWifiConfig(ssid);
        localWifiManager.disconnect();
    }

    /**
     *  删除wifi配置
     * @param ssid
     */
    public void removeWifiConfig(String ssid){
        WifiConfiguration tempConfig = this.isExsits(ssid);
        if (tempConfig != null){
            localWifiManager.removeNetwork(tempConfig.networkId);
            localWifiManager.saveConfiguration();
        }
    }

}