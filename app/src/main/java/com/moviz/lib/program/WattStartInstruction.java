package com.moviz.lib.program;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.hw.PafersDataProcessor;

public class WattStartInstruction extends ProgramInstruction {
    private int wattValue = 0;
    private PUserHolder user = null;

    public WattStartInstruction(PafersDataProcessor dev, ProgramParser d, int w, PUserHolder us) {
        super(dev, d);
        wattValue = w;
        user = us;
    }

    @Override
    protected void execute() {
        device.startWatt(wattValue, user);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "startWatt(" + wattValue + ", " + user + ")";
    }

}
