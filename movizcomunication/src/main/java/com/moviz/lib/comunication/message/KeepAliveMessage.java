package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class KeepAliveMessage extends ProtocolMessage {

    public KeepAliveMessage() {
        super(TCPMessageTypes.KEEPALIVE_MESSAGE);
    }

    @Override
    public void decode(IDecoder dec, ByteBuffer from) {
        // TODO Auto-generated method stub

    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder dec, ByteBuffer to) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e) {
        // TODO Auto-generated method stub
        return 0;
    }

}
