package com.moviz.lib.utils;

import com.moviz.lib.comunication.message.BaseMessage;

/**
 * Created by Matteo on 01/11/2016.
 */

public interface CommandManager {
    void addCommandProcessor(CommandProcessor cmdp, Class<? extends BaseMessage>... messages);

    void removeCommandProcessor(CommandProcessor cmdp, Class<? extends BaseMessage>... messages);

    void postMessage(BaseMessage bm, CommandProcessor source);
}
