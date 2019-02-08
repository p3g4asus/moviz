package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

import java.util.HashMap;
import java.util.Map;

public class KeiserM3iDeviceSearcher extends BLEDeviceSearcher {
    private int id = -1;
    private HashMap<String,PDeviceHolder> resMap = new HashMap<>();
    public KeiserM3iDeviceSearcher(BLESearchCallback sc, long timeout, int i) {
        super(sc,timeout,null);
        id = i;
        ScanSettings.Builder sst = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0);
        if (Build.VERSION.SDK_INT >= 23) {
            sst.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).setReportDelay(0);
        }
        setSettings(sst);
    }
    public KeiserM3iDeviceSearcher() {
        this(null,10000,-1);
    }

    @Override
    public void onScanTimeout() {
        CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_END).putExtra(DEVICE_FOUND, resMap.values().toArray(new PDeviceHolder[0])));
    }

    @Override
    public void onScanOk(BluetoothDevice bd, ScanRecord rec) {
        String addr = bd.getAddress();
        String nm = bd.getName();
        if (nm!=null && nm.startsWith("M3") && !resMap.containsKey(addr)) {
            String mid = KeiserM3iDataProcessor.getMachineId(rec);
            if (!mid.isEmpty() && (id<0 || (""+id).equals(mid))) {
                PDeviceHolder cc = new PDeviceHolder(-1, addr, bd.getName(), "", DeviceType.hrdevice, "", "", true);
                Map<String,String> map = cc.deserializeAdditionalSettings();
                map.put("machineid",mid);
                cc.serializeAdditionalSettings(map,false);
                resMap.put(addr,cc);
            }
        }
    }
}
