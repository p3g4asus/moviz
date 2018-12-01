package com.moviz.lib.hw.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public class GattDisconnectOperation extends GattOperation {

    public GattDisconnectOperation(BluetoothDevice device) {
        super(device);
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        gatt.disconnect();
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }
}
