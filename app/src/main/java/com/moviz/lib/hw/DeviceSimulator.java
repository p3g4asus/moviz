package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;

/**
 * Created by Matteo on 13/11/2016.
 */

public interface DeviceSimulator {
    void reset();
    boolean step(DeviceUpdate deviceUpdate);
    int PAUSE_DELAY_DETECT_THRESHOLD = 10000;

    void setOffsets();
}
