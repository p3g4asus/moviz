package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.utils.ParcelableMessage;

public interface DeviceListener {
    public void onDeviceConnected(GenericDevice dev, PDeviceHolder devh);

    public void onDeviceConnectionFailed(GenericDevice dev, PDeviceHolder devh);

    public void onDeviceError(GenericDevice dev, PDeviceHolder devh, ParcelableMessage e);

    public void onDeviceDisconnected(GenericDevice dev, PDeviceHolder devh);

    public void onDeviceUpdate(GenericDevice dev, PDeviceHolder devh,
                               DeviceUpdate paramFitnessHwApiDeviceFeedback);

    // public void onSessionStart(GenericDevice dev, PDeviceHolder devh,
    // PSessionHolder sess);
    public void onUserSet(GenericDevice dev, PDeviceHolder devh, PUserHolder us);

    public void onDeviceDescription(GenericDevice dev, PDeviceHolder devh,
                                    String desc);

    public void onDeviceStatusChange(GenericDevice dev, PDeviceHolder devh,
                                     PHolderSetter status);

    public void onDeviceConnecting(GenericDevice mDeviceHolder,
                                   PDeviceHolder innerDevice);
}
