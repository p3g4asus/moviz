package com.moviz.lib.comunication.holder;

public class TCPStatusPrinter extends EnumPrinter {
    public TCPStatusPrinter() {
        reference = com.moviz.lib.comunication.tcp.TCPStatus.statuses;
    }
}
