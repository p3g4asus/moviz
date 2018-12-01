package com.moviz.lib.comunication.plus.message;

import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.plus.holder.PUserHolder;

/**
 * Created by Matteo on 01/11/2016.
 */

public class UserSetMessage implements BaseMessage {
    public PUserHolder getUser() {
        return mUser;
    }

    private PUserHolder mUser;

    public UserSetMessage(PUserHolder us) {
        mUser = us;
    }

    @Override
    public byte getType() {
        return 0x15;
    }
}
