package com.moviz.lib.comunication;

public enum DeviceStatus {
    OFFLINE, CONNECTING, STANDBY, RUNNING, PAUSED, DPAUSE;

    public static DeviceStatus[] statuses = DeviceStatus.values();
}
