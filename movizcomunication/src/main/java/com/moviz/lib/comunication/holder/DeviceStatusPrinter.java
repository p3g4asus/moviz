package com.moviz.lib.comunication.holder;

import com.moviz.lib.comunication.DeviceStatus;

public class DeviceStatusPrinter extends EnumPrinter {
    public DeviceStatusPrinter() {
        reference = DeviceStatus.statuses;
    }
}
