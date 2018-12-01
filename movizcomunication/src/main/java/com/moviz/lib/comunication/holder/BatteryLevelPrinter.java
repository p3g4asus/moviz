package com.moviz.lib.comunication.holder;

/**
 * Created by Matteo on 29/10/2016.
 */

public class BatteryLevelPrinter implements HolderPrinter {
    @Override
    public String printVal(Holder h) {
        return printVal(h, null);
    }

    @Override
    public String printVal(Holder h, Object form) {
        byte b = h.getByte();
        //CRITICAL, LOW, GOOD, UNKNOWN;
        if (b == 0)
            return "CRITICAL";
        else if (b == 1)
            return "LOW";
        else if (b == 2)
            return "GOOD";
        else
            return "UNKNOWN";
    }
}
