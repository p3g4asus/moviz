package com.moviz.lib.comunication;

import java.nio.ByteBuffer;

public interface EncDec {
    public void encode(IEncoder enc, ByteBuffer bb);

    public int eSize(IEncoder enc);

    public EncDec decode(IDecoder dec, ByteBuffer bb);
}
