package com.moviz.lib.hw;

import android.util.Log;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;

import java.util.Arrays;

/**
 * Created by Matteo on 28/10/2016.
 */

public class WahooBlueSCDeviceSimulator implements DeviceSimulator {
    private static final int FIRST_MOVE_THRESHOLD = 0;
    protected final String TAG = getClass().getSimpleName();
    protected double[] gearFactor = new double[]{2.0,1.8};
    protected long wheelDiam = 667;
    protected PUserHolder user = null;

    private WheelRevIntUpdater wheel = new WheelRevIntUpdater();
    private CrankRevIntUpdater crank = new CrankRevIntUpdater();

    public void setWheelDiam(long wheelDiam) {
        this.wheelDiam = wheelDiam;
    }

    public void setUser(PUserHolder user) {
        this.user = user;
    }

    public void setGearFactor(double[] gearFactor) {
        this.gearFactor = gearFactor;
    }

    public void setCurrentGear(int currentGear) {
        this.wheel.setCurrentGear(currentGear);
        this.crank.setCurrentGear(currentGear);
    }

    private abstract static class IntUpdater {
        protected String TAG = getClass().getSimpleName();
        protected long lastUpdateTime;
        protected long lastValue;
        protected int nUpdates;
        protected long sumTime;
        protected double sumSpeed;
        protected int firstMove;
        protected long sensVal_o, sensVal_old;
        protected long wheelDiam = 667;
        protected PUserHolder user = null;
        protected int currentGear = 0;
        protected double[] gearFactor = new double[]{2.0,1.8};
        protected long[] timeGear = null;
        protected long[] sensGear = null;

        public void setCurrentGear(int currentGear) {
            this.currentGear = currentGear;
        }

        public void setGearFactor(double[] gearFactor) {
            this.gearFactor = gearFactor;
            timeGear = new long[gearFactor.length];
            sensGear = new long[gearFactor.length];
        }

        public void setUser(PUserHolder us) {
            this.user = us;
        }

        public void setWheelDiam(long wheelDiam) {
            this.wheelDiam = wheelDiam;
        }
        protected abstract double getKmHSpeed(double spdvalpermin,boolean meanspeed);


        public long getSessionStart() {
            return sessionStart;
        }

        public void setSessionStart(long sessionStart) {
            if (this.sessionStart == 0)
                this.sessionStart = sessionStart;
        }

        private long sessionStart;

        public boolean inPause() {
            return firstMove < FIRST_MOVE_THRESHOLD || System.currentTimeMillis()-lastUpdateTime>=PAUSE_DELAY_DETECT_THRESHOLD;
        }

        public IntUpdater() {
            reset();
        }

        public void reset() {
            lastUpdateTime = 0;
            sessionStart = 0;
            nUpdates = 0;
            sumTime = 0;
            sumSpeed = 0.0;
            firstMove = 0;
            lastValue = 0;
            sensVal_o = 0;
            sensVal_old = 0;
            if (sensGear!=null)
                Arrays.fill(sensGear,0);
            if (timeGear!=null)
                Arrays.fill(timeGear,0);
            Log.v(TAG,"Resetting");
        }

        public void setOffsets() {
            sensVal_o = sensVal_old;
            Log.v(TAG,"Setting offset "+sensVal_o);
        }

        public boolean step(WahooBlueSCHolder sh) {
            boolean wasinpause = false;
            long now = System.currentTimeMillis();
            long diff = now-lastUpdateTime;
            if (sh.sensVal == lastValue || diff>PAUSE_DELAY_DETECT_THRESHOLD) {
                firstMove = 0;
            }
            else {
                if (firstMove < FIRST_MOVE_THRESHOLD)
                    firstMove++;
                wasinpause = inPause();
            }
            double pconv = sh.sensSpd;
            //sh.sensSpd = convertSensSpd(sh.sensSpd);
            if (!wasinpause && !inPause()) {
                sumTime += diff;
                timeGear[currentGear] += diff;
                nUpdates++;
                sumSpeed += sh.sensSpd;
            }
            if (sessionStart == 0)
                sessionStart = now;
            sh.updateN = nUpdates;
            lastUpdateTime = now;
            lastValue = sh.sensVal;
            sh.sensVal += sensVal_o;
            sh.gear = currentGear;
            if (nUpdates > 0) {
                diff = sh.sensVal-sensVal_old;
                if (diff>=0)
                    sensGear[currentGear] += diff;
                sh.sensSpdMn = sumSpeed / (double) nUpdates;
                sh.sensSpdMnR = (double) sh.sensVal / (sumTime / 60000.0);
                Log.v(TAG,"pre = "+pconv+" post = "+ sh.sensSpd+" latorsensspd = "+sh.sensSpdMn+" R = "+sh.sensSpdMnR);
                sh.timeRms = sumTime;
                sh.timeRAbsms = now - sessionStart;
                double tm = (double) sumTime / 1000.0;
                sh.timeR = (short) (tm + 0.5);
                sh.speedKmHmn = getKmHSpeed(sh.sensSpdMnR,true);
                sh.speedKmH = getKmHSpeed(sh.sensSpd,false);
                sh.distance = sh.speedKmHmn *(sumTime/3600000.0);
                sh.calorie = calcCalorie(sh);
            }
            sensVal_old = sh.sensVal;
            return inPause();
        }

        protected short calcCalorie(WahooBlueSCHolder sh) {
            //tireValues = [0.005, 0.004, 0.012];
            //aeroValues = [0.388, 0.445, 0.420, 0.300, 0.233, 0.200];
            double rweightv = user == null ? 75 : user.getWeight();
            double bweightv = 8.0; // peso medio bicicletta
            double rollingRes = 0.007; //media tireValues
            double frontalArea = 0.331; //media aeroValues
            double gradev = 0.0 * 0.01; //percentuale salita
            double headwindv = 0.0 / 3.6;  // vento in k/h converted to m/s
            double distancev = sh.distance; //distanza in Km
            double temperaturev = 23.0; // temperatura media
            double elevationv = 0.0; //livello del mare
            double transv = 0.95; // no one knows what this is, so why bother presenting a choice?

		/* Common calculations */
            double density = (1.293 - 0.00426 * temperaturev) * Math.exp(-elevationv / 7000.0);
            double twt = 9.8 * (rweightv + bweightv);  // total weight in newtons
            double A2 = 0.5 * frontalArea * density;  // full air resistance parameter
            double tres = twt * (gradev + rollingRes); // gravity and rolling resistance
            double v = sh.speedKmHmn / 3.6;  // converted to m/s;
            double tv = v + headwindv;
            double A2Eff = (tv > 0.0) ? A2 : -A2; // wind in face, must reverse effect
            double powerv = (v * tres + v * tv * tv * A2Eff) / transv;
            double t;
            if (v > 0.0)
                t = 16.6667 * distancev / v;  // v is m/s here, t is in minutes
            else
                t = 0.0;  // don't want any div by zero errors
        /* Common calculations */

            // c = t * 60.0 * powerv / 0.25 / 1000.0; // kilowatt-seconds, aka kilojoules. t is converted to seconds from minutes, 25% conversion efficiency
            double c = t * powerv * 0.24;  // simplified
            double wl = c / 32318.0; // comes from 1 lb = 3500 Calories
            return (short) (c * 0.2388 + 0.5);
        }

    }

    private static class WheelRevIntUpdater extends IntUpdater {

        @Override
        protected double getKmHSpeed(double s,boolean mean) {
            return s*(wheelDiam * Math.PI) * 60.0/1000000.0;
        }
    }

    private class CrankRevIntUpdater extends IntUpdater {


        @Override
        protected double getKmHSpeed(double s,boolean mean) {
            if (mean) {
                double num = 0.0;
                long den = 0;
                for (int i = 0; i < sensGear.length; i++) {
                    den += timeGear[i];
                    num += sensGear[i] * gearFactor[i];
                }
                return den>0?num * (wheelDiam * Math.PI)*3.6 / den:0.0;
            }
            else
                return s*gearFactor[currentGear]*(wheelDiam * Math.PI) * 60.0/1000000.0;
        }
    }


    public WahooBlueSCDeviceSimulator() {
        reset();
    }

    @Override
    public void reset() {
        crank.reset();
        wheel.reset();
    }



    @Override
    public boolean step(DeviceUpdate du) {
        WahooBlueSCHolder sh = (WahooBlueSCHolder) du;
        boolean pause = true;
        if (crank.getSessionStart()==0 && wheel.getSessionStart()==0) {
            this.wheel.setWheelDiam(wheelDiam);
            this.crank.setWheelDiam(wheelDiam);
            this.wheel.setUser(user);
            this.crank.setUser(user);
            this.wheel.setGearFactor(gearFactor);
            this.crank.setGearFactor(gearFactor);
        }
        if (sh.sensType == WahooBlueSCHolder.SensorType.CRANK) {
            pause = crank.step(sh);
            sh.updateN+=wheel.nUpdates;
            wheel.setSessionStart(crank.getSessionStart());
        } else if (sh.sensType == WahooBlueSCHolder.SensorType.WHEEL) {
            pause = wheel.step(sh);
            sh.updateN+=crank.nUpdates;
            crank.setSessionStart(wheel.getSessionStart());
        }
        return pause;
    }

    @Override
    public void setOffsets() {
        wheel.setOffsets();
        crank.setOffsets();
    }

}
