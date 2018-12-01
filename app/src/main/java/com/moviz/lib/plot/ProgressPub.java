package com.moviz.lib.plot;

public abstract class ProgressPub<T> {
    int oldrp = -1;

    public ProgressPub() {

    }

    public abstract void publishProgress(T s, int cur, int tot);

    public void calcProgress(T s, int cur, int tot) {
        int rp = (int) ((double) cur / (double) tot * 100.0 + 0.5);
        if (cur == tot || rp > 100)
            rp = 100;
        if (oldrp != rp) {
            oldrp = rp;
            publishProgress(s, rp, 100);
        }
    }
}
