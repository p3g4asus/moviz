package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.ZephyrHxMHolder;

public class ZephyrHxMDeviceSimulator implements DeviceSimulator {
    private short oldRawDistance;
    private int oldDistanceR;
    private short oldStrides;
    private int oldStridesR;
    private short oldHeartBeat;
    private long sessionStart;
    private int oldTs;
    private int oldTsR;
    private int heartRateN;
    private int speedN;
    private int updateN;
    private int heartRateSum;
    private int speedSum;
    private int nBeats;
    private long lastUpdateTime;
    private long timeTotms;
    private long lastStart;
    private long lastTimeTot;
    private boolean wasActive;

    @Override
    public void reset() {
        oldRawDistance = -1;
        oldStrides = -1;
        oldHeartBeat = -1;
        oldStridesR = 0;
        oldDistanceR = 0;
        sessionStart = 0;
        oldTs = -1;
        oldTsR = 0;
        heartRateN = 0;
        speedN = 0;
        updateN = 0;
        heartRateSum = 0;
        speedSum = 0;
        nBeats = 0;
        lastUpdateTime = -1;
        lastStart = -1;
        timeTotms = 0;
        lastTimeTot = 0;
        wasActive = false;
    }

    public ZephyrHxMDeviceSimulator() {
        reset();
    }

    @Override
    public boolean step(DeviceUpdate du) {
        ZephyrHxMHolder w = (ZephyrHxMHolder) du;
        long now = System.currentTimeMillis();
        boolean active = oldRawDistance >= 0;
        if (active) {
            short hb;
            if (sessionStart == 0)
                sessionStart = now;
            if (lastStart < 0)
                lastStart = now;
            long diff = now - lastStart;
            if (diff>0 && (lastUpdateTime<0 || now-lastUpdateTime<PAUSE_DELAY_DETECT_THRESHOLD)) {
                timeTotms = diff;
                if (w.rawDistance < oldRawDistance)
                    oldDistanceR += 4096 + (w.rawDistance - oldRawDistance);
                else
                    oldDistanceR += (w.rawDistance - oldRawDistance);
                w.distanceR = ((double) oldDistanceR) / 16.0 / 1000.0;
                if (w.strides < oldStrides)
                    oldStridesR += 256 + (w.strides - oldStrides);
                else
                    oldStridesR += (w.strides - oldStrides);
                w.stridesR = oldStridesR;
                if (w.heartBeat < oldHeartBeat)
                    hb = (short) (256 + (w.heartBeat - oldHeartBeat));
                else
                    hb = (short) (w.heartBeat - oldHeartBeat);
                w.okts = (byte) Math.min(15, hb);
                if (w.pulse == 0)
                    oldTs = -1;
                else {
                    heartRateSum += w.pulse;
                    heartRateN++;
                }
                if (w.speed != 0.0) {
                    speedSum += w.speed;
                    speedN++;
                }
                if (w.okts > 0) {
                    nBeats += w.okts;
                    if (oldTs == -1) {
                        int adj = w.ts[0] - w.ts[w.okts - 1];
                        if (adj < 0)
                            adj += 65536;
                        oldTs = w.ts[w.okts - 1];
                        oldTsR = (int) (now - adj - sessionStart);
                        //Log.i("ciao","1ts o="+oldTs+" r="+oldTsR);
                    }
                    int dif;
                    for (int i = w.okts - 1; i >= 0; i--) {
                        w.tsR[i] = ((dif = w.ts[i] - oldTs) < 0 ? dif + 65536 : dif) + oldTsR;
                        oldTs = w.ts[i];
                        oldTsR = w.tsR[i];
                        //Log.i("ciao","2ts o="+oldTs+" r="+oldTsR+" d="+dif+" k"+w.okts);
                    }

                }
                w.nBeatsR = nBeats;
                w.updateN = ++updateN;
                w.timeRms = lastTimeTot+timeTotms;
                w.timeRAbsms = now - sessionStart;
                w.timeR = (short) (w.timeRms / 1000.0 + 0.5);
                if (heartRateN > 0)
                    w.pulseMn = (double) heartRateSum / (double) heartRateN;
                if (speedN > 0)
                    w.speedMn = (double) speedSum / (double) speedN;
            }
            else if (diff>0)
                active = false;
        }
        if (!active && wasActive) {
            lastUpdateTime = -1;
            lastStart = -1;
            oldRawDistance = -1;
            oldStrides = -1;
            oldHeartBeat = -1;
            lastTimeTot += timeTotms;
        }
        wasActive = active;
        oldRawDistance = w.rawDistance;
        oldStrides = w.strides;
        oldHeartBeat = w.heartBeat;
        return !active;
    }

    @Override
    public void setOffsets() {
    }
}
