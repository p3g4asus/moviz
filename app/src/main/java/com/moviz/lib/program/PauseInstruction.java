package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class PauseInstruction extends ProgramInstruction {

    public PauseInstruction(PafersDataProcessor dev, ProgramParser d) {
        super(dev, d);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.pause();
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "pause()";
    }

}
