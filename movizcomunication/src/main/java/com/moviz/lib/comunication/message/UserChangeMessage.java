package com.moviz.lib.comunication.message;


import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.IEncoder;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.message.ProtocolMessage;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class UserChangeMessage extends ProtocolMessage {
    private String user = "";

    public String getUser() {
        return user;
    }

    public UserChangeMessage(String us) {
        super(TCPMessageTypes.USERCHANGE_MESSAGE);
        user = us;
    }

    public UserChangeMessage() {
        super(TCPMessageTypes.USERCHANGE_MESSAGE);
    }

    @Override
    public void decode(IDecoder dec, ByteBuffer from) {
        user = dec.decodeString(from);
    }

    @Override
    public void encode(IEncoder enc, ByteBuffer to) {
        enc.encodeString(user, to);
    }

    @Override
    public int getEncodedContentSize(IEncoder e) {
        return e.getStringSize(user);
    }

    @Override
    public HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends HolderSetter> cllist, String pref) {
        HolderSetter rv = super.toHolder(cl, cllist, pref);
        try {
            Holder hld = cl.newInstance();
            hld.setId(pref + "userchange.user");
            hld.sO(user);
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
        user = hs.get(pref + "userchange.user").getString();
    }

}
