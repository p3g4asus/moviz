package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

public interface BLESearchCallback {
    void onScanOk(BluetoothDevice dev, ScanRecord rec);
    void onScanError(int code);
    void onScanTimeout();
}
