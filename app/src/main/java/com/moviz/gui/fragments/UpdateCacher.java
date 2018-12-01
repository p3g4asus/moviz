package com.moviz.gui.fragments;

import android.app.Activity;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateCacher {
    private static UpdateCacher instance = null;
    private ConcurrentHashMap<DeviceFragment, Boolean> sources = new ConcurrentHashMap<>();
    private long updateDelay = -1;
    private Timer updateTimer = null;

    public static UpdateCacher newInstance(long delay) {
        if (instance == null)
            instance = new UpdateCacher(delay);
        else
            instance.setDelay(delay);
        return instance;
    }

    public void setDelay(long newdelay) {
        if (newdelay != updateDelay) {
            updateDelay = newdelay;
            if (updateTimer != null) {
                updateTimer.cancel();
                updateTimer = null;
            }
            if (updateDelay > 0) {
                updateTimer = new Timer();
                updateTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        asynchUpdateVelocity();
                    }

                }, updateDelay, updateDelay);
            }
        }

    }

    private UpdateCacher(long delay) {
        setDelay(delay);

    }

    public void notifyNeedsUpdate(DeviceFragment d) {
        if (updateDelay == 0)
            d.updateVelocity();
        else
            sources.put(d, true);
    }

    public void registerSource(DeviceFragment d) {
        sources.put(d, false);
    }

    public void unregisterSource(DeviceFragment d) {
        sources.remove(d);
    }

    //tolto
    protected void asynchUpdateVelocity() {
        if (!sources.isEmpty()) {
            Activity a = null;
            DeviceFragment d;
            for (Map.Entry<DeviceFragment, Boolean> entry : sources.entrySet()) {
                d = entry.getKey();
                if (d.isVisible())
                    a = d.getActivity();
                if (entry.getValue() && a != null) {
                    d.updateVelocity();
                    entry.setValue(false);
                }
            }
        }
    }
}
