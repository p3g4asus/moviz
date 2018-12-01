package com.moviz.lib.hw;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import com.moviz.gui.app.CA;
import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Matteo on 23/10/2016.
 */

public class BLEDeviceSearcher implements DeviceSearcher {
    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mSettings;
    private ScanCallback mScanCallback;
    private ArrayList<PDeviceHolder> resultsList = new ArrayList<>();
    private HashMap<String, Boolean> addrMap = new HashMap<>();

    public BLEDeviceSearcher() {
        if (mBluetoothAdapter != null && Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            mScanCallback = new MyScanCallback();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            resultsList.clear();
            addrMap.clear();
            if (mBluetoothAdapter != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT < 21) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        } else {
                            mLEScanner.stopScan(mScanCallback);
                        }
                        CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_END).putExtra(DEVICE_FOUND, resultsList.toArray(new PDeviceHolder[0])));
                    }
                }, 10000);
                if (Build.VERSION.SDK_INT < 21) {
                    if (!mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                        CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_searcherror").put(100)));
                    }
                } else {
                    mLEScanner.startScan(new ArrayList<ScanFilter>(), mSettings, mScanCallback);
                }
            } else
                CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_adapter")));
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private void onScanResult(BluetoothDevice bd) {
        String addr = bd.getAddress();
        if (!addrMap.containsKey(addr)) {
            resultsList.add(new PDeviceHolder(-1, addr, bd.getName(), "", DeviceType.hrdevice, "", "", true));
            addrMap.put(addr, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            BLEDeviceSearcher.this.onScanResult(result.getDevice());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                onScanResult(1, sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_searcherror").put(errorCode)));
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice bd, int rssi,
                                     byte[] scanRecord) {
                    onScanResult(bd);
                }
            };

    @Override
    public void startSearch(Context ct) {
        scanLeDevice(true);
    }

    @Override
    public void stopSearch() {
        scanLeDevice(false);
    }

    @Override
    public int needsRebind(GenericDevice d) {
        return 0;
    }

    @Override
    public void startRebind(Context ct, List<GenericDevice> d) {

    }

    @Override
    public void stopRebind() {

    }
}
