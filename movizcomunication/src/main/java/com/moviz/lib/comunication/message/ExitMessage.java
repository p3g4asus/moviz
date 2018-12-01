package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class ExitMessage extends ProtocolMessage {

    public ExitMessage() {
        super(TCPMessageTypes.EXIT_MESSAGE);
        // TODO Auto-generated constructor stub
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
