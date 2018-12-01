package com.moviz.lib.comunication;

import com.moviz.lib.comunication.message.CommandMessage;

import java.nio.ByteBuffer;


public interface IEncoder {

    public boolean encodeInt(int val, ByteBuffer bb);

    public boolean encodeByte(int val, ByteBuffer bb);

    public boolean encodeShort(int val, ByteBuffer bb);

    public boolean encodeDouble(double val, ByteBuffer bb);

    public boolean encodeString(String val, ByteBuffer bb);

    public void encodeMsg(CommandMessage prt, ByteBuffer bb);

    public int getOverHeadSize();

    public int getIntSize();

    public int getShortSize();

    public int getByteSize();

    public int getDoubleSize();

    public int getStringSize(String s);
}
