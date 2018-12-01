package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

/**
 * Created by Matteo on 30/10/2016.
 */

public class WahooBlueSCWorkoutMessage extends ProtocolMessage implements com.moviz.lib.comunication.message.UpdateCommandMessage {

    private WahooBlueSCHolder workout = null;

    @Override
    public DeviceUpdate getUpdate() {
        return workout;
    }

    @Override
    public void setUpdate(DeviceUpdate dupd) {
        workout = (WahooBlueSCHolder) dupd;
    }

    public WahooBlueSCWorkoutMessage() {
        super(TCPMessageTypes.WAHOOBLUESCWORKOUT_MESSAGE);
    }

    public WahooBlueSCWorkoutMessage(DeviceUpdate f) {
        super(TCPMessageTypes.WAHOOBLUESCWORKOUT_MESSAGE);
        workout = (WahooBlueSCHolder) f;
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
            workout = new WahooBlueSCHolder();
        workout.decode(d, b);
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        rv.addAll(workout.toHolder(cl, cllist, pref + "wahoobluescworkout."));
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        if (workout == null)
            workout = new WahooBlueSCHolder();
        super.fromHolder(hs, pref);
        workout.fromHolder(hs, pref + "wahoobluescworkout.");
    }

}