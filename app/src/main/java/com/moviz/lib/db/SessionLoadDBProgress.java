package com.moviz.lib.db;

import com.moviz.lib.comunication.plus.holder.PSessionHolder;

public class SessionLoadDBProgress {
    public int cur;
    public int tot;
    public PSessionHolder session;
    public long mainSid;

    public SessionLoadDBProgress(int c, int t, long msid, PSessionHolder s) {
        cur = c;
        tot = t;
        session = s;
        mainSid = msid;
    }
}
