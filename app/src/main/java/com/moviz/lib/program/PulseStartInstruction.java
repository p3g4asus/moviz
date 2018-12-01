package com.moviz.lib.program;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.hw.PafersDataProcessor;

public class PulseStartInstruction extends ProgramInstruction {
    private int incline = 0;
    private int pulse = 0;
    private double speed = 0.0;
    private PUserHolder user = null;

    public PulseStartInstruction(PafersDataProcessor dev, ProgramParser d, int inc, double spd, int pul, PUserHolder us) {
        super(dev, d);
        incline = inc;
        speed = spd;
        pulse = pul;
        user = us;
    }

    @Override
    protected void execute() {
        device.startHrc(speed, incline, pulse, user);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "startHrc(" + speed + ", " + incline + ", " + pulse + ", " + user + ")";
    }

}
