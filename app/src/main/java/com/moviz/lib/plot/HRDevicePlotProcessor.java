package com.moviz.lib.plot;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.plus.holder.PHRDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;

import java.util.ArrayList;
import java.util.List;

public class HRDevicePlotProcessor extends PlotProcessor {

    public HRDevicePlotProcessor() {
        super();
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.minutes"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.pulse"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.joule"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.seconds"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.cardio"));
    }

    @Override
    public PHolderSetter getPlotVars(com.moviz.lib.comunication.holder.SessionHolder ses, int maxPoints, long offsetms, ProgressPub<Integer[]> pp) {
        List<DeviceUpdate> vals = ses.getValues();
        Holder hld;
        double offm = offsetms / 60000.0, offs = offsetms / 1000.0;
        List<Double> minutes = (List<Double>) (hld = plotVars.getP(".minutes")).getList();
        if (minutes == null) {
            minutes = new ArrayList<Double>();
            hld.sO(minutes);
        }
        List<Double> pulse = (List<Double>) (hld = plotVars.getP(".pulse")).getList();
        if (pulse == null) {
            pulse = new ArrayList<Double>();
            hld.sO(pulse);
        }
        List<Double> joule = (List<Double>) (hld = plotVars.getP(".joule")).getList();
        if (joule == null) {
            joule = new ArrayList<Double>();
            hld.sO(joule);
        }
        List<Double> secs = (List<Double>) (hld = plotVars.getP(".seconds")).getList();
        if (secs == null) {
            secs = new ArrayList<Double>();
            hld.sO(secs);
        }
        List<Double> cardio = (List<Double>) (hld = plotVars.getP(".cardio")).getList();
        if (cardio == null) {
            cardio = new ArrayList<Double>();
            hld.sO(cardio);
        }
        joule.clear();
        secs.clear();
        pulse.clear();
        minutes.clear();
        cardio.clear();
        int decstatus = 0;
        int otherP = 0;
        secs.add(0.0);
        cardio.add(0.0);
        Integer[] progInt = new Integer[2];
        progInt[1] = vals.size();
        double timeTemp;
        int i = 0;
        long lastTime = 0;
        for (DeviceUpdate ww : vals) {
            PHRDeviceHolder w = (PHRDeviceHolder) ww;
            if (w.pulse < 0) {
                timeTemp = lastTime / 1000.0 + offs - w.timeRAbsms / 1024.0;
                secs.add(timeTemp - 1e-3);
                secs.add(timeTemp);
                secs.add(timeTemp + 1e-3);
                cardio.add(0.0);
                cardio.add(1.0);
                cardio.add(0.0);
            } else {
                lastTime = w.timeRAbsms;
                otherP++;
            }
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }
        int dec = calcDecimation(otherP, maxPoints);
        otherP = otherP / dec;
        ((ArrayList<Double>) joule).ensureCapacity(otherP);
        ((ArrayList<Double>) minutes).ensureCapacity(otherP);
        ((ArrayList<Double>) pulse).ensureCapacity(otherP);

        double jouled = 0.0, pulsed = 0.0, minutesd = 0.0, mins;
        for (DeviceUpdate ww : vals) {
            PHRDeviceHolder w = (PHRDeviceHolder) ww;
            if (w.pulse >= 0) {
                jouled += w.joule;
                pulsed += w.pulse;
                minutesd += (mins = w.timeRAbsms / 60000.0 + offm);
                if (++decstatus == dec) {
                    if (dec == 1) {
                        if (w.joule >= 0)
                            joule.add((double) w.joule);
                        pulse.add((double) w.pulse);
                        minutes.add(mins);
                    } else {
                        if (w.joule >= 0)
                            joule.add(jouled / (double) dec);
                        pulse.add(pulsed / (double) dec);
                        minutes.add(minutesd / (double) dec);
                        jouled = 0.0;
                        pulsed = 0.0;
                        minutesd = 0.0;
                    }
                    decstatus = 0;
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
