package com.moviz.lib.hw;

import com.moviz.lib.comunication.plus.holder.PUserHolder;

public class PafersBinder extends BluetoothChatBinder {
    public PafersBinder() {
    }

    public void setIncline(GenericDevice d, int i) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.setIncline(i);
        }
    }

    public void setProgramParams(GenericDevice d, String pfold, String pfile, long startDelay) {
        PafersDataProcessor dev = (PafersDataProcessor) newDp(d);
        dev.setProgramParams(pfold, pfile, startDelay);
    }

    public void startQuick(GenericDevice d, PUserHolder u) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.startQuick(u);
        }
    }

    public void pause(GenericDevice d) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.pause();
        }
    }

    public void start(GenericDevice d) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.start();
        }
    }

    public void stop(GenericDevice d) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.stop();
        }
    }

    public void startWatt(GenericDevice d, int watt, PUserHolder user) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.startWatt(watt, user);
        }
    }

    public void startHrc(GenericDevice d, final double speed, final int incline,
                         final int pulse, final PUserHolder user) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.startHrc(speed, incline, pulse, user);
        }
    }

    public void setTargetPulse(GenericDevice d, int pulse) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.setTargetPulse(pulse);
        }
    }

    public void setTargetWatt(GenericDevice d, int watt) {
        PafersDataProcessor dev = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dev != null) {
            dev.setTargetWatt(watt);
        }
    }

    public String getProgramFold(GenericDevice d) {
        PafersDataProcessor dch = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getProgramFold();
        else
            return "";
    }

    public String getProgramFile(GenericDevice d) {
        PafersDataProcessor dch = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getProgramFile();
        else
            return "";
    }

    public long getProgramDelay(GenericDevice d) {
        PafersDataProcessor dch = (PafersDataProcessor) mDevices.get(d.getAddress());
        if (dch != null)
            return dch.getProgramDelay();
        else
            return 0;
    }
}
