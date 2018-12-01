package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;

import java.nio.ByteBuffer;

public class ProgramChangeMessage extends ProtocolMessage {
    private String program = "";

    public String getProgram() {
        return program;
    }

    public ProgramChangeMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.PROGRAMCHANGE_MESSAGE);
    }

    public ProgramChangeMessage(String prg) {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.PROGRAMCHANGE_MESSAGE);
        program = prg;
    }

    @Override
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from) {
        program = dec.decodeString(from);
    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder enc, ByteBuffer to) {
        enc.encodeString(program, to);
    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e) {
        return e.getStringSize(program);
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        try {
            Holder hld = cl.newInstance();
            hld.setId(pref + "programchange.program");
            hld.sO(program);
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
        program = hs.get(pref + "programchange.program").getString();
    }

}
