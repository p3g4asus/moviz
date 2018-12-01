package com.moviz.lib.hw;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.movisens.smartgattlib.Descriptor;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.hw.gatt.CharacteristicChangeListener;
import com.moviz.lib.hw.gatt.ConnectionStateChangedListener;
import com.moviz.lib.hw.gatt.GattCharacteristicReadCallback;
import com.moviz.lib.hw.gatt.GattManager;
import com.moviz.lib.hw.gatt.operations.GattCharacteristicReadOperation;
import com.moviz.lib.hw.gatt.operations.GattDisconnectOperation;
import com.moviz.lib.hw.gatt.operations.GattOperation;
import com.moviz.lib.hw.gatt.operations.GattSetNotificationOperation;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.ArrayList;
import java.util.List;

public class BLEBinder extends DeviceBinder {
    private GattManager mGattManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mSettings;
    private MyScanCallback mScanCallback = new MyScanCallback();
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice bd, int rssi,
                                     byte[] scanRecord) {
                    onScanResult(bd);
                }
            };

    private Handler mHandler = new Handler(Looper.myLooper());

    private EndDiscoveryRunnable endDiscovery = new EndDiscoveryRunnable();

    private class EndDiscoveryRunnable implements Runnable {
        public BLEDataProcessor getDataProcessor() {
            return dataProcessor;
        }

        private BLEDataProcessor dataProcessor = null;

        public void stopDiscovery() {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }

        @Override
        public void run() {
            stopDiscovery();
            if (dataProcessor!=null) {
                dataProcessor.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                dataProcessor = null;
            }
        }

        public void setDataProcessor(BLEDataProcessor wd) {
            dataProcessor = wd;
        }
    };

    private void onScanResult(BluetoothDevice dev) {
        BLEDataProcessor bldevb = endDiscovery.getDataProcessor();
        if (bldevb!=null) {
            if (dev.getAddress().equals(bldevb.getDeviceAddress())) {
                mHandler.removeCallbacks(endDiscovery);
                endDiscovery.stopDiscovery();
                BLEDevice device = (BLEDevice) bldevb.getDevice();
                long foundOnce = System.currentTimeMillis();
                device.sendMessage(new DeviceChangeRequestMessage(device.innerDevice(),"tmpfoundonce",foundOnce+""));
                device.setFoundOnce(foundOnce);
                onScanResultOK(bldevb);
            }
        }
    }

    private void onScanResultOK(BLEDataProcessor bldevb) {
        for (UUIDBundle bund : bldevb.getReadOnceChar())
            mGattManager.queue(
                    new GattCharacteristicReadOperation(bldevb.mBluetoothDevice, bund.mService, bund.mCharacteristic, characteristicReadOperationCallback));
        for (UUIDBundle bund : bldevb.getNotifyChar()) {
            mGattManager.addNoDuplicateCharacteristicChangeListener(bund.mCharacteristic, characteristicChangeListener);
            mGattManager.queue(
                    new GattSetNotificationOperation(bldevb.mBluetoothDevice, bund.mService, bund.mCharacteristic, Descriptor.CLIENT_CHARACTERISTIC_CONFIGURATION));
        }
    }

    @Override
    public void setService(DeviceService.BaseBinder bb) {
        super.setService(bb);
        if (mGattManager == null) {
            mGattManager = new GattManager(bb.getContext());
            mGattManager.setConnectionStateChangedListener(mGattConnectionStateChangedListener);
        }
    }

    private GattCharacteristicReadCallback characteristicReadOperationCallback = new GattCharacteristicReadCallback() {

        @Override
        public void call(String deviceAddress,
                         BluetoothGattCharacteristic characteristic) {

            processNewData(deviceAddress, characteristic);

        }
    };

    private CharacteristicChangeListener characteristicChangeListener = new CharacteristicChangeListener() {

        @Override
        public void onCharacteristicChanged(String deviceAddress,
                                            BluetoothGattCharacteristic characteristic) {
            processNewData(deviceAddress, characteristic);
        }

    };

    private void processNewData(String address, final BluetoothGattCharacteristic characteristic) {
        DeviceDataProcessor c2i = mDevices.get(address);
        if (c2i != null)
            c2i.postReadData(characteristic);
    }

    @Override
    public void disconnect(GenericDevice device) {
        DeviceDataProcessor bund = mDevices.get(device.getAddress());
        if (bund != null) {
            if (bund.isConnected()) {
                GattDisconnectOperation disc = new GattDisconnectOperation(bund.mBluetoothDevice);
                mGattManager.queue(disc);
                bund.setBluetoothState(BluetoothState.DISCONNECTING);
            } else {
                mDevices.remove(bund);
                bund.setBluetoothState(BluetoothState.IDLE);
            }

        }

    }

    public void setConnectionParams(GenericDevice d, ArrayList<UUIDBundle> readOnceChar, ArrayList<UUIDBundle> notifyChar) {
        BLEDataProcessor dp = (BLEDataProcessor) newDp(d);
        dp.setCharacteristic(readOnceChar, notifyChar);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MyScanCallback extends ScanCallback {


        public MyScanCallback() {
        }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BLEBinder.this.onScanResult(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                onScanResult(1, sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            mHandler.removeCallbacks(endDiscovery);
            endDiscovery.run();
        }
    }

    @Override
    public boolean connect(GenericDevice device, PUserHolder us) {
        if (device == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        else {
            final BLEDataProcessor bldevb = (BLEDataProcessor) newDp(device);
            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        mSettings = new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                                .build();
                    }
                } else {
                    bldevb.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                    return false;
                }

            }

            BluetoothState bst = bldevb.getBluetoothState();
            if (bst != BluetoothState.CONNECTING && bst != BluetoothState.CONNECTED) {
                bldevb.setUser(us);
                bldevb.setBluetoothState(BluetoothState.CONNECTING);
                if (((BLEDevice)device).getFoundOnce()==0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT < 21) {
                                if (!mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                                    bldevb.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
                                    return;
                                }
                            } else {
                                ArrayList<ScanFilter> filts = new ArrayList<>();
                                filts.add(new ScanFilter.Builder().setDeviceAddress(bldevb.getDeviceAddress()).build());
                                mLEScanner.startScan(new ArrayList<ScanFilter>(), mSettings, mScanCallback);
                            }
                            endDiscovery.setDataProcessor(bldevb);
                            mHandler.postDelayed(endDiscovery, 10000);
                        }
                    });
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onScanResultOK(bldevb);
                        }
                    });
                }
                return true;
            } else
                return false;
        }
    }

    private final ConnectionStateChangedListener mGattConnectionStateChangedListener = new ConnectionStateChangedListener() {

        @Override
        public void stateChanged(String address, BluetoothState newState, GattOperation op) {
            DeviceDataProcessor devb = mDevices.get(address);
            Log.i(TAG, "New Bluetooth state " + newState);
            if (devb != null)
                devb.setBluetoothState(newState);

        }

        @Override
        public void error(String address, ParcelableMessage e, GattOperation op) {
            String id = e.getId();
            DeviceDataProcessor devb = mDevices.get(address);
            Log.i(TAG, "Gatt server " + id);
            if (devb != null) {
                if (id.equals("exm_errr_gatt_operationfailed") || id.equals("exm_errr_gatt_operationtimeout"))
                    disconnect(devb.mDeviceHolder);
                devb.postDeviceError(e.put(devb.mDeviceHolder.innerDevice()));
            }
        }

    };
}