package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

public class DeviceChangedMessage implements BaseMessage {
    public enum Reason {
        BECAUSE_DEVICE_ADDED,
        BECAUSE_DEVICE_CHANGED,
        BECAUSE_DEVICE_CONF_CHANGED,
        BECAUSE_DEVICE_REMOVED
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    private String key = null,value = null;

    private Reason why;

    public Reason getWhy() {
        return why;
    }

    public void setWhy(Reason why) {
        this.why = why;
    }

    public PDeviceHolder getDev() {
        return dev;
    }

    public void setDev(PDeviceHolder dev) {
        this.dev = dev;
    }

    private PDeviceHolder dev = null;

    public DeviceChangedMessage(Reason w, PDeviceHolder d, String k, String v) {
        why = w;
        dev = d;
        key = k;
        value = v;
    }

    public DeviceChangedMessage(Reason w, String k, String v) {
        why = w;
        key = k;
        value = v;
    }

    @Override
    public byte getType() {
        // TODO Auto-generated method stub
        return 0x11;
    }

}
