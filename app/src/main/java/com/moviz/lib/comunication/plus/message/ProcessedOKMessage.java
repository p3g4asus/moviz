package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;

/**
 * Created by Matteo on 01/11/2016.
 */

public class ProcessedOKMessage implements BaseMessage {
    public ProcessedOKMessage() {

    }

    @Override
    public byte getType() {
        return 0x13;
    }
}
