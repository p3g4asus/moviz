package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;

import java.nio.ByteBuffer;


public abstract class ProtocolMessage implements CommandMessage {
    protected byte type = -1;

    public byte getType() {
        return type;
    }

    public ProtocolMessage(byte rv) {
        type = rv;
    }

    public static ProtocolMessage mFromHolder(HolderSetter hs, String pref) {
        String clname = hs.get(pref + "class").getString();
        ProtocolMessage rv = null;
        try {
            rv = (ProtocolMessage) Class.forName(clname).newInstance();
            rv.fromHolder(hs, pref);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(HolderSetter hs, String pref) {
        type = hs.get(pref + "type").getByte();
    }

    public abstract void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from);

    public abstract void encode(com.moviz.lib.comunication.IEncoder dec, ByteBuffer to);

    public abstract int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e);

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = null;
        try {
            rv = cllist.newInstance();
            Holder hld = cl.newInstance();
            hld.setId(pref + "type");
            hld.sO(type);
            rv.add(hld);
            hld = cl.newInstance();
            hld.setId(pref + "class");
            hld.sO(getClass().getName());
            rv.add(hld);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }
}
