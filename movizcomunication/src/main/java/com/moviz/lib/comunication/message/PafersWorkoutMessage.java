package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.holder.PafersHolder;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class PafersWorkoutMessage extends ProtocolMessage implements com.moviz.lib.comunication.message.UpdateCommandMessage {

    private PafersHolder workout = null;

    @Override
    public DeviceUpdate getUpdate() {
        return workout;
    }

    @Override
    public void setUpdate(DeviceUpdate dupd) {
        workout = (PafersHolder) dupd;
    }

    public PafersWorkoutMessage() {
        super(TCPMessageTypes.PAFERSWORKOUT_MESSAGE);
    }

    public PafersWorkoutMessage(DeviceUpdate f) {
        super(TCPMessageTypes.PAFERSWORKOUT_MESSAGE);
        workout = (PafersHolder) f;
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
            workout = new PafersHolder();
        workout.decode(d, b);
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        rv.addAll(workout.toHolder(cl, cllist, pref + "pafersworkout."));
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        if (workout == null)
            workout = new PafersHolder();
        super.fromHolder(hs, pref);
        workout.fromHolder(hs, pref + "pafersworkout.");
    }

}
