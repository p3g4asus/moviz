package com.moviz.workers;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PStatusHolder;

import java.util.Map;

public interface AdvancedListener {
    void onDeviceUpdate(PDeviceHolder devh, DeviceUpdate upd, Map<PDeviceHolder, DeviceUpdate> uM);

    void onDeviceStatus(PDeviceHolder devh, PStatusHolder sta, Map<PDeviceHolder, PStatusHolder> uM);
}
