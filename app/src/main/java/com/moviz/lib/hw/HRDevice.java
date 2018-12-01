package com.moviz.lib.hw;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Service;
import com.moviz.gui.R;
import com.moviz.lib.comunication.plus.holder.PHolder;

import java.util.ArrayList;

public class HRDevice extends BLEDevice {


    @Override
    protected ArrayList<UUIDBundle> getNotifyCharacteristicUids() {
        ArrayList<UUIDBundle> rv = new ArrayList<UUIDBundle>();
        rv.add(new UUIDBundle(Service.HEART_RATE, Characteristic.HEART_RATE_MEASUREMENT));
        rv.add(new UUIDBundle(Service.BATTERY_SERVICE, Characteristic.BATTERY_LEVEL));
        return rv;
    }

    @Override
    protected ArrayList<UUIDBundle> getReadOnceCharacteristicUids() {
        ArrayList<UUIDBundle> rv = new ArrayList<UUIDBundle>();

        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.MANUFACTURER_NAME_STRING));
        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.HARDWARE_REVISION_STRING));
        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.MODEL_NUMBER_STRING));
        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.SERIAL_NUMBER_STRING));
        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.FIRMWARE_REVISION_STRING));
        rv.add(new UUIDBundle(Service.DEVICE_INFORMATION, Characteristic.SOFTWARE_REVISION_STRING));

        rv.add(new UUIDBundle(Service.BATTERY_SERVICE, Characteristic.BATTERY_LEVEL));
        return rv;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_stat_heartdevice;
    }

    @Override
    protected String getNotificationTitle() {
        return "Heart rate LE";
    }

    @Override
    protected String getNotificationText() {
        return "HR BLE " + innerDevice().getAlias() + " active";
    }

    @Override
    protected Class<? extends DeviceDataProcessor> getDataProcessorClass() {
        return HRDataProcessor.class;
    }

    @Override
    protected Class<? extends DeviceSimulator> getSimulatorClass() {
        return HRDeviceSimulator.class;
    }

}
