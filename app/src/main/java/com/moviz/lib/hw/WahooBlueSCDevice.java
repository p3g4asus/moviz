package com.moviz.lib.hw;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.plus.message.DeviceChangedMessage;
import com.moviz.lib.comunication.plus.message.UserSetMessage;

/**
 * Created by Matteo on 25/10/2016.
 */

public class WahooBlueSCDevice extends WahooDevice {

    @Override
    protected Class<? extends DeviceDataProcessor> getDataProcessorClass() {
        return WahooBlueSCDataProcessor.class;
    }

    @Override
    protected Class<? extends DeviceSimulator> getSimulatorClass() {
        return WahooBlueSCDeviceSimulator.class;
    }

    @Override
    protected Class<? extends BaseMessage>[] getAcceptedMessages() {
        return new Class[]{
                DeviceChangedMessage.class,
                UserSetMessage.class,
                UpDownMessage.class
        };
    }
}
