package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public class BluetoothRefreshMesssage implements BaseMessage {
    public PDeviceHolder[] getDevices() {
        return devices;
    }

    private PDeviceHolder[] devices = null;


    private PDeviceHolder source = null;

    public BluetoothRefreshMesssage(PDeviceHolder s, PDeviceHolder[] devs) {
        source = s;
        devices = devs;
    }

    @Override
    public byte getType() {
        // TODO Auto-generated method stub
        return 0x10;
    }

    public PDeviceHolder getSource() {
        return source;
    }
}
