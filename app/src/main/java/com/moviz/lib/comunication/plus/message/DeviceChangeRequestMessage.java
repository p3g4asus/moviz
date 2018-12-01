package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;

/**
 * Created by Matteo on 01/11/2016.
 */

public class DeviceChangeRequestMessage implements BaseMessage {

    private final String mValue;
    private final String mKey;
    private final PDeviceHolder mDev;

    public PDeviceHolder getDev() {
        return mDev;
    }

    public String getKey() {
        return mKey;
    }

    public String getFullKey() {
        return fullkeyFromKey(mKey,mDev);
    }

    public static String fullkeyFromKey(String k,PDeviceHolder devh) {
        return "pref_devicepriv_" + devh.getType().name() + "_" + devh.getId() + "_" + k;
    }

    public String getValue() {
        return mValue;
    }


    public DeviceChangeRequestMessage(PDeviceHolder dev, String key, String value) {
        mDev = dev;
        mKey = key;
        mValue = value;
    }

    @Override
    public byte getType() {
        return 0x14;
    }
}
