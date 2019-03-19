package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;

/**
 * Created by Matteo on 13/11/2016.
 */

public interface DeviceSimulator {
    int PAUSE_DETECTED = 1;
    int DEVICE_ONLINE = 0;
    int DO_NOT_POST_DU = -1;
    void reset();
    int step(DeviceUpdate deviceUpdate);
    int PAUSE_DELAY_DETECT_THRESHOLD = 10000;

    void setOffsets();
}
