package com.moviz.lib.comunication.message;

import java.nio.ByteBuffer;

public class ZephyrHxMWorkoutMessage extends ProtocolMessage implements UpdateCommandMessage {
    private com.moviz.lib.comunication.holder.ZephyrHxMHolder workout = null;

    @Override
    public com.moviz.lib.comunication.holder.DeviceUpdate getUpdate() {
        return workout;
    }

    @Override
    public void setUpdate(com.moviz.lib.comunication.holder.DeviceUpdate dupd) {
        workout = (com.moviz.lib.comunication.holder.ZephyrHxMHolder) dupd;
    }

    public ZephyrHxMWorkoutMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.ZEPHYRHXMWORKOUT_MESSAGE);
    }

    public ZephyrHxMWorkoutMessage(com.moviz.lib.comunication.holder.DeviceUpdate f) {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.ZEPHYRHXMWORKOUT_MESSAGE);
        workout = (com.moviz.lib.comunication.holder.ZephyrHxMHolder) f;
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
            workout = new com.moviz.lib.comunication.holder.ZephyrHxMHolder();
        workout.decode(d, b);
    }

    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl, Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = super.toHolder(cl, cllist, pref);
        rv.addAll(workout.toHolder(cl, cllist, pref));
        return rv;
    }

    @Override
    public void fromHolder(com.moviz.lib.comunication.holder.HolderSetter hs, String pref) {
        super.fromHolder(hs, pref);
        if (workout == null)
            workout = new com.moviz.lib.comunication.holder.ZephyrHxMHolder();
        workout.fromHolder(hs, pref);
    }
}
