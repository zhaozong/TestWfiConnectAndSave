package com.slightech.testwficonnectandsave;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String SAVED = "已保存";
    private static final String CONNECTED = "已连接";

    private PopupWindow wifiListPopupWindow;
    private List<WifiSavedBean> wifiSavedBeanList = new ArrayList<>();
    private Map<String, ScanWifiResultBean> scanWifiResultBeanMap = new HashMap<>();
    private List<ScanWifiResultBean> scanWifiResultBeanList = new ArrayList<>(scanWifiResultBeanMap.values());
    private Button button;
    private WifiConnectUtils wifiConnectUtils;
    private SharedPreferences wifiSavedListSP;
    private WifiListAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (adapter != null)
                adapter.notifyDataSetChanged();
            super.handleMessage(msg);
        }
    };
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (wifiConnectUtils.wifiCheckState() == 0 || wifiConnectUtils.wifiCheckState() == 3) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshWifiList();
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btn_wifiscan);
        wifiConnectUtils = new WifiConnectUtils(this);
        wifiSavedListSP = this.getSharedPreferences("wifi", Context.MODE_PRIVATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void click(View view) {
        //TODO 显示WIFI扫描的界面
        showPopupwindow(this);
    }

    public void showPopupwindow(Context context) {
        final View popuView = View.inflate(context, R.layout.popupwindow_listview, null);
        ListView wifiListView = (ListView) popuView.findViewById(R.id.lv_popupwindow);
        refreshWifiList();
        handler.sendEmptyMessage(0);
        adapter = new WifiListAdapter(this, scanWifiResultBeanList);
        wifiListView.setAdapter(adapter);
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                final ScanWifiResultBean scanWifiResultBean = scanWifiResultBeanList.get(position);

                if (scanWifiResultBean.getStatus().equals(CONNECTED)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    AlertDialog dialog = builder.setMessage("")
                            .setTitle(scanWifiResultBean.getSSID())
                            .setNegativeButton("取消保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (WifiSavedBean wifiSavedBean : wifiSavedBeanList) {
                                        if (wifiSavedBean.getSSID().equals(scanWifiResultBean.getSSID())) {
                                            wifiSavedBeanList.remove(wifiSavedBean);
                                            break;
                                        }
                                    }
                                    //重新保存
                                    ObjectSaveUitls.saveArray(wifiSavedListSP, wifiSavedBeanList);
                                    wifiConnectUtils.disconnectedWifi(scanWifiResultBean.getSSID());
                                    scanWifiResultBeanList.remove(position);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshWifiList();
                                            handler.sendEmptyMessage(0);
                                        }
                                    }).start();
                                }
                            }).setPositiveButton("完成", null)
                            .create();
                    dialog.show();
                } else if (scanWifiResultBean.getStatus().equals(SAVED)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    AlertDialog dialog = builder.setTitle(scanWifiResultBean.getSSID())
                            .setNegativeButton("取消保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeFromSavedList(scanWifiResultBean);
                                    wifiConnectUtils.removeWifiConfig(scanWifiResultBean.getSSID());
                                }
                            }).setPositiveButton("连接", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (WifiSavedBean wifiSavedBean : wifiSavedBeanList) {
                                        if (wifiSavedBean.getSSID().equals(scanWifiResultBean.getSSID())) {
                                            //TODO 将需要连接的wifi 信息通过SDENO 传给AIUI
                                        }
                                    }
                                    List<WifiConfiguration> configurations = wifiConnectUtils.getConfiguration();
                                    int wifiID = wifiConnectUtils.getWifiID(configurations, scanWifiResultBean.getSSID());
                                    wifiConnectUtils.connectWifi(wifiID);
                                }
                            }).create();
                    dialog.show();
                } else if (TextUtils.isEmpty(scanWifiResultBean.getStatus())) {
                    WifiPswDialog dialog = new WifiPswDialog(MainActivity.this, new WifiPswDialog.OnCustomDialogListener() {
                        @Override
                        public void back(String str) {
                            //弹出dialog，输入密码，连接，保存
                            WifiSavedBean wifiSavedBean = new WifiSavedBean();
                            wifiSavedBean.setSSID(scanWifiResultBean.getSSID());
                            wifiSavedBean.setPwd(str);
                            wifiSavedBean.setType(scanWifiResultBean.getType());
                            wifiSavedBeanList.add(wifiSavedBean);
                            ObjectSaveUitls.saveArray(wifiSavedListSP, wifiSavedBeanList);
                            int wifiID = wifiConnectUtils.addWifiConfig(scanWifiResultBean.getSSID(), str, scanWifiResultBean.getType());
                            wifiConnectUtils.connectWifi(wifiID);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshWifiList();
                                    handler.sendEmptyMessage(0);
                                }
                            }).start();
                        }
                    });
                    dialog.show();
                }
            }
        });
        wifiListPopupWindow = new PopupWindow(popuView, UiUtil.dp2px(this, 300), UiUtil.dp2px(this, 500));
        wifiListPopupWindow.setContentView(popuView);
        wifiListPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        wifiListPopupWindow.setOutsideTouchable(true);
        wifiListPopupWindow.showAtLocation(button, Gravity.START, 0, 0);
    }

    /**
     * 刷新wifi热点list
     */
    private void refreshWifiList() {
        //打开wifi，扫描热点,获取扫描结果
        wifiConnectUtils.wifiOpen();
        wifiConnectUtils.wifiStartScan();
        List<ScanResult> scanResults = wifiConnectUtils.getScanResults();
        scanWifiResultBeanList.clear();
        scanWifiResultBeanMap.clear();
        //已保存的wifi
        wifiSavedBeanList.clear();
        wifiSavedBeanList.addAll(ObjectSaveUitls.<WifiSavedBean>loadArray(wifiSavedListSP));

        for (ScanResult scanResult : scanResults) {
            ScanWifiResultBean scanWifiResultBean = new ScanWifiResultBean();
            scanWifiResultBean.setSSID(scanResult.SSID);
            scanWifiResultBean.setLevel(WifiManager.calculateSignalLevel(scanResult.level, 4));
            scanWifiResultBean.setStatus("");
            if (scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("wpa")) {
                scanWifiResultBean.setType(WifiConnectUtils.WPA);
            } else if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("wep")) {
                scanWifiResultBean.setType(WifiConnectUtils.WEP);
            } else {
                scanWifiResultBean.setType(WifiConnectUtils.OPEN);
            }
//            List<String> strings = wifiConnectUtils.scanResultToString(scanResults);
//            for (String str : strings) {
//                Log.i("rokey", str);
//            }
            for (WifiSavedBean wifiSavedBean : wifiSavedBeanList) {
                if (wifiSavedBean.getSSID().equals(scanResult.SSID)) {
                    scanWifiResultBean.setStatus(SAVED);
                }
            }
            if (wifiConnectUtils.getConnectedSSID().equals(scanResult.SSID)) {
                scanWifiResultBean.setStatus(CONNECTED);
            }
            //信号最强的显示出来
            if (scanWifiResultBeanMap.containsKey(scanWifiResultBean.getSSID())
                    && scanWifiResultBean.getLevel() < scanWifiResultBeanMap.get(scanWifiResultBean.getSSID()).getLevel()) {
                continue;
            } else {
                scanWifiResultBeanMap.put(scanWifiResultBean.getSSID(), scanWifiResultBean);
            }
        }
        scanWifiResultBeanList.addAll(scanWifiResultBeanMap.values());
        //把已连接的提到最前
        //把已保存的提到最前
        for (int i = 0; i < scanWifiResultBeanList.size(); i++) {
            ScanWifiResultBean result = scanWifiResultBeanList.get(i);
            if (result.getStatus().equals(CONNECTED)) {
                scanWifiResultBeanList.remove(result);
                scanWifiResultBeanList.add(0, result);
            } else if (result.getStatus().equals(SAVED)) {
                scanWifiResultBeanList.remove(result);
                if (scanWifiResultBeanList.get(0).getStatus().equals(CONNECTED) && scanWifiResultBeanList.size() > 2) {
                    scanWifiResultBeanList.add(1, result);
                } else {
                    scanWifiResultBeanList.add(0, result);
                }
            }
        }
    }

    private void removeFromSavedList(ScanWifiResultBean scanWifiResultBean) {
        for (WifiSavedBean wifiSaveBean : wifiSavedBeanList) {
            if (wifiSaveBean.getSSID().equals(scanWifiResultBean.getSSID())) {
                wifiSavedBeanList.remove(wifiSaveBean);
                ObjectSaveUitls.saveArray(wifiSavedListSP, wifiSavedBeanList);
                break;
            }
        }
    }

}
