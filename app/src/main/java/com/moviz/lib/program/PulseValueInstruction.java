package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class PulseValueInstruction extends ProgramInstruction {
    private int pulseValue = 0;

    public PulseValueInstruction(PafersDataProcessor dev, ProgramParser d, int w) {
        super(dev, d);
        pulseValue = w;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.setTargetPulse(pulseValue);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "setTargetPulse(" + pulseValue + ")";
    }

}
