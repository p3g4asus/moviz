package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class HRDeviceWorkoutMessage extends ProtocolMessage implements com.moviz.lib.comunication.message.UpdateCommandMessage {
    private com.moviz.lib.comunication.holder.HRDeviceHolder workout = null;

    @Override
    public DeviceUpdate getUpdate() {
        return workout;
    }

    @Override
    public void setUpdate(DeviceUpdate dupd) {
        workout = (com.moviz.lib.comunication.holder.HRDeviceHolder) dupd;
    }

    public HRDeviceWorkoutMessage() {
        super(TCPMessageTypes.HRDEVICEWORKOUT_MESSAGE);
    }

    public HRDeviceWorkoutMessage(DeviceUpdate f) {
        super(TCPMessageTypes.HRDEVICEWORKOUT_MESSAGE);
        workout = (com.moviz.lib.comunication.holder.HRDeviceHolder) f;
    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder enc) {
        if (workout == null)
            return 0;
        else
            return workout.eSize(enc);
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer b) {
        workout.encode(enc, b);
    }

    @Override
    public void decode(com.moviz.lib.comunication.IDecoder d, ByteBuffer b) {
        if (workout == null)
            workout = new com.moviz.lib.comunication.holder.HRDeviceHolder();
        workout.decode(d, b);
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        rv.addAll(workout.toHolder(cl, cllist, pref));
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        super.fromHolder(hs, pref);
        if (workout == null)
            workout = new com.moviz.lib.comunication.holder.HRDeviceHolder();
        workout.fromHolder(hs, pref);
    }
}