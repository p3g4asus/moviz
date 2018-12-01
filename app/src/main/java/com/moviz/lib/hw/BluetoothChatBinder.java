package com.moviz.lib.hw;

import com.moviz.lib.comunication.plus.holder.PUserHolder;

public class BluetoothChatBinder extends DeviceBinder {

    @Override
    public void disconnect(GenericDevice d) {
        BluetoothChatDataProcessor<? extends Enum<?>> dev = (BluetoothChatDataProcessor<? extends Enum<?>>) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.stopTh();
        }
    }

    @Override
    public boolean connect(GenericDevice d, PUserHolder us) {
        BluetoothChatDataProcessor<? extends Enum<?>> dev = (BluetoothChatDataProcessor<? extends Enum<?>>) newDp(d);
        BluetoothState bst = dev.getBluetoothState();
        if (bst != BluetoothState.CONNECTING && bst != BluetoothState.CONNECTED) {
            dev.setUser(us);
            mDevices.put(d.getAddress(), dev);
            dev.connectTh();
        }
        return true;
    }

    public void setUser(GenericDevice d, PUserHolder u) {
        BluetoothChatDataProcessor<? extends Enum<?>> dev = (BluetoothChatDataProcessor<? extends Enum<?>>) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.setUser(u);
        }
    }

    public void write(GenericDevice d, byte[] out) {
        BluetoothChatDataProcessor<? extends Enum<?>> dev = (BluetoothChatDataProcessor<? extends Enum<?>>) mDevices.get(d.getAddress());
        if (dev != null)
            dev.write(out);
    }
}

/*
 * Location: C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar
 * 
 * Qualified Name: com.pafers.fitnesshwapi.lib.bluetooth.BluetoothChatService
 * 
 * JD-Core Version: 0.7.0.1
 */