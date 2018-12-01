package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.holder.StatusHolder;

import java.nio.ByteBuffer;


public class StatusMessage extends ProtocolMessage {
    private StatusHolder status = null;

    public StatusHolder getStatus() {
        return status;
    }

    public StatusMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.STATUS_MESSAGE);
    }

    public StatusMessage(StatusHolder s) {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.STATUS_MESSAGE);
        status = s;
    }


    @Override
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer b) {
        if (status == null)
            status = new StatusHolder();
        status.decode(dec, b);
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder e, ByteBuffer b) {
        if (status != null)
            status.encode(e, b);
    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e) {
        if (status == null)
            return 0;
        else
            return status.eSize(e);
    }

    @Override
    public HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        rv.addAll(status.toHolder(cl, cllist, pref + "statuschange."));
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        if (status == null)
            status = new StatusHolder();
        super.fromHolder(hs, pref);
        status.fromHolder(hs, pref + "statusmessage.");
    }

}
