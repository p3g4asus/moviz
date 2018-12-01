package com.moviz.lib.hw;

import com.moviz.gui.R;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.ProgramChangeMessage;
import com.moviz.lib.comunication.message.ProgramListRequestMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.UserSetMessage;

public class PafersDevice extends GenericDevice {

    @Override
    protected Class<? extends BaseMessage>[] getAcceptedMessages() {
        return new Class[]{
                DeviceChangedMessage.class,
                UserSetMessage.class,
                UpDownMessage.class,
                StartMessage.class,
                PauseMessage.class,
                ProgramChangeMessage.class,
                ProgramListRequestMessage.class
        };
    }

    public enum Message {
        MSG_NEWDATA,
        MSG_UNIT,
        MSG_INVALIDMACHINETYPE,
        MSG_TYPE,
        MSG_SPEEDLIM,
        MSG_CHANGESTATE0,
        MSG_CHANGESTATE1,
        MSG_CHANGESTATE2,
        MSG_KEYSTOP,
        MSG_MANUFACTURER,
        MSG_BRAND
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_stat_pafersdevice;
    }

    @Override
    protected String getNotificationTitle() {
        return "Pafers Bike";
    }

    @Override
    protected String getNotificationText() {
        return "Pafers Bike " + innerDevice().getAlias() + " running";
    }

    @Override
    protected void prepareServiceConnection() {

    }

    @Override
    protected Class<? extends DeviceDataProcessor> getDataProcessorClass() {
        // TODO Auto-generated method stub
        return PafersDataProcessor.class;
    }

    @Override
    protected Class<? extends DeviceSimulator> getSimulatorClass() {
        return PafersDeviceSimulator.class;
    }

}