package com.moviz.lib.comunication.holder;


public interface DeviceUpdate extends Holderable {
    public long getTs();

    public int getUpdateN();

    public long getAbsTs();

    public void adjustAbsTs(long d);

    public void copyFrom(DeviceUpdate u);

    public long getSessionId();

    public void setSessionId(long i);

}
