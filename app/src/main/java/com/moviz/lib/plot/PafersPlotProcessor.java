package com.moviz.lib.plot;

import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;

import java.util.ArrayList;
import java.util.List;

public class PafersPlotProcessor extends PlotProcessor {

    public PafersPlotProcessor() {
        super();
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.minutes"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.rpm"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.speed"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.watt"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.pulse"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.incline"));
    }

    @Override
    public PHolderSetter getPlotVars(com.moviz.lib.comunication.holder.SessionHolder ses, int maxPoints, long offsetms, ProgressPub<Integer[]> pp) {
        List<com.moviz.lib.comunication.holder.DeviceUpdate> vals = ses.getValues();
        int dec = calcDecimation(vals.size(), maxPoints);
        double off = offsetms / 60000.0;
        com.moviz.lib.comunication.holder.Holder hld;
        List<Double> minutes = (List<Double>) (hld = plotVars.getP(".minutes")).getList();
        if (minutes == null) {
            minutes = new ArrayList<Double>();
            hld.sO(minutes);
        }
        List<Double> rpm = (List<Double>) (hld = plotVars.getP(".rpm")).getList();
        if (rpm == null) {
            rpm = new ArrayList<Double>();
            hld.sO(rpm);
        }
        List<Double> speed = (List<Double>) (hld = plotVars.getP(".speed")).getList();
        if (speed == null) {
            speed = new ArrayList<Double>();
            hld.sO(speed);
        }
        List<Double> watt = (List<Double>) (hld = plotVars.getP(".watt")).getList();
        if (watt == null) {
            watt = new ArrayList<Double>();
            hld.sO(watt);
        }
        List<Double> pulse = (List<Double>) (hld = plotVars.getP(".pulse")).getList();
        if (pulse == null) {
            pulse = new ArrayList<Double>();
            hld.sO(pulse);
        }
        List<Double> incline = (List<Double>) (hld = plotVars.getP(".incline")).getList();
        if (incline == null) {
            incline = new ArrayList<Double>();
            hld.sO(incline);
        }
        speed.clear();
        incline.clear();
        watt.clear();
        pulse.clear();
        minutes.clear();
        rpm.clear();
        int n = vals.size();
        Integer[] progInt = new Integer[2];
        progInt[1] = n;
        n = n / dec;
        ((ArrayList<Double>) speed).ensureCapacity(n);
        ((ArrayList<Double>) incline).ensureCapacity(n);
        ((ArrayList<Double>) watt).ensureCapacity(n);
        ((ArrayList<Double>) pulse).ensureCapacity(n);
        ((ArrayList<Double>) minutes).ensureCapacity(n);
        ((ArrayList<Double>) rpm).ensureCapacity(n);
        double speedd = 0.0, inclined = 0.0, wattd = 0.0, pulsed = 0.0, minutesd = 0.0, rpmd = 0.0, mins;
        int decstatus = 0;
        int i = 0;
        for (com.moviz.lib.comunication.holder.DeviceUpdate ww : vals) {
            com.moviz.lib.comunication.holder.PafersHolder w = (com.moviz.lib.comunication.holder.PafersHolder) ww;
            speedd += w.speed;
            inclined += w.incline;
            wattd += w.watt;
            pulsed += w.pulse;
            minutesd += (mins = w.timeRAbsms / 60000.0 + off);
            rpmd += w.rpm;
            if (++decstatus == dec) {
                if (dec == 1) {
                    speed.add(w.speed);
                    incline.add((double) w.incline);
                    watt.add((double) w.watt);
                    pulse.add((double) w.pulse);
                    minutes.add(mins);
                    rpm.add((double) w.rpm);
                } else {
                    speed.add(speedd / (double) dec);
                    incline.add(inclined / (double) dec);
                    watt.add(wattd / (double) dec);
                    pulse.add(pulsed / (double) dec);
                    minutes.add(minutesd / (double) dec);
                    rpm.add(rpmd / (double) dec);
                    speedd = 0.0;
                    inclined = 0.0;
                    wattd = 0.0;
                    pulsed = 0.0;
                    minutesd = 0.0;
                    rpmd = 0.0;
                }
                decstatus = 0;
            }
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }
        return plotVars;
    }

}
