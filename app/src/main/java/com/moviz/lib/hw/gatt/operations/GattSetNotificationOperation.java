package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Descriptor;
import com.movisens.smartgattlib.Service;
import com.moviz.lib.hw.gatt.GattManager;

import java.util.UUID;

public class GattSetNotificationOperation extends GattOperation {

    GattManager mGattManager;

    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;

    public GattSetNotificationOperation(BluetoothDevice device, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        super(device);
        mService = serviceUuid;
        mCharacteristic = characteristicUuid;
        mDescriptor = descriptorUuid;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + Characteristic.lookup(mCharacteristic, null) + " s = "
                + Service.lookup(mService, null) + " d = " + Descriptor.lookup(mDescriptor, null);
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        boolean enable = true;
        gatt.setCharacteristicNotification(characteristic, enable);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(mDescriptor);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return false;
    }
}
