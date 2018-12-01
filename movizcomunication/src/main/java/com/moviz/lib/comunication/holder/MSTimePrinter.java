package com.moviz.lib.comunication.holder;

public class MSTimePrinter implements HolderPrinter {
    boolean ms = true;

    public MSTimePrinter() {

    }

    public MSTimePrinter(boolean m) {
        ms = m;
    }

    public static String time2Str(long durationMs) {
        int dir = (int) (durationMs / 1000), r;
        int hr = dir / 3600;
        int min = (r = dir - hr * 3600) / 60;
        int sec = r - min * 60;
        return String.format("%d:%02d:%02d", hr, min, sec);
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h) {
        return time2Str(ms ? h.getLong() : h.getLong() * 1000);
    }

    @Override
    public String printVal(com.moviz.lib.comunication.holder.Holder h, Object form) {
        return printVal(h);
    }

}