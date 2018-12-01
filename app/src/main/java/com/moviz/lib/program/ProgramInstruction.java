package com.moviz.lib.program;

import com.moviz.lib.hw.PafersDataProcessor;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class ProgramInstruction {
    public static long DEFAULT_DELAY = 200L;
    protected long delay = DEFAULT_DELAY;
    protected long scheduletime = 0;
    protected long pausetime = 0;
    protected boolean executed = false;
    protected ExecuteTask currentTask = null;

    protected abstract void execute();

    protected PafersDataProcessor device = null;
    protected ProgramParser parser = null;

    private class ExecuteTask extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            parser.setCurrentStatus(getStringRepr());
            execute();
            executed = true;
        }

    }

    ;

    public ProgramInstruction(PafersDataProcessor dev, ProgramParser par) {
        device = dev;
        parser = par;
    }

    public void pause() {
        if (!executed && pausetime == 0 && scheduletime != 0) {
            pausetime = System.currentTimeMillis();
            currentTask.cancel();
            currentTask = null;
        }
    }

    public void reset() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        executed = false;
        pausetime = scheduletime = 0;
    }

    public void schedule(Timer t) {
        if (!executed) {
            if (pausetime != 0) {
                scheduletime += System.currentTimeMillis() - pausetime;
                pausetime = 0;
            } else
                scheduletime = System.currentTimeMillis() + delay;
            currentTask = new ExecuteTask();
            t.schedule(currentTask, new Date(scheduletime));
        }
    }

    public void setDelay(long val) {
        // TODO Auto-generated method stub
        delay = val;
    }

    public abstract String getStringRepr();

    @Override
    public String toString() {
        return getStringRepr();
    }

    public boolean isExecuted() {
        // TODO Auto-generated method stub
        return executed;
    }
}
