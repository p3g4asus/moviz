package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

public class ResumeInstruction extends ProgramInstruction {

    public ResumeInstruction(PafersDataProcessor dev, ProgramParser d) {
        super(dev, d);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void execute() {
        device.start();
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        return "start()";
    }
}
