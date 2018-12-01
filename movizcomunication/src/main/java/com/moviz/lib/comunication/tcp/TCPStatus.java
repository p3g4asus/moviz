package com.moviz.lib.comunication.tcp;

public enum TCPStatus {
    BOUNDING,
    BOUNDED,
    CONNECTED,
    ERROR,
    IDLE;

    public static TCPStatus[] statuses = TCPStatus.values();
}
