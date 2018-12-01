package com.moviz.lib.hw.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattCharacteristicReadCallback {

    void call(String address, BluetoothGattCharacteristic characteristic);
}
