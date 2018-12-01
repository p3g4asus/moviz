package com.moviz.lib.hw;

import com.moviz.gui.R;
import com.moviz.lib.comunication.message.BaseMessage;

public class ZephyrHxMDevice extends GenericDevice {


    public enum Message {
        MSG_NEWDATA
    }

    ;

    public ZephyrHxMDevice() {

    }

    @Override
    protected Class<? extends BaseMessage>[] getAcceptedMessages() {
        return new Class[0];
    }


    @Override
    protected void prepareServiceConnection() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Class<? extends DeviceDataProcessor> getDataProcessorClass() {
        return ZephyrHxMDataProcessor.class;
    }

    @Override
    protected Class<? extends DeviceSimulator> getSimulatorClass() {
        return ZephyrHxMDeviceSimulator.class;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_stat_heartdevice;
    }

    @Override
    protected String getNotificationTitle() {
        return "ZephyrHxM Heart rate";
    }

    @Override
    protected String getNotificationText() {
        return "ZephyrHxM " + innerDevice().getAlias() + " active";
    }

}
