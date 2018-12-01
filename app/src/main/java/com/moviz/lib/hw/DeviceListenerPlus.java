package com.moviz.lib.hw;

import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PSessionHolder;

public interface DeviceListenerPlus extends DeviceListener {
    public void onDeviceSession(GenericDevice dev, PDeviceHolder devh, PSessionHolder ses);

    public void onTcpStatus(com.moviz.lib.comunication.tcp.TCPStatus newStatus, String addr);
}
