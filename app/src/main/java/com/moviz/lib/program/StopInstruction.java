package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class StopInstruction extends ProgramInstruction {

    public StopInstruction(PafersDataProcessor dev, ProgramParser d) {
        super(dev, d);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.stopTh();
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "stop()";
    }
}
