package com.moviz.lib.comunication.message;

import java.nio.ByteBuffer;

public class StartMessage extends ProtocolMessage {

    public StartMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.START_MESSAGE);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from) {
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
