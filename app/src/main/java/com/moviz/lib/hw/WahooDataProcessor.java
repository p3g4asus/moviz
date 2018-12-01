package com.moviz.lib.hw;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.utils.ParcelableMessage;

/**
 * Created by Matteo on 24/10/2016.
 */

public abstract class WahooDataProcessor extends DeviceDataProcessor implements WahooBinder.OnNewCapabilityListener {
    private ParcelableMessage errorMessage = null;
    @Override
    public BaseMessage processCommand(BaseMessage hs) {
        return null;
    }

    public void setDeviceError(ParcelableMessage msg) {
        errorMessage = msg;
    }

    public ParcelableMessage resetDeviceError() {
        ParcelableMessage pm = errorMessage;
        if (errorMessage!=null)
            errorMessage = null;
        return pm;
    }
}
