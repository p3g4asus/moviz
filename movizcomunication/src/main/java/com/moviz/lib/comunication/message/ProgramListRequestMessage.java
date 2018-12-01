package com.moviz.lib.comunication.message;

import java.nio.ByteBuffer;

public class ProgramListRequestMessage extends ProtocolMessage {

    public ProgramListRequestMessage() {
        super(com.moviz.lib.comunication.tcp.TCPMessageTypes.PROGRAMLISTREQUEST_MESSAGE);
    }

    @Override
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from) {

    }

    @Override
    public void encode(com.moviz.lib.comunication.IEncoder dec, ByteBuffer to) {
    }

    @Override
    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e) {
        return 0;
    }

}
