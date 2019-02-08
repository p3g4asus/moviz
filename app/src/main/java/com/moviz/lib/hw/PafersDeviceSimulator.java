package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;

public class PafersDeviceSimulator implements DeviceSimulator {
    protected static final int DETECT_PAUSE_THRESHOLD = 2;
    protected static final int FIRST_MOVE_THRESHOLD = 5;
    protected static final int VALID_PULSE_THRESHOLD = 50;

    protected long lastUpdateTime;
    protected long sessionStart;
    protected int nUpdates;
    protected int nPulses;
    protected long sumWatt;
    protected long sumTime;
    protected double sumSpeed;
    protected long sumPulse;
    protected long sumRpm;
    protected int firstMove;
    protected short time_o;
    protected short calorie_o;
    protected double distance_o;
    protected short time_old;
    protected short calorie_old;
    protected double distance_old;

    protected int zeroRpm;

    public PafersDeviceSimulator() {
        reset();
    }

    @Override
    public void reset() {
        lastUpdateTime = 0;
        sessionStart = 0;
        nUpdates = 0;
        nPulses = 0;
        sumWatt = 0;
        sumTime = 0;
        sumSpeed = 0.0;
        sumPulse = 0;
        sumRpm = 0;
        zeroRpm = 0;
        firstMove = 0;
        time_o = 0;
        calorie_o = 0;
        distance_o = 0.0;
        time_old = 0;
        distance_old = 0.0;
        calorie_old = 0;
    }

    public boolean inPause() {
        return firstMove < FIRST_MOVE_THRESHOLD || zeroRpm >= DETECT_PAUSE_THRESHOLD || System.currentTimeMillis()-lastUpdateTime>=PAUSE_DELAY_DETECT_THRESHOLD;
    }


    @Override
    public boolean step(DeviceUpdate du) {
        PPafersHolder f = (PPafersHolder) du;
        long now = System.currentTimeMillis();
        boolean wasinpause = false;
        if (f.rpm == 0) {
            if (zeroRpm < DETECT_PAUSE_THRESHOLD)
                zeroRpm++;
            firstMove = 0;
        } else {
            wasinpause = inPause();
            zeroRpm = 0;
            if (firstMove < FIRST_MOVE_THRESHOLD)
                firstMove++;
        }
        if (!wasinpause && !inPause()) {
            sumTime += (now - lastUpdateTime);
            nUpdates++;
            sumSpeed += f.speed;
            sumRpm += f.rpm;
            sumWatt += f.watt;
            if (f.pulse > VALID_PULSE_THRESHOLD) {
                sumPulse += f.pulse;
                nPulses++;
            }
        }
        if (nPulses > 0)
            f.pulseMn = (double) sumPulse / (double) nPulses;
        if (sessionStart == 0)
            sessionStart = now;
        if (nUpdates > 0) {
            f.rpmMn = (double) sumRpm / (double) nUpdates;
            f.wattMn = (double) sumWatt / (double) nUpdates;
            f.speedMn = (double) sumSpeed / (double) nUpdates;
            f.timeRms = sumTime;
            f.timeRAbsms = now - sessionStart;
            double tm = (double) sumTime / 1000.0;
            f.timeR = (short) (tm + 0.5);
            f.distanceR = f.speedMn * (tm / 3600.0);
        }
        f.updateN = nUpdates;
        f.time +=time_o;
        f.calorie +=calorie_o;
        f.distance +=distance_o;
        time_old = f.time;
        calorie_old = f.calorie;
        distance_old = f.distance;
        lastUpdateTime = now;
        return inPause();
    }

    @Override
    public void setOffsets() {
        time_o = time_old;
        calorie_o = calorie_old;
        distance_o = distance_old;
    }
}

/*
 * Location: C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar
 * 
 * Qualified Name:
 * com.pafers.fitnesshwapi.lib.device.FitnessHwApiDeviceSimulator
 * 
 * JD-Core Version: 0.7.0.1
 */