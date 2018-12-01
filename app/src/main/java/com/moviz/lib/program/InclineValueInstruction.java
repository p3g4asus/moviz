package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class InclineValueInstruction extends ProgramInstruction {
    private int inclineValue = 0;

    public InclineValueInstruction(PafersDataProcessor dev, ProgramParser d, int w) {
        super(dev, d);
        inclineValue = w;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.setIncline(inclineValue);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "setIncline(" + inclineValue + ")";
    }

}
