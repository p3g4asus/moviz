package com.moviz.lib.comunication.message;

public interface UpdateCommandMessage extends CommandMessage {
    public com.moviz.lib.comunication.holder.DeviceUpdate getUpdate();

    public void setUpdate(com.moviz.lib.comunication.holder.DeviceUpdate d);
}
