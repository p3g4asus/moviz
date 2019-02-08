package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;

public class NonConnectableBinder extends DeviceBinder implements BLESearchCallback {
    private BLEDeviceSearcher mLEScanner = null;
    protected Handler mHandler = new Handler();
    protected long mScanBetween = 1000;
    public NonConnectableBinder() {
        super();
    }

    @Override
    public void disconnect(GenericDevice d) {
        String addr = d.getAddress();
        NonConnectableDataProcessor bund = (NonConnectableDataProcessor) mDevices.get(addr);
        if (bund != null) {
            Runnable r = bund.getTimeoutRunnable();
            if (r!=null)
                mHandler.removeCallbacks(r);
            mDevices.remove(addr);
            BluetoothState oldst = bund.getBluetoothState();
            if (oldst==BluetoothState.CONNECTING)
                bund.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
            else
                bund.postDeviceError(new ParcelableMessage("exm_errr_nonconn_multipletileout").put(bund.mDeviceHolder.innerDevice()));
            bund.setBluetoothState(BluetoothState.IDLE);
            if (mDevices.isEmpty())
                mLEScanner.stopSearch();
        }
    }

    protected BLEDeviceSearcher bulildScanner() {
        ScanSettings.Builder sst = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0);
        if (Build.VERSION.SDK_INT >= 23) {
            sst.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).setReportDelay(0);
        }
        return new BLEDeviceSearcher(this, 0,sst);
    }



    @Override
    public boolean connect(final GenericDevice device, PUserHolder us) {
        if (device == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        else {

            boolean needsStart = mDevices.isEmpty();
            final NonConnectableDataProcessor bldevb = (NonConnectableDataProcessor) newDp(device);

            BluetoothState bst = bldevb.getBluetoothState();
            if (bst != BluetoothState.CONNECTING && bst != BluetoothState.CONNECTED) {
                bldevb.setUser(us);
                bldevb.setBluetoothState(BluetoothState.CONNECTING);
                if (needsStart) {
                    if (mLEScanner==null) {
                        mScanBetween = bldevb.mScanBetween;
                        mLEScanner = bulildScanner();
                    }
                    else {
                        long st = bldevb.getScanBetween();
                        if (st<mScanBetween)
                            mScanBetween = st;
                    }
                    mLEScanner.startSearch(null);

                }
                acquireData(bldevb);
                return true;
            } else
                return false;
        }
    }

    private void acquireData(final NonConnectableDataProcessor bldevb) {
        Runnable r = bldevb.getTimeoutRunnable();
        if (r!=null)
            mHandler.removeCallbacks(r);
        r = new Runnable() {
            @Override
            public void run() {
                disconnect(bldevb.mDeviceHolder);
                Log.i(TAG,"Timeout detected for "+bldevb.mDeviceHolder);
                onScanTimeout();
            }
        };
        bldevb.setTimeoutRunnable(r);
        mHandler.postDelayed(r,bldevb.getScanBetween()+bldevb.getScanTimeout());
    }

    protected NonConnectableDataProcessor dataProcessorFromDevice(BluetoothDevice dev, ScanRecord rec) {
        return (NonConnectableDataProcessor)mDevices.get(dev.getAddress());
    }

    @Override
    public void onScanOk(BluetoothDevice dev, ScanRecord rec) {
        final NonConnectableDataProcessor bldevb = dataProcessorFromDevice(dev,rec);
        Log.i(TAG,"onScanOK "+dev.getName()+"/"+dev.getAddress());
        if (bldevb!=null) {
            BluetoothState btst = bldevb.getBluetoothState();
            if (btst!=BluetoothState.DISCONNECTING) {
                SparseArray<byte[]> sp;
                byte[] bt;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP &&
                        (sp = rec.getManufacturerSpecificData()) != null && sp.size() > 0 &&
                        (bt = sp.get(sp.keyAt(0)))!=null && bt.length>0 &&
                        bldevb.onReadData(bldevb.mDeviceHolder, bldevb.mDeviceHolder.innerDevice(), bt, bt.length)) {

                    if (btst == BluetoothState.CONNECTING)
                        bldevb.setBluetoothState(BluetoothState.CONNECTED);
                    acquireData(bldevb);
                }
            }
        }
    }

    @Override
    public void onScanError(int code) {
        onScanTimeout();
    }

    @Override
    public void onScanTimeout() {
        Log.i(TAG,"Scan timeout");
        if (!mDevices.isEmpty()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mDevices.isEmpty())
                        mLEScanner.startSearch(null);
                }
            }, mScanBetween);
        }

    }
}
