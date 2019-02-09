package com.moviz.lib.hw;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.plus.holder.PPafersHolder;

public class KeiserM3iDeviceSimulator extends PafersDeviceSimulator {
    protected final static int DIST_BUFF_SIZE = 165;
    protected short time_o_2 = -1;
    protected short calorie_o_2 = 0;
    protected double distance_o_2 = 0;
    protected short last_received_time = 0;
    protected int dist_buff_size = 0;
    protected int dist_buff_idx = 0;
    protected int buffSize = DIST_BUFF_SIZE;
    protected double[] dist_buff = new double[DIST_BUFF_SIZE];
    protected long[] dist_buff_time = new long[DIST_BUFF_SIZE];
    protected double dist_acc = 0.0;
    protected double old_dist = -1.0;

    public void setBuffSize(int siz) {
        if (siz!=buffSize) {
            this.buffSize = siz;
            dist_buff = new double[buffSize];
            dist_buff_time = new long[buffSize];
            dist_buff_idx = 0;
            dist_buff_size = 0;
            dist_acc = 0.0;
            old_dist = -1.0;
        }
    }


    @Override
    public void reset() {
        super.reset();
        time_o_2 = -1;
        calorie_o_2 = 0;
        distance_o_2 = 0;
        last_received_time = 0;
        dist_buff_size = 0;
        dist_buff_idx = 0;
        dist_acc = 0.0;
        old_dist = -1.0;
        dist_buff = new double[buffSize];
        dist_buff_time = new long[buffSize];
    }

    @Override
    public boolean step(DeviceUpdate du) {
        PPafersHolder f = (PPafersHolder) du;
        long now = System.currentTimeMillis();
        long t;
        double realdist;
        if (time_o_2>=0) {
            if (Math.abs(f.time - last_received_time)>20) {
                time_o = time_o_2;
                calorie_o = calorie_o_2;
                distance_o = distance_o_2;
            }
            time_o_2 = -1;
        }
        last_received_time = f.time;
        realdist = f.distance+distance_o;
        if (old_dist<0) {
            old_dist = realdist;
            f.speed = 0;
        }
        else {
            double acc;
            if (dist_buff_size == buffSize) {
                if (dist_buff_idx == buffSize)
                    dist_buff_idx = 0;
                dist_acc -= dist_buff[dist_buff_idx];
                t = now - dist_buff_time[dist_buff_idx];
            } else {
                dist_buff_size++;
                t = dist_buff_size==1?0:now - dist_buff_time[0];
            }
            dist_buff_time[dist_buff_idx] = now;
            dist_buff[dist_buff_idx++] = acc = realdist-old_dist;
            dist_acc += acc;
            if (t == 0)
                f.speed = 0;
            else
                f.speed = dist_acc / ((double) t / 3600000.00);
            old_dist = realdist;
        }
        boolean out =  super.step(du);
        f.pulse/=10;
        f.pulseMn/=10.0;
        f.rpm/=10;
        f.rpmMn/=10.0;
        return out;
    }

    @Override
    public void setOffsets() {
        time_o_2 = time_old;
        calorie_o_2 = calorie_old;
        distance_o_2 = distance_old;
    }
}
