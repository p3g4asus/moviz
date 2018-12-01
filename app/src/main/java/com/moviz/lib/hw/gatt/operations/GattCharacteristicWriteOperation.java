package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Service;

import java.util.Arrays;
import java.util.UUID;

public class GattCharacteristicWriteOperation extends GattOperation {

    private final UUID mService;
    private final UUID mCharacteristic;
    private final byte[] mValue;

    public GattCharacteristicWriteOperation(BluetoothDevice device, UUID service, UUID characteristic, byte[] value) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mValue = value;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        characteristic.setValue(mValue);
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + Characteristic.lookup(mCharacteristic, null) + " s = "
                + Service.lookup(mService, null) + " v = " + Arrays.toString(mValue);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }
}
