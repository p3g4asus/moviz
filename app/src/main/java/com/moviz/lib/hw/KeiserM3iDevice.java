package com.moviz.lib.hw;

import com.moviz.gui.R;

public class KeiserM3iDevice extends GenericDevice {
    @Override
    protected void prepareServiceConnection() {

    }

    @Override
    protected Class<? extends DeviceDataProcessor> getDataProcessorClass() {
        return KeiserM3iDataProcessor.class;
    }

    @Override
    protected Class<? extends DeviceSimulator> getSimulatorClass() {
        return PafersDeviceSimulator.class;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_stat_m3idevice;
    }

    @Override
    protected String getNotificationTitle() {
        return "Keiser M3i";
    }

    @Override
    protected String getNotificationText() {
        return "KM3i " + innerDevice().getAlias() + " active";
    }
}
