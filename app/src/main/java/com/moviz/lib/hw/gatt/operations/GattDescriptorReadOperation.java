package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Descriptor;
import com.movisens.smartgattlib.Service;
import com.moviz.lib.hw.gatt.GattDescriptorReadCallback;

import java.util.UUID;

public class GattDescriptorReadOperation extends GattOperation {

    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;
    private final GattDescriptorReadCallback mCallback;

    public GattDescriptorReadOperation(BluetoothDevice device, UUID service, UUID characteristic, UUID descriptor, GattDescriptorReadCallback callback) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mDescriptor = descriptor;
        mCallback = callback;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        BluetoothGattDescriptor descriptor = gatt.getService(mService).getCharacteristic(mCharacteristic).getDescriptor(mDescriptor);
        gatt.readDescriptor(descriptor);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + Characteristic.lookup(mCharacteristic, null) + " s = "
                + Service.lookup(mService, null) + " d = " + Descriptor.lookup(mDescriptor, null);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattDescriptor descriptor) {
        mCallback.call(descriptor.getValue());
    }
}
