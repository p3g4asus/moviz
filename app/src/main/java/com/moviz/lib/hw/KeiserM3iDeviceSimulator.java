package com.moviz.lib.hw;

import android.util.Log;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;

public class KeiserM3iDeviceSimulator extends PafersDeviceSimulator {
    private static String TAG = KeiserM3iDeviceSimulator.class.getSimpleName();
    protected final static int DIST_BUFF_SIZE = 165;
    protected int dist_buff_size = 0;
    protected int dist_buff_idx = 0;
    protected int buffSize = DIST_BUFF_SIZE;
    protected double[] dist_buff = new double[DIST_BUFF_SIZE];
    protected long[] dist_buff_time = new long[DIST_BUFF_SIZE];
    protected double dist_acc = 0.0;
    protected double old_dist = -1.0;
    protected long timeRms_acc = 0;
    protected long old_timeRms = 0;

    private int equalTime;
    private short old_time_orig;

    private static int EQUAL_TIME_THRESHOLD = 4;

    public void setBuffSize(int siz) {
        if (siz!=buffSize) {
            this.buffSize = siz;
            dist_buff = new double[buffSize];
            dist_buff_time = new long[buffSize];
            dist_buff_idx = 0;
            dist_buff_size = 0;
            dist_acc = 0.0;
            old_dist = -1.0;
            timeRms_acc = 0;
            old_timeRms = 0;
        }
    }

    @Override
    protected void detectPause(PPafersHolder f) {
        if (f.time == old_time_orig) {
            if (equalTime < EQUAL_TIME_THRESHOLD)
                equalTime++;
            Log.v(TAG,"EqualTime "+equalTime);
        } else {
            equalTime = 0;
            old_time_orig = f.time;
        }
    }

    @Override
    public boolean inPause() {
        return equalTime >= EQUAL_TIME_THRESHOLD || System.currentTimeMillis()-lastUpdateTime>=PAUSE_DELAY_DETECT_THRESHOLD;
    }


    @Override
    public void reset() {
        super.reset();
        equalTime = 0;
        old_time_orig = -1;
        dist_buff_size = 0;
        dist_buff_idx = 0;
        dist_acc = 0.0;
        timeRms_acc = 0;
        old_dist = -1.0;
        old_timeRms = 0;
        dist_buff = new double[buffSize];
        dist_buff_time = new long[buffSize];
    }

    @Override
    protected double calcSpeed(PPafersHolder f,boolean pause) {
        double realdist;
        long realtime;
        realdist = f.distance+distance_o;
        realtime = f.time+time_o;
        //realtime = f.time + time_o;
        if (old_dist<0) {
            old_dist = realdist;
            old_timeRms = realtime;
            Log.v(TAG,"Init: old_dist = "+realdist+" old_time = "+realtime);
            f.speed = 0;
        }
        else {
            String logv;
            double acc;
            long acc_time = realtime - old_timeRms;
            if (!pause && ((acc = realdist - old_dist)>1e-6 || acc_time>0)) {
                double rem = 0.0;
                long rem_time = 0;
                if (dist_buff_size == buffSize) {
                    if (dist_buff_idx == buffSize)
                        dist_buff_idx = 0;
                    dist_acc -= (rem = dist_buff[dist_buff_idx]);
                    timeRms_acc -= (rem_time = dist_buff_time[dist_buff_idx]);
                } else {
                    dist_buff_size++;
                }
                dist_buff_time[dist_buff_idx] = acc_time;
                dist_buff[dist_buff_idx++] = acc;
                dist_acc += acc;
                timeRms_acc += acc_time;


                old_dist = realdist;
                old_timeRms = realtime;
                logv = "D = ("+realdist+","+acc+"->"+rem+","+dist_acc+") T = ("+realtime+","+acc_time+"->"+rem_time+","+timeRms_acc+") => ";
            }
            else
                logv = "P D = ("+realdist+",- -> -,"+dist_acc+") T = ("+realtime+",- -> -,"+timeRms_acc+") => ";

            if (timeRms_acc == 0)
                f.speed = 0;
            else
                f.speed = dist_acc / ((double) timeRms_acc / 3600.00);
            Log.v(TAG,logv+f.speed);
        }
        return f.speed;
    }

    @Override
    public boolean step(DeviceUpdate du) {
        PPafersHolder f = (PPafersHolder) du;
        if (old_time_orig>f.time)
            setOffsets();
        boolean out =  super.step(du);
        f.pulse/=10;
        f.pulseMn/=10.0;
        f.rpm/=10;
        f.rpmMn/=10.0;
        return out;
    }
}
