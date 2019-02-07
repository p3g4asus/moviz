package com.moviz.lib.hw;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
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

public class BLEDeviceSearcher implements DeviceSearcher,BLESearchCallback {
    private final String TAG  = getClass().getSimpleName();
    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner mLEScanner;
    private ScanSettings.Builder mSettings;
    private ScanCallback mScanCallback;
    private ArrayList<PDeviceHolder> resultsList = new ArrayList<>();
    private HashMap<String, Boolean> addrMap = new HashMap<>();
    private BLESearchCallback searchCallback = this;

    public long getScanTimeout() {
        return mScanTimeout;
    }

    public void setScanTimeout(long mScanTimeout) {
        this.mScanTimeout = mScanTimeout;
    }

    private long mScanTimeout = 10000;

    public BLEDeviceSearcher() {
        if (mBluetoothAdapter != null && Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
            mScanCallback = new MyScanCallback();
        }
    }

    public ScanSettings.Builder getSettings() {
        return mSettings;
    }

    public void setSettings(ScanSettings.Builder mSettings) {
        this.mSettings = mSettings;
    }

    public BLEDeviceSearcher(BLESearchCallback sc, long timeout, ScanSettings.Builder scan) {
        this();
        searchCallback = sc==null?this:sc;
        mScanTimeout = timeout;
        if (scan!=null)
            mSettings = scan;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            resultsList.clear();
            addrMap.clear();
            if (mBluetoothAdapter != null) {
                if (mScanTimeout>0)
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT < 21) {
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            } else {
                                mLEScanner.stopScan(mScanCallback);
                            }
                            searchCallback.onScanTimeout();
                        }
                    }, mScanTimeout);
                if (Build.VERSION.SDK_INT < 21) {
                    if (!mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                        searchCallback.onScanError(100);
                    }
                } else {
                    mLEScanner.startScan(new ArrayList<ScanFilter>(), mSettings.build(), mScanCallback);
                }
            } else
                onScanError(-1);
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    @Override
    public void onScanOk(BluetoothDevice bd, ScanRecord rec) {
        String addr = bd.getAddress();
        if (!addrMap.containsKey(addr)) {
            resultsList.add(new PDeviceHolder(-1, addr, bd.getName(), "", DeviceType.hrdevice, "", "", true));
            addrMap.put(addr, true);
        }
    }

    @Override
    public void onScanError(int errorCode) {
        Log.e(TAG, "onScanError Error Code: " + errorCode);
        if (errorCode<0)
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_adapter")));
        else
            CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_ERROR).putExtra(DEVICE_ERROR_CODE, (Parcelable) new ParcelableMessage("exm_errs_searcherror").put(errorCode)));
    }

    @Override
    public void onScanTimeout() {
        CA.lbm.sendBroadcast(new Intent(DEVICE_SEARCH_END).putExtra(DEVICE_FOUND, resultsList.toArray(new PDeviceHolder[0])));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "onScanResult "+result.getDevice().getName()+"/"+result.getDevice().getAddress());
            searchCallback.onScanOk(result.getDevice(),result.getScanRecord());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                onScanResult(1, sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(TAG, "onScanFailed "+errorCode);
            searchCallback.onScanError(errorCode);
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice bd, int rssi,
                                     byte[] scanRecord) {

                    searchCallback.onScanOk(bd,null);
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
