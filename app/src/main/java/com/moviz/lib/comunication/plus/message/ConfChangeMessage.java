package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;

/**
 * Created by Matteo on 01/11/2016.
 */

public class ConfChangeMessage implements BaseMessage {
    public long getConfId() {
        return mConf;
    }
    public String getConfName() {
        return mConfName;
    }

    private long mConf = -1;
    private String mConfName = null;

    public ConfChangeMessage(long id) {
        mConf = id;
    }

    public ConfChangeMessage(String cn) {
        mConfName = cn;
    }

    @Override
    public byte getType() {
        return 0x12;
    }
}
