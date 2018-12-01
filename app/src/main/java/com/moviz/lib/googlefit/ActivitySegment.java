package com.moviz.lib.googlefit;

public class ActivitySegment {
    public long tStart = -1;
    public long tStop = -1;

    public boolean valid() {
        return tStart >= 0 && tStop >= 0 && tStart < tStop;
    }

    public ActivitySegment(long start) {
        tStart = start;
    }

    public ActivitySegment(long sta, long sto) {
        tStart = sta;
        tStop = sto;
    }

    public boolean c(long t) {
        return t >= tStart && (t <= tStop || tStop < 0);
    }
}
