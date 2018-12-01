package com.moviz.lib.comunication.message;

import java.nio.ByteBuffer;

public interface CommandMessage extends com.moviz.lib.comunication.holder.Holderable, BaseMessage {
    public void decode(com.moviz.lib.comunication.IDecoder dec, ByteBuffer from);

    public void encode(com.moviz.lib.comunication.IEncoder dec, ByteBuffer to);

    public int getEncodedContentSize(com.moviz.lib.comunication.IEncoder e);
}
