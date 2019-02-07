package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.util.SparseArray;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

import java.util.HashMap;
import java.util.Map;

public class M3iDeviceSearcher extends BLEDeviceSearcher {
    private int id = -1;
    private HashMap<String,PDeviceHolder> resMap = new HashMap<>();
    public M3iDeviceSearcher(int i) {
        super(null,10000,null);
        id = i;
        ScanSettings.Builder sst = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0);
        if (Build.VERSION.SDK_INT >= 23) {
            sst.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).setReportDelay(0);
        }
        setSettings(sst);
    }
    public M3iDeviceSearcher() {
        this(-1);
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
            SparseArray<byte[]> sp = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                sp = rec.getManufacturerSpecificData();
            }
            byte[] bt = null;
            if (sp != null && sp.size() > 0)
                bt = sp.get(sp.keyAt(0));
            if (bt!=null && bt.length>6) {
                PDeviceHolder cc = new PDeviceHolder(-1, addr, bd.getName(), "", DeviceType.hrdevice, "", "", true);
                Map<String,String> map = cc.deserializeAdditionalSettings();
                int index = 0;

                // Moves index past prefix bits (some platforms remove prefix bits from data)
                if (bt[index] == 2 && bt[index + 1] == 1)
                    index += 2;
                map.put("machineid",""+((int)bt[index+3]));
                cc.serializeAdditionalSettings(map,false);
                resMap.put(addr,cc);
            }
        }
    }
}
