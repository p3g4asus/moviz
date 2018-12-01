package com.moviz.lib.sessionexport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;
import com.moviz.lib.comunication.plus.holder.PZephyrHxMHolder;
import com.moviz.lib.plot.ProgressPub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ZephyrHxMSessionExporter implements SessionExporter {

    @Override
    public ArrayList<MLArray> export(com.moviz.lib.comunication.holder.SessionHolder ses, double offsetms, String path,
                                     ProgressPub<Integer[]> pp) throws IOException {
        ArrayList<MLArray> rv = new ArrayList<MLArray>();
        List<com.moviz.lib.comunication.holder.DeviceUpdate> vals = ses.getValues();
        double offs = offsetms / 1000.0;
        int ssize = 0, psize = 0;
        Integer[] progInt = new Integer[2];
        progInt[1] = vals.size();
        int i = 0;
        for (com.moviz.lib.comunication.holder.DeviceUpdate ww : vals) {
            PZephyrHxMHolder w = (PZephyrHxMHolder) ww;
            if (w.distance < 0)
                psize++;
            else
                ssize++;
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }

        Double[] distance_a = new Double[ssize];
        Double[] speed_a = new Double[ssize];
        Byte[] pulse_a = new Byte[ssize];
        Byte[] battery_a = new Byte[ssize];
        Long[] strides_a = new Long[ssize];
        Long[] nbeats_a = new Long[ssize];
        Short[] time_a = new Short[ssize];
        Double[] cardio_a = new Double[psize];
        int s = 0, p = 0;
        i = 0;
        for (com.moviz.lib.comunication.holder.DeviceUpdate ww : vals) {
            PZephyrHxMHolder w = (PZephyrHxMHolder) ww;
            if (w.distance < 0)
                cardio_a[p++] = w.timeRAbsms / 1000.0 + offs;
            else {
                distance_a[s] = w.distanceR;
                speed_a[s] = w.speed;
                nbeats_a[s] = (long) w.nBeatsR;
                time_a[s] = (short) ((w.timeRAbsms / 1000.0 + 0.5) + offs);
                strides_a[s] = (long) w.stridesR;
                battery_a[s] = w.battery;
                pulse_a[s++] = (byte) w.pulse;
            }
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }


        rv.add(new MLDouble("distance", distance_a, 1));
        rv.add(new MLDouble("speed", speed_a, 1));
        rv.add(new MLUInt8("pulse", pulse_a, 1));
        rv.add(new MLUInt8("battery", battery_a, 1));
        rv.add(new MLUInt64("strides", strides_a, 1));
        rv.add(new MLUInt64("nbeats", nbeats_a, 1));
        rv.add(new MLDouble("cardio", cardio_a, 1));
        rv.add(new MLInt16("time", time_a, 1));
        com.moviz.lib.comunication.holder.DeviceHolder devh = ses.getDevice();
        new MatFileWriter(path + "/" + devh.getType().name() + "." + devh.getAlias() + ".mat", rv);
        return rv;
    }

}
