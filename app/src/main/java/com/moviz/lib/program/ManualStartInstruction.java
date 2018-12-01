package com.moviz.lib.program;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.hw.PafersDataProcessor;

public class ManualStartInstruction extends ProgramInstruction {
    private int minutes = 0;
    private int calories = 0;
    private double distance = 0.0;
    private PUserHolder user = null;

    public ManualStartInstruction(PafersDataProcessor dev, ProgramParser d, int min, double dis, int cal, PUserHolder us) {
        super(dev, d);
        minutes = min;
        distance = dis;
        calories = cal;
        user = us;
    }

    @Override
    protected void execute() {
        if (minutes == 0 && distance == 0.0 && calories == 0)
            device.startQuick(user);
        else
            device.startCustom(minutes, distance, calories, user);
    }

    @Override
    public String getStringRepr() {
        // TODO Auto-generated method stub
        if (minutes == 0 && distance == 0.0 && calories == 0)
            return "startQuick(" + user + ")";
        else
            return "startCustom(" + minutes + ", " + distance + ", " + calories + ", " + user + ")";
    }

}
