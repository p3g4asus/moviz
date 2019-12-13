package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.util.SparseArray;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;

import timber.log.Timber;

public class NonConnectableBinder extends DeviceBinder implements BLESearchCallback {
    private BLEDeviceSearcher mLEScanner = null;
    protected Handler mHandler = new Handler();
    protected long mScanBetween = 1000;
    protected Runnable mForceRestart = null;
    public String TAG = getClass().getName(); 
    public NonConnectableBinder() {
        super();
    }

    protected void scheduleForceRestart(boolean yes) {
        if (!yes && mForceRestart!=null) {
            Timber.tag(TAG).i("Deleting forceRestart");
            mHandler.removeCallbacks(mForceRestart);
            mForceRestart = null;
        }
        else if (yes) {
            Timber.tag(TAG).i("Scheduling forceRestart");
            mForceRestart = new Runnable() {
                @Override
                public void run() {
                    if (!mDevices.isEmpty()) {
                        mLEScanner.stopSearch();
                        mLEScanner.startSearch(null);
                        scheduleForceRestart(true);
                    }
                }
            };
            mHandler.postDelayed(mForceRestart,900000);
        }

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
            String errstr;
            if (oldst==BluetoothState.CONNECTING)
                bund.postDeviceError(new ParcelableMessage(errstr = "exm_errr_connectionfailed"));
            else if (oldst==BluetoothState.DISCONNECTING)
                bund.postDeviceError(new ParcelableMessage(errstr = "exm_errr_connectionlost"));
            else
                bund.postDeviceError(new ParcelableMessage(errstr = "exm_errr_nonconn_multipletimeout").put(bund.mDeviceHolder.innerDevice()));
            Timber.tag(TAG).w(
                    "Disconnecting "+errstr);
            bund.setBluetoothState(BluetoothState.IDLE);
            if (mDevices.isEmpty()) {
                mLEScanner.stopSearch();
                scheduleForceRestart(false);
            }
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
            Timber.tag(TAG).w(
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        else {

            boolean needsStart = mDevices.isEmpty();
            final NonConnectableDataProcessor bldevb = (NonConnectableDataProcessor) newDp(device);
            //bldevb.setIsDebugging(DF_ACTIVE);

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
                    scheduleForceRestart(true);
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
                Timber.tag(TAG).i("Timeout detected for "+bldevb.mDeviceHolder);
                if (bldevb.getBluetoothState()!=BluetoothState.DISCONNECTING)
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
        Timber.tag(TAG).i("onScanOK "+dev.getName()+"/"+dev.getAddress());
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
        Timber.tag(TAG).i("Scan timeout");
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
