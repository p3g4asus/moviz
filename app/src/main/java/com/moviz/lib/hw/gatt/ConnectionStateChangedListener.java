package com.moviz.lib.hw.gatt;

import com.moviz.lib.hw.BluetoothState;
import com.moviz.lib.hw.gatt.operations.GattOperation;
import com.moviz.lib.utils.ParcelableMessage;

public interface ConnectionStateChangedListener {
    public void stateChanged(String address, BluetoothState newState, GattOperation op);

    public void error(String address, ParcelableMessage e, GattOperation op);
}
