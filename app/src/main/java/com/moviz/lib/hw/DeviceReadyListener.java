package com.moviz.lib.hw;

import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public interface DeviceReadyListener {
    public void onDeviceReady(GenericDevice g, PDeviceHolder h);
}
