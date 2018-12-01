package com.moviz.lib.plot;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.plus.holder.PHolder;
import com.moviz.lib.comunication.plus.holder.PHolderSetter;
import com.moviz.lib.comunication.plus.holder.PZephyrHxMHolder;

import java.util.ArrayList;
import java.util.List;

public class ZephyrHxMPlotProcessor extends PlotProcessor {

    public ZephyrHxMPlotProcessor() {
        super();
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.x.minutes"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.pulse"));
        plotVars.add(new PHolder(PHolder.Type.LIST, "plot.y.speed"));
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
        List<Double> speed = (List<Double>) (hld = plotVars.getP(".speed")).getList();
        if (speed == null) {
            speed = new ArrayList<Double>();
            hld.sO(speed);
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
        speed.clear();
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
        for (DeviceUpdate ww : vals) {
            PZephyrHxMHolder w = (PZephyrHxMHolder) ww;
            if (w.distance < 0) {
                timeTemp = w.timeRAbsms / 1000.0 + offs;
                secs.add(timeTemp - 1e-3);
                secs.add(timeTemp);
                secs.add(timeTemp + 1e-3);
                cardio.add(0.0);
                cardio.add(1.0);
                cardio.add(0.0);
            } else
                otherP++;
            if (pp != null) {
                progInt[0] = ++i;
                pp.calcProgress(progInt, progInt[0], progInt[1]);
            }
        }
        int dec = calcDecimation(otherP, maxPoints);
        otherP = otherP / dec;
        ((ArrayList<Double>) speed).ensureCapacity(otherP);
        ((ArrayList<Double>) minutes).ensureCapacity(otherP);
        ((ArrayList<Double>) pulse).ensureCapacity(otherP);

        double speedd = 0.0, pulsed = 0.0, minutesd = 0.0, mins;
        for (DeviceUpdate ww : vals) {
            PZephyrHxMHolder w = (PZephyrHxMHolder) ww;
            if (w.distance >= 0) {
                speedd += w.speed;
                pulsed += w.pulse;
                minutesd += (mins = w.timeRAbsms / 60000.0 + offm);
                if (++decstatus == dec) {
                    if (dec == 1) {
                        speed.add(w.speed);
                        pulse.add((double) w.pulse);
                        minutes.add(mins);
                    } else {
                        speed.add(speedd / (double) dec);
                        pulse.add(pulsed / (double) dec);
                        minutes.add(minutesd / (double) dec);
                        speedd = 0.0;
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
