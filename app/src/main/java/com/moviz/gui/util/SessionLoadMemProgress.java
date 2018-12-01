package com.moviz.gui.util;

import com.moviz.lib.comunication.plus.holder.PSessionHolder;

import java.util.List;

public class SessionLoadMemProgress {
    public List<PSessionHolder> sessions;
    public long mainSid;
    public int cur;

    public SessionLoadMemProgress(int c, long msid, List<PSessionHolder> s) {
        cur = c;
        mainSid = msid;
        sessions = s;
    }
}