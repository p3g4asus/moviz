package com.moviz.lib.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import timber.log.Timber;

public class VelocityLogger implements LogChute {
    private final static String tag = "Velocity";

    @Override
    public void init(RuntimeServices arg0) throws Exception {
    }

    @Override
    public boolean isLevelEnabled(int level) {
        return level > LogChute.DEBUG_ID;
    }

    @Override
    public void log(int level, String msg) {
        switch (level) {
            case LogChute.DEBUG_ID:
                Timber.tag(tag).d(msg);
                break;
            case LogChute.ERROR_ID:
                Timber.tag(tag).e(msg);
                break;
            case LogChute.INFO_ID:
                Timber.tag(tag).i(msg);
                break;
            case LogChute.TRACE_ID:
                Timber.tag(tag).d(msg);
                break;
            case LogChute.WARN_ID:
                Timber.tag(tag).w(msg);
        }
    }

    @Override
    public void log(int level, String msg, Throwable t) {
        switch (level) {
            case LogChute.DEBUG_ID:
                Timber.tag(tag).d(msg, t);
                break;
            case LogChute.ERROR_ID:
                Timber.tag(tag).e(msg, t);
                break;
            case LogChute.INFO_ID:
                Timber.tag(tag).i(msg, t);
                break;
            case LogChute.TRACE_ID:
                Timber.tag(tag).d(msg, t);
                break;
            case LogChute.WARN_ID:
                Timber.tag(tag).w(msg, t);
        }
    }
}
