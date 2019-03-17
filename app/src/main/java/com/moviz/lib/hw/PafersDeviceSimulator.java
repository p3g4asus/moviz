package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;

public class PafersDeviceSimulator implements DeviceSimulator {
    private static final int DETECT_PAUSE_THRESHOLD = 2;
    private static final int FIRST_MOVE_THRESHOLD = 5;
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
    protected short time_o;
    protected short calorie_o;
    protected double distance_o;
    protected short time_old;
    protected short calorie_old;
    protected double distance_old;

    private int zeroRpm;
    private int firstMove;

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

    protected void detectPause(PPafersHolder f) {
        if (f.rpm == 0) {
            if (zeroRpm < DETECT_PAUSE_THRESHOLD)
                zeroRpm++;
            firstMove = 0;
        } else {
            zeroRpm = 0;
            if (firstMove < FIRST_MOVE_THRESHOLD)
                firstMove++;
        }
    }

    protected double calcSpeed(PPafersHolder f) {
        return f.speed;
    }

    protected void fillTimeRFields(PPafersHolder f,long updateTime) {
        f.timeRms = sumTime;
        f.timeRAbsms = updateTime - sessionStart;
        f.timeR = (short) ((double) sumTime / 1000.0 + 0.5);
    }


    @Override
    public boolean step(DeviceUpdate du) {
        PPafersHolder f = (PPafersHolder) du;
        long now = System.currentTimeMillis();
        boolean wasinpause = inPause();
        detectPause(f);
        if (!wasinpause && !inPause()) {
            sumTime += (now - lastUpdateTime);
            fillTimeRFields(f,now);
            nUpdates++;
            sumSpeed += calcSpeed(f);
            sumRpm += f.rpm;
            sumWatt += f.watt;
            if (f.pulse > VALID_PULSE_THRESHOLD) {
                sumPulse += f.pulse;
                nPulses++;
            }
        }
        else {
            fillTimeRFields(f,now);
            calcSpeed(f);
        }
        if (nPulses > 0)
            f.pulseMn = (double) sumPulse / (double) nPulses;
        if (sessionStart == 0)
            sessionStart = now;
        if (nUpdates > 0) {
            f.rpmMn = (double) sumRpm / (double) nUpdates;
            f.wattMn = (double) sumWatt / (double) nUpdates;
            f.speedMn = sumSpeed / (double) nUpdates;
            if (sumTime<=0)
                f.distanceR = 0.0;
            else
                f.distanceR = f.speedMn * ((double) sumTime / 3600000.0);
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