package com.moviz.lib.sessionexport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt32;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.plot.ProgressPub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WahooBlueSCSessionExporter implements SessionExporter {
    @Override
    public ArrayList<MLArray> export(com.moviz.lib.comunication.holder.SessionHolder ses, double offsetms,
                                     String path, ProgressPub<Integer[]> pp) throws IOException {
        ArrayList<MLArray> rv = new ArrayList<>();
        List<DeviceUpdate> vals = ses.getValues();
        double offs = offsetms / 1000.0;
        int ssize = vals.size();
        Integer[] progInt = new Integer[2];
        progInt[1] = ssize;
        int i = 0;
        Double[] distance_a = new Double[ssize];
        Double[] crankrpn_a = new Double[ssize];
        Double[] wheelkh_a = new Double[ssize];
        Integer[] crankn_a = new Integer[ssize];
        Integer[] wheeln_a = new Integer[ssize];
        Short[] tcrank_a = new Short[ssize];
        Short[] twheel_a = new Short[ssize];
        Short[] calorie_a = new Short[ssize];
        int ncrank = 0, nwheel = 0;
        i = 0;
        for (DeviceUpdate ww : vals) {
            WahooBlueSCHolder w = (WahooBlueSCHolder) ww;
            if (w.sensType == WahooBlueSCHolder.SensorType.CRANK) {
                crankrpn_a[ncrank] = w.sensSpd;
                tcrank_a[ncrank] = (short) ((w.timeRAbsms / 1000.0 + 0.5) + offs);
                crankn_a[ncrank] = (int) w.sensVal;
                ncrank++;
            } else {
                distance_a[nwheel] = w.distance;
                calorie_a[nwheel] = w.calorie;
                wheeln_a[nwheel] = (int) w.sensVal;
                wheelkh_a[nwheel] = w.sensSpd;
                twheel_a[nwheel] = (short) ((w.timeRAbsms / 1000.0 + 0.5) + offs);
                nwheel++;
            }

            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }


        rv.add(new MLDouble("distance", Arrays.copyOf(distance_a, nwheel), 1));
        rv.add(new MLInt16("calorie", Arrays.copyOf(calorie_a, nwheel), 1));
        rv.add(new MLDouble("wheelkh", Arrays.copyOf(wheelkh_a, nwheel), 1));
        rv.add(new MLInt32("wheeln", Arrays.copyOf(wheeln_a, nwheel), 1));
        rv.add(new MLInt16("twheel", Arrays.copyOf(twheel_a, nwheel), 1));
        rv.add(new MLDouble("crankkh", Arrays.copyOf(crankrpn_a, ncrank), 1));
        rv.add(new MLInt32("crankn", Arrays.copyOf(crankn_a, ncrank), 1));
        rv.add(new MLInt16("tcrank", Arrays.copyOf(tcrank_a, ncrank), 1));
        DeviceHolder devh = ses.getDevice();
        new MatFileWriter(path + "/" + devh.getType().name() + "." + devh.getAlias() + ".mat", rv);
        return rv;
    }
}
