package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.IEncoder;
import com.moviz.lib.comunication.tcp.TCPMessageTypes;

import java.nio.ByteBuffer;

public class UserListRequestMessage extends ProtocolMessage {

    public UserListRequestMessage() {
        super(TCPMessageTypes.USERLISTREQUEST_MESSAGE);
    }

    @Override
    public void decode(IDecoder dec, ByteBuffer from) {
        // TODO Auto-generated method stub

    }

    @Override
    public void encode(IEncoder dec, ByteBuffer to) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEncodedContentSize(IEncoder e) {
        // TODO Auto-generated method stub
        return 0;
    }

}
