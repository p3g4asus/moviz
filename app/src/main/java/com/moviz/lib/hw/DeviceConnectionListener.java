package com.moviz.lib.hw;

import android.bluetooth.BluetoothGattCharacteristic;

import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public interface DeviceConnectionListener extends DeviceListener {

    boolean onReadData(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length);

    void onDataWrite(GenericDevice dev, PDeviceHolder devh, byte[] arr, int length);

    boolean onReadData(GenericDevice dev, PDeviceHolder devh, BluetoothGattCharacteristic bcc);

    void onDeviceStopped(GenericDevice dev, PDeviceHolder devh);

    void onDevicePaused(GenericDevice dev, PDeviceHolder devh);

    void onDeviceResumed(GenericDevice dev, PDeviceHolder devh);

    void onDeviceStarted(GenericDevice dev, PDeviceHolder devh);

}
