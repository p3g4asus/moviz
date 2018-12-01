package com.moviz.lib.sessionexport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLUInt8;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.PafersHolder;
import com.moviz.lib.plot.ProgressPub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PafersSessionExporter implements SessionExporter {

    @Override
    public ArrayList<MLArray> export(com.moviz.lib.comunication.holder.SessionHolder ses, double offsetms,
                                     String path, ProgressPub<Integer[]> pp) throws IOException {
        ArrayList<MLArray> rv = new ArrayList<MLArray>();
        List<DeviceUpdate> vals = ses.getValues();
        double offs = offsetms / 1000.0;
        int ssize = vals.size();
        Integer[] progInt = new Integer[2];
        progInt[1] = ssize;
        int i = 0;
        Double[] distance_a = new Double[ssize];
        Double[] speed_a = new Double[ssize];
        Byte[] pulse_a = new Byte[ssize];
        Byte[] incline_a = new Byte[ssize];
        Short[] rpm_a = new Short[ssize];
        Short[] watt_a = new Short[ssize];
        Short[] calorie_a = new Short[ssize];
        Short[] time_a = new Short[ssize];
        int s = 0;
        i = 0;
        for (DeviceUpdate ww : vals) {
            PafersHolder w = (PafersHolder) ww;
            distance_a[s] = w.distanceR;
            speed_a[s] = w.speed;
            watt_a[s] = w.watt;
            time_a[s] = (short) ((w.timeRAbsms / 1000.0 + 0.5) + offs);
            rpm_a[s] = (short) w.rpm;
            calorie_a[s] = w.calorie;
            pulse_a[s] = (byte) w.pulse;
            incline_a[s++] = (byte) w.incline;
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }


        rv.add(new MLDouble("distance", distance_a, 1));
        rv.add(new MLDouble("speed", speed_a, 1));
        rv.add(new MLUInt8("pulse", pulse_a, 1));
        rv.add(new MLUInt8("incline", incline_a, 1));
        rv.add(new MLInt16("watt", watt_a, 1));
        rv.add(new MLInt16("rpm", rpm_a, 1));
        rv.add(new MLInt16("calorie", calorie_a, 1));
        rv.add(new MLInt16("time", time_a, 1));
        DeviceHolder devh = ses.getDevice();
        new MatFileWriter(path + "/" + devh.getType().name() + "." + devh.getAlias() + ".mat", rv);
        return rv;
    }

}
