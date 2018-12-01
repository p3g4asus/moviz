package com.moviz.lib.comunication;

import com.moviz.lib.comunication.message.CommandMessage;

import java.nio.ByteBuffer;


public interface IDecoder {

    public int decodeInt(ByteBuffer b);

    public int decodeByte(ByteBuffer b);

    public int decodeShort(ByteBuffer b);

    public double decodeDouble(ByteBuffer b);

    public String decodeString(ByteBuffer b);

    public CommandMessage decodeMsg(ByteBuffer b);

    public CommandMessage typeToMessage(byte type);
}
