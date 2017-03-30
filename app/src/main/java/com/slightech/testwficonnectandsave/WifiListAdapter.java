package com.slightech.testwficonnectandsave;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rokey on 2017/3/29.
 */

public class WifiListAdapter extends BaseAdapter {
    private Context context;
    private List<ScanWifiResultBean> scanResultList;

    public WifiListAdapter(Context context, List<ScanWifiResultBean> scanResultList) {
        this.context = context;
        this.scanResultList = scanResultList;
    }

    @Override
    public int getCount() {
        return scanResultList.size();
    }

    @Override
    public Object getItem(int position) {
        return scanResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_wifilist, null);
            holder = new Holder();
            holder.ssidView = (TextView) convertView.findViewById(R.id.tv_ssid);
            holder.connectStatus = (TextView) convertView.findViewById(R.id.tv_connection_status);
            holder.wifiLevel = (ImageView) convertView.findViewById(R.id.iv_wifi_level);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.ssidView.setText(scanResultList.get(position).getSSID());
        if (!TextUtils.isEmpty(scanResultList.get(position).getStatus())) {
            holder.connectStatus.setVisibility(View.VISIBLE);
            holder.connectStatus.setText(scanResultList.get(position).getStatus());
        }else {
            holder.connectStatus.setVisibility(View.GONE);
        }
        switch (scanResultList.get(position).getLevel()) {
            case 0:
                holder.wifiLevel.setImageResource(R.mipmap.wifi_state_leave_0);
                break;
            case 1:
                holder.wifiLevel.setImageResource(R.mipmap.wifi_state_leave_1);
                break;
            case 2:
                holder.wifiLevel.setImageResource(R.mipmap.wifi_state_leave_2);
                break;
            case 3:
                holder.wifiLevel.setImageResource(R.mipmap.wifi_state_leave_3);
                break;
        }
        return convertView;
    }

    class Holder {
        TextView ssidView;
        TextView connectStatus;
        ImageView wifiLevel;
    }
}
