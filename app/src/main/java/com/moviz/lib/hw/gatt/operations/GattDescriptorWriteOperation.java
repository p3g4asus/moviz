package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Descriptor;
import com.movisens.smartgattlib.Service;

import java.util.UUID;

import timber.log.Timber;

public class GattDescriptorWriteOperation extends GattOperation {

    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;

    public GattDescriptorWriteOperation(BluetoothDevice device, UUID service, UUID characteristic, UUID descriptor) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mDescriptor = descriptor;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Timber.tag("GattManager").d("Writing to " + mDescriptor);
        BluetoothGattDescriptor descriptor = gatt.getService(mService).getCharacteristic(mCharacteristic).getDescriptor(mDescriptor);
        gatt.writeDescriptor(descriptor);
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
}
