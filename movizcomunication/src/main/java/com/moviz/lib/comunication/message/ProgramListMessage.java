package com.moviz.lib.comunication.message;

import java.nio.ByteBuffer;

public class ProgramListMessage extends ProtocolMessage {

    private String list = "";

    public String getList() {
        return list;
    }

    public ProgramListMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.PROGRAMLIST_MESSAGE);
    }

    public ProgramListMessage(String l) {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.PROGRAMLIST_MESSAGE);
        list = l;
    }

    @Override
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from) {
        list = dec.decodeString(from);
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer to) {
        enc.encodeString(list, to);
    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e) {
        return e.getStringSize(list);
    }

    @Override
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends com.moviz.lib.comunication.holder.Holder> cl, Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref) {
        com.moviz.lib.comunication.holder.HolderSetter rv = super.toHolder(cl, cllist, pref);
        try {
            com.moviz.lib.comunication.holder.Holder hld = cl.newInstance();
            hld.setId(pref + "programlist.list");
            hld.sO(list);
            rv.add(hld);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rv;
    }

    @Override
    public void fromHolder(com.moviz.lib.comunication.holder.HolderSetter hs, String pref) {
        super.fromHolder(hs, pref);
        list = hs.get(pref + "programlist.list").getString();
    }
}
