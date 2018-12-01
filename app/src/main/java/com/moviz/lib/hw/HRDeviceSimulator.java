package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.HRDeviceHolder;

public class HRDeviceSimulator implements DeviceSimulator{
    private long sessionStart, lastStart, timeTotms, lastUpdate, lastTimeTot;
    private int updateN;
    private double nBeats;
    private int jouleSum, pulseSum;
    private boolean wasActive;

    @Override
    public void reset() {
        lastStart = -1;
        timeTotms = 0;
        sessionStart = 0;
        updateN = 0;
        nBeats = 0.0;
        pulseSum = 0;
        jouleSum = 0;
        lastUpdate = -1;
        lastTimeTot = 0;
        wasActive = false;
    }

    public HRDeviceSimulator() {
        reset();
    }

    @Override
    public boolean step(DeviceUpdate du) {
        HRDeviceHolder w = (HRDeviceHolder) du;
        long now = System.currentTimeMillis();
        boolean active = w.pulse > 0 && w.worn != 0;
        if (active) {
            long diff;
            if (sessionStart == 0)
                sessionStart = now;
            if (lastStart < 0)
                lastStart = now;
            diff = now - lastStart;
            if (diff > 0 && (lastUpdate<0 || now-lastUpdate<PAUSE_DELAY_DETECT_THRESHOLD)) {
                timeTotms = diff;
                if (lastUpdate > 0)
                    nBeats += w.pulse * ((now - lastUpdate) / 60000.0);
                w.nBeatsR = (int) (nBeats + 0.5);
                w.updateN = ++updateN;
                pulseSum += w.pulse;
                if (w.joule >= 0) {
                    jouleSum += w.joule;
                    w.jouleMn = (double) jouleSum / (double) updateN;
                }
                w.pulseMn = (double) pulseSum / (double) updateN;
                w.timeRms = lastTimeTot+timeTotms;
                w.timeRAbsms = now - sessionStart;
                w.timeR = (short) (w.timeRms / 1000.0 + 0.5);
                lastUpdate = now;
            } else if (diff>0)
                active = false;
        }
        if (!active && wasActive) {
            lastStart = -1;
            lastUpdate = -1;
            lastTimeTot += timeTotms;
        }
        wasActive = active;
        return !active;
    }

    @Override
    public void setOffsets() {
    }
}
