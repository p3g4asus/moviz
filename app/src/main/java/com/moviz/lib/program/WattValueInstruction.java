package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class WattValueInstruction extends ProgramInstruction {
    private int wattValue = 0;

    public WattValueInstruction(PafersDataProcessor dev, ProgramParser d, int w) {
        super(dev, d);
        wattValue = w;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.setTargetWatt(wattValue);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "setTargetWatt(" + wattValue + ")";
    }

}
