package com.moviz.lib.hw;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanRecord;

import com.movisens.smartgattlib.Descriptor;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.hw.gatt.CharacteristicChangeListener;
import com.moviz.lib.hw.gatt.ConnectionStateChangedListener;
import com.moviz.lib.hw.gatt.GattCharacteristicReadCallback;
import com.moviz.lib.hw.gatt.GattManager;
import com.moviz.lib.hw.gatt.operations.GattCharacteristicReadOperation;
import com.moviz.lib.hw.gatt.operations.GattDisconnectOperation;
import com.moviz.lib.hw.gatt.operations.GattOperation;
import com.moviz.lib.hw.gatt.operations.GattSetNotificationOperation;
import com.moviz.lib.utils.ParcelableMessage;

import java.util.ArrayList;

import timber.log.Timber;

public class BLEBinder extends DeviceBinder implements BLESearchCallback {
    private GattManager mGattManager = null;
    private BLEDataProcessor dataProcessor = null;

    private BLEDeviceSearcher mLEScanner = new BLEDeviceSearcher(this,10000,null);


    @Override
    public void onScanOk(BluetoothDevice dev, ScanRecord rec) {
        if (dataProcessor!=null) {
            if (dev.getAddress().equals(dataProcessor.getDeviceAddress())) {
                mLEScanner.stopSearch();
                BLEDevice device = (BLEDevice) dataProcessor.getDevice();
                long foundOnce = System.currentTimeMillis();
                device.sendMessage(new DeviceChangeRequestMessage(device.innerDevice(),"tmpfoundonce",foundOnce+""));
                device.setFoundOnce(foundOnce);
                onScanResultOK(dataProcessor);
            }
        }
    }

    @Override
    public void onScanError(int code) {
        onScanTimeout();
    }

    @Override
    public void onScanTimeout() {
        if (dataProcessor!=null) {
            dataProcessor.postDeviceError(new ParcelableMessage("exm_errr_connectionfailed"));
            dataProcessor = null;
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

    @Override
    public boolean connect(GenericDevice device, PUserHolder us) {
        if (device == null) {
            Timber.tag(TAG).w("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        else {
            final BLEDataProcessor bldevb = (BLEDataProcessor) newDp(device);

            BluetoothState bst = bldevb.getBluetoothState();
            if (bst != BluetoothState.CONNECTING && bst != BluetoothState.CONNECTED) {
                bldevb.setUser(us);
                bldevb.setBluetoothState(BluetoothState.CONNECTING);
                if (((BLEDevice)device).getFoundOnce()==0) {
                    dataProcessor = bldevb;
                    mLEScanner.startSearch(null);
                }
                else {
                    onScanResultOK(bldevb);
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
            Timber.tag(TAG).i("New Bluetooth state " + newState);
            if (devb != null)
                devb.setBluetoothState(newState);

        }

        @Override
        public void error(String address, ParcelableMessage e, GattOperation op) {
            String id = e.getId();
            DeviceDataProcessor devb = mDevices.get(address);
            Timber.tag(TAG).i("Gatt server " + id);
            if (devb != null) {
                if (id.equals("exm_errr_gatt_operationfailed") || id.equals("exm_errr_gatt_operationtimeout"))
                    disconnect(devb.mDeviceHolder);
                devb.postDeviceError(e.put(devb.mDeviceHolder.innerDevice()));
            }
        }

    };
}