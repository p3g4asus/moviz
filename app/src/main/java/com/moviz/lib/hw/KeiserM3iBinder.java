package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KeiserM3iBinder extends NonConnectableBinder {
    HashMap<String,String> mAddreMap = new HashMap<String,String>();
    @Override
    protected NonConnectableDataProcessor dataProcessorFromDevice(BluetoothDevice dev, ScanRecord rec) {
        String a1;
        NonConnectableDataProcessor cc = (NonConnectableDataProcessor)mDevices.get(a1 = dev.getAddress());
        if (cc==null && "M3s".equals(dev.getName())) {
            String a2 = mAddreMap.get(a1);
            if (a2!=null) {
                return (NonConnectableDataProcessor)mDevices.get(a2);
            }
            Collection<DeviceDataProcessor> col = mDevices.values();
            for (DeviceDataProcessor dp:col) {
                if (dp.mDeviceName.equals("M3s")) {
                    Map<String,String> mp = dp.getDevice().device.deserializeAdditionalSettings();
                    String id;
                    if (((id = mp.get("machineid"))!=null && id.equals(KeiserM3iDataProcessor.getMachineId(rec))) || id==null) {
                        mAddreMap.put(a1,dp.mAddress);
                        return (NonConnectableDataProcessor) dp;
                    }
                }
            }
        }
        return cc;
    }

    @Override
    protected BLEDeviceSearcher bulildScanner() {
        return new KeiserM3iDeviceSearcher(this,0,-1);
    }
}
