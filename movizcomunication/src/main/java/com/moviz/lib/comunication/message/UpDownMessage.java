package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.IEncoder;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class UpDownMessage extends ProtocolMessage {
    private int how = 1;

    public int getHow() {
        return how;
    }

    public UpDownMessage() {
        super(TCPMessageTypes.UPDOWN_MESSAGE);
    }

    public UpDownMessage(int h) {
        super(TCPMessageTypes.UPDOWN_MESSAGE);
        how = h;
    }

    @Override
    public void decode(IDecoder dec, ByteBuffer from) {
        how = dec.decodeByte(from);
        how = how > 127 ? how - 256 : how;
    }

    @Override
    public void encode(IEncoder enc, ByteBuffer to) {
        enc.encodeByte(how, to);
    }

    @Override
    public int getEncodedContentSize(IEncoder e) {
        return e.getByteSize();
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        try {
            Holder hld = cl.newInstance();
            hld.setId(pref + "updown.how");
            hld.sO((byte) how);
            rv.add(hld);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        super.fromHolder(hs, pref);
        how = hs.get(pref + "updown.how").getInt();
    }

}
