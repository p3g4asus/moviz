package com.moviz.lib.plot;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WahooBlueSCPlotProcessor extends PlotProcessor {
    public WahooBlueSCPlotProcessor() {
        super();
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.crankminutes"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.crankrev"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.wheelminutes"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.speed"));
    }

    @Override
    public PHolderSetter getPlotVars(com.moviz.lib.comunication.holder.SessionHolder ses, int maxPoints, long offsetms, ProgressPub<Integer[]> pp) {
        List<DeviceUpdate> vals = ses.getValues();
        int dec = calcDecimation(vals.size(), maxPoints);
        double off = offsetms / 60000.0;
        com.moviz.lib.comunication.holder.Holder hld;
        List<Double> crankminutes = (List<Double>) (hld = plotVars.getP(".crankminutes")).getList();
        if (crankminutes == null) {
            crankminutes = new ArrayList<Double>();
            hld.sO(crankminutes);
        }
        List<Double> crankrev = (List<Double>) (hld = plotVars.getP(".crankrev")).getList();
        if (crankrev == null) {
            crankrev = new ArrayList<Double>();
            hld.sO(crankrev);
        }
        List<Double> wheelminutes = (List<Double>) (hld = plotVars.getP(".wheelminutes")).getList();
        if (wheelminutes == null) {
            wheelminutes = new ArrayList<Double>();
            hld.sO(wheelminutes);
        }
        List<Double> speed = (List<Double>) (hld = plotVars.getP(".speed")).getList();
        if (speed == null) {
            speed = new ArrayList<Double>();
            hld.sO(speed);
        }
        speed.clear();
        crankrev.clear();
        wheelminutes.clear();
        crankminutes.clear();
        int n = vals.size();
        Integer[] progInt = new Integer[2];
        progInt[1] = n;
        n = n / dec;
        ((ArrayList<Double>) speed).ensureCapacity(n);
        ((ArrayList<Double>) crankrev).ensureCapacity(n);
        ((ArrayList<Double>) wheelminutes).ensureCapacity(n);
        ((ArrayList<Double>) crankminutes).ensureCapacity(n);
        double speedd = 0.0, crankrevd = 0.0, wheelminutesd = 0.0, crankminutesd = 0.0, mins = 0.0;
        int ncrank = 0, nwheel = 0;
        int i = 0;
        for (com.moviz.lib.comunication.holder.DeviceUpdate ww : vals) {
            com.moviz.lib.comunication.holder.WahooBlueSCHolder w = (com.moviz.lib.comunication.holder.WahooBlueSCHolder) ww;
            if (w.sensType == WahooBlueSCHolder.SensorType.CRANK) {
                crankminutesd += (mins = w.timeRAbsms / 60000.0 + off);
                crankrevd += w.sensSpd;
                if (++ncrank == dec) {
                    if (dec == 1) {
                        crankminutes.add(mins);
                        crankrev.add(w.sensSpd);
                    } else {
                        crankminutes.add(crankminutesd / (double) dec);
                        crankrev.add(crankrevd / (double) dec);
                        crankminutesd = 0.0;
                        crankrevd = 0.0;
                    }
                    ncrank = 0;
                }
            } else {
                wheelminutesd += (mins = w.timeRAbsms / 60000.0 + off);
                speedd += w.sensSpd;
                if (++nwheel == dec) {
                    if (dec == 1) {
                        wheelminutes.add(mins);
                        speed.add(w.sensSpd);
                    } else {
                        wheelminutes.add(wheelminutesd / (double) dec);
                        speed.add(speedd / (double) dec);
                        wheelminutesd = 0.0;
                        speedd = 0.0;
                    }
                    nwheel = 0;
                }
            }
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }
        return plotVars;
    }
}
