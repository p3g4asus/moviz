package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.movisens.smartgattlib.Characteristic;
import com.moviz.lib.hw.gatt.GattCharacteristicReadCallback;

import java.util.UUID;

public class GattCharacteristicReadOperation extends GattOperation {
    private final UUID mService;
    private final UUID mCharacteristic;
    private final GattCharacteristicReadCallback mCallback;

    public GattCharacteristicReadOperation(BluetoothDevice device, UUID service, UUID characteristic, GattCharacteristicReadCallback callback) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mCallback = callback;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        gatt.readCharacteristic(characteristic);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + Characteristic.lookup(mCharacteristic, null);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattCharacteristic characteristic) {
        mCallback.call(getDevice().getAddress(), characteristic);
    }
}
