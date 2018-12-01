package com.moviz.lib.hw;

import com.moviz.lib.comunication.plus.holder.PUserHolder;

public abstract interface PafersDeviceListener extends DeviceListener {

    public abstract void onDeviceStarted();

    public abstract void onDeviceResumed();

    public abstract void onDevicePaused();

    public abstract void onDeviceStopped(boolean paramBoolean);

    public abstract void onDeviceSpeedLimit(double paramDouble1, double paramDouble2);

    public abstract void onDeviceBrand(String paramString);

    public abstract void onDeviceManufacturer(String paramString);

    public abstract void onDeviceUser(PUserHolder user);
}



/* Location:           C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar

 * Qualified Name:     com.pafers.fitnesshwapi.lib.device.FitnessHwApiDeviceListener

 * JD-Core Version:    0.7.0.1

 */