package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;

/**
 * Created by Matteo on 12/12/2015.
 */
public class TerminateMessage implements BaseMessage {
    @Override
    public byte getType() {
        return 0;
    }
}
